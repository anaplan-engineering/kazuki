package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.internal._KMapping
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName

internal fun TypeSpec.Builder.addMappingType(
    interfaceClassDcl: KSClassDeclaration,
    processingState: KazukiSymbolProcessor.ProcessingState,
) = addMappingType(interfaceClassDcl, processingState, false)

internal fun TypeSpec.Builder.addMapping1Type(
    interfaceClassDcl: KSClassDeclaration,
    processingState: KazukiSymbolProcessor.ProcessingState,
) = addMappingType(interfaceClassDcl, processingState, true)

private fun TypeSpec.Builder.addMappingType(
    interfaceClassDcl: KSClassDeclaration,
    processingState: KazukiSymbolProcessor.ProcessingState,
    requiresNonEmpty: Boolean
) {
    val interfaceName = interfaceClassDcl.simpleName.asString()
    val interfaceTypeArguments = interfaceClassDcl.typeParameters.map { it.toTypeVariableName() }
    val interfaceTypeName = if (interfaceTypeArguments.isEmpty()) {
        interfaceClassDcl.toClassName()
    } else {
        interfaceClassDcl.toClassName().parameterizedBy(interfaceTypeArguments)
    }
    val interfaceTypeParameterResolver = interfaceClassDcl.typeParameters.toTypeParameterResolver()

    val properties = interfaceClassDcl.declarations.filterIsInstance<KSPropertyDeclaration>()
    if (properties.filter { it.isAbstract() }.firstOrNull() != null) {
        val propertyNames = properties.map { it.simpleName.asString() }.toList()
        processingState.errors.add("Mapping type $interfaceTypeName may not have properties: $propertyNames")
    }

    val superInterface = if (requiresNonEmpty) Mapping1::class else Mapping::class
    val mappingType =
        interfaceClassDcl.superTypes.single { it.resolve().declaration.qualifiedName?.asString() == superInterface.qualifiedName }
            .resolve()
    val domainType = mappingType.arguments[0].type!!.resolve()
    val rangeType = mappingType.arguments[1].type!!.resolve()
    val domainTypeName = domainType.toTypeName(interfaceTypeParameterResolver)
    val rangeTypeName = rangeType.toTypeName(interfaceTypeParameterResolver)
    val baseMapPropertyName = "baseMap"
    val baseSetPropertyName = "baseSet"
    val enforceInvariantParameterName = "enforceInvariant"
    val superMappingTypeName = mappingType.toClassName().parameterizedBy(domainTypeName, rangeTypeName)
    val suffix = if (requiresNonEmpty) "Mapping1" else "Mapping"
    val implClassName = "${interfaceName}_$suffix"
    val mapType = Map::class.asClassName().parameterizedBy(domainTypeName, rangeTypeName)
    val tupleType = Tuple2::class.asClassName().parameterizedBy(domainTypeName, rangeTypeName)
    val implTypeSpec = TypeSpec.classBuilder(implClassName).apply {
        if (interfaceTypeArguments.isNotEmpty()) {
            addTypeVariables(interfaceTypeArguments)
        }
        addModifiers(KModifier.PRIVATE)
        addSuperinterface(interfaceTypeName)
        addSuperinterface(
            _KMapping::class.asClassName().parameterizedBy(domainTypeName, rangeTypeName, interfaceTypeName)
        )
        addSuperclassConstructorParameter(baseMapPropertyName)
        primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(baseMapPropertyName, mapType)
                .addParameter(
                    ParameterSpec.builder(enforceInvariantParameterName, Boolean::class).defaultValue("true")
                        .build()
                )
                .build()
        )
        addProperty(
            PropertySpec.builder(baseMapPropertyName, mapType, KModifier.OVERRIDE)
                .initializer(baseMapPropertyName).build()
        )
        val setType = if (requiresNonEmpty) Set1::class else Set::class
        addProperty(
            PropertySpec.builder("dom", setType.asClassName().parameterizedBy(domainTypeName), KModifier.OVERRIDE)
                .delegate(CodeBlock.builder().apply {
                    beginControlFlow("lazy")
                    addStatement("%M(%N.keys)", InbuiltNames.asSet, baseMapPropertyName)
                    endControlFlow()
                }.build()).build()
        )
        addProperty(
            PropertySpec.builder("rng", setType.asClassName().parameterizedBy(rangeTypeName), KModifier.OVERRIDE)
                .delegate(CodeBlock.builder().apply {
                    beginControlFlow("lazy")
                    addStatement("%M(%N.values)", InbuiltNames.asSet, baseMapPropertyName)
                    endControlFlow()
                }.build()).build()
        )
        addProperty(
            PropertySpec.builder("size", Int::class.asTypeName()).addModifiers(KModifier.OVERRIDE)
                .delegate("$baseMapPropertyName::size").build()
        )
        if (requiresNonEmpty) {
            addProperty(
                PropertySpec.builder("card", nat1::class.asTypeName()).addModifiers(KModifier.OVERRIDE)
                    .delegate("$baseMapPropertyName::size").build()
            )
        }

        // N.B. it is important to have properties before init block
        val additionalInvariantParts = if (requiresNonEmpty) listOf("card > 0") else emptyList()
        // TODO -- should we get this from super interface -- Sequence1.atLeastOneElement()
        addInvariantFrom(
            interfaceClassDcl,
            false,
            enforceInvariantParameterName,
            processingState,
            additionalInvariantParts
        )

        addFunction(
            FunSpec.builder("get").apply {
                val dParameterName = "d"
                addModifiers(KModifier.OVERRIDE)
                addParameter(dParameterName, domainTypeName)
                returns(rangeTypeName)
                addStatement(
                    "return %N[%N] ?: throw %T(%P)",
                    baseMapPropertyName,
                    dParameterName,
                    PreconditionFailure::class,
                    "\${$dParameterName} not in mapping domain"
                )
            }.build()
        )
        addFunction(
            FunSpec.builder("contains").apply {
                val elementParameterName = "element"
                addModifiers(KModifier.OVERRIDE)
                addParameter(elementParameterName, tupleType)
                returns(Boolean::class)
                addStatement(
                    "return %N[%N._1]·==·%N._2",
                    baseMapPropertyName,
                    elementParameterName,
                    elementParameterName,
                )
            }.build()
        )
        addFunction(
            FunSpec.builder("containsAll").apply {
                val elementsParameterName = "elements"
                addModifiers(KModifier.OVERRIDE)
                addParameter(elementsParameterName, Collection::class.asClassName().parameterizedBy(tupleType))
                returns(Boolean::class)
                addStatement(
                    "return %M(%N)·{ contains(it) }",
                    InbuiltNames.forall,
                    elementsParameterName,
                )
            }.build()
        )
        addFunction(
            FunSpec.builder("construct").apply {
                addModifiers(KModifier.OVERRIDE)
                addParameter(
                    baseMapPropertyName,
                    mapType
                )
                returns(interfaceTypeName)
                addStatement("return %N(%N)", implClassName, baseMapPropertyName)
            }.build()
        )
        addFunction(
            FunSpec.builder("toString").addModifiers(KModifier.OVERRIDE)
                .returns(String::class)
                .addStatement("return \"%N\$%N\"", interfaceName, baseMapPropertyName)
                .build()
        )
        addFunction(
            FunSpec.builder("hashCode").addModifiers(KModifier.OVERRIDE)
                .returns(Int::class).addStatement("return %N.hashCode()", baseMapPropertyName).build()
        )
        addFunction(
            FunSpec.builder("isEmpty").addModifiers(KModifier.OVERRIDE)
                .returns(Boolean::class).addStatement("return %N.isEmpty()", baseMapPropertyName).build()
        )
        addFunction(
            FunSpec.builder("iterator").addModifiers(KModifier.OVERRIDE)
                .returns(
                    Iterator::class.asClassName()
                        .parameterizedBy(tupleType)
                ).addStatement("return %N.iterator()", baseSetPropertyName).build()
        )
        val equalsParameterName = "other"
        addFunction(
            FunSpec.builder("equals").addModifiers(KModifier.OVERRIDE)
                .addParameter(
                    ParameterSpec.builder(equalsParameterName, Any::class.asTypeName().copy(nullable = true))
                        .build()
                )
                .returns(Boolean::class).addCode(CodeBlock.builder().apply {
                    beginControlFlow("if (this === %N)", equalsParameterName)
                    addStatement("return true")
                    endControlFlow()

                    beginControlFlow(
                        "if (%N !is %T)", equalsParameterName, Relation::class.asTypeName().parameterizedBy(
                            STAR, STAR
                        )
                    )
                    addStatement("return false")
                    endControlFlow()

                    addStatement("return %N == %N", baseSetPropertyName, equalsParameterName)

                }.build()).build()
        )
    }.build()
    addType(implTypeSpec)

    val mapletsParameterName = "maplets"
    addFunction(
        FunSpec.builder("mk_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(baseMapPropertyName, mapType)
            returns(interfaceTypeName)
            addStatement("return %N(%N)", implTypeSpec, baseMapPropertyName)
        }.build()
    )
    addFunction(
        FunSpec.builder("mk_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(
                mapletsParameterName,
                tupleType,
                KModifier.VARARG
            )
            returns(interfaceTypeName)
            addStatement(
                "return %N(%T().apply·{ %N.forEach{ put(it._1, it._2) } })",
                implClassName,
                LinkedHashMap::class.asClassName().parameterizedBy(domainTypeName, rangeTypeName),
                mapletsParameterName
            )
        }.build()
    )
    addFunction(
        FunSpec.builder("mk_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(
                mapletsParameterName,
                Iterable::class.asClassName().parameterizedBy(
                    tupleType
                )
            )
            returns(interfaceTypeName)
            addStatement(
                "return %N(%T().apply·{ %N.forEach{ put(it._1, it._2) } })",
                implClassName,
                LinkedHashMap::class.asClassName().parameterizedBy(domainTypeName, rangeTypeName),
                mapletsParameterName
            )
        }.build()
    )
    // TDOO - check for duplicate domains
    addFunction(
        FunSpec.builder("is_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(
                mapletsParameterName, Iterable::class.asClassName().parameterizedBy(
                    tupleType
                )
            )
            returns(Boolean::class)
            addStatement(
                "return %N(%T().apply·{ %N.forEach{ put(it._1, it._2) } }, false).%N()",
                implClassName,
                LinkedHashMap::class.asClassName().parameterizedBy(domainTypeName, rangeTypeName),
                mapletsParameterName,
                validityFunctionName,
            )
        }.build()
    )
    // TDOO - check for duplicate domains
    addFunction(
        FunSpec.builder("is_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(
                mapletsParameterName,
                tupleType,
                KModifier.VARARG
            )
            returns(Boolean::class)
            addStatement(
                "return %N(%T().apply·{ %N.forEach·{ put(it._1, it._2) } }, false).%N()",
                implClassName,
                LinkedHashMap::class.asClassName().parameterizedBy(domainTypeName, rangeTypeName),
                mapletsParameterName,
                validityFunctionName
            )
        }.build()
    )
    addFunction(
        FunSpec.builder("as_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(baseMapPropertyName, superMappingTypeName)
            returns(interfaceTypeName)
            addStatement("return mk_%N(%N)", interfaceName, baseMapPropertyName)
        }.build()
    )
}
