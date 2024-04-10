package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.core.Map1
import com.anaplan.engineering.kazuki.core.nat
import com.anaplan.engineering.kazuki.core.KMap
import com.anaplan.engineering.kazuki.core.Tuple2
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName

internal fun TypeSpec.Builder.addMapType(
    interfaceClassDcl: KSClassDeclaration,
    processingState: KazukiSymbolProcessor.ProcessingState,
) = addMapType(interfaceClassDcl, processingState, false)

internal fun TypeSpec.Builder.addMap1Type(
    interfaceClassDcl: KSClassDeclaration,
    processingState: KazukiSymbolProcessor.ProcessingState,
) = addMapType(interfaceClassDcl, processingState, true)

private fun TypeSpec.Builder.addMapType(
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
        processingState.errors.add("Map type $interfaceTypeName may not have properties: $propertyNames")
    }

    val superInterface = if (requiresNonEmpty) Map1::class else Map::class
    val mapType =
        interfaceClassDcl.superTypes.single { it.resolve().declaration.qualifiedName?.asString() == superInterface.qualifiedName }
            .resolve()
    val domainType = mapType.arguments[0].type!!.resolve()
    val rangeType = mapType.arguments[1].type!!.resolve()
    val domainTypeName = domainType.toTypeName(interfaceTypeParameterResolver)
    val rangeTypeName = rangeType.toTypeName(interfaceTypeParameterResolver)
    val basePropertyName = "base"
    val enforceInvariantParameterName = "enforceInvariant"
    val superMapTypeName = Map::class.asClassName().parameterizedBy(domainTypeName, rangeTypeName)
    val suffix = if (requiresNonEmpty) "Map1" else "Map"
    val implClassName = "${interfaceName}_$suffix"
    val implTypeSpec = TypeSpec.classBuilder(implClassName).apply {
        if (interfaceTypeArguments.isNotEmpty()) {
            addTypeVariables(interfaceTypeArguments)
        }
        addModifiers(KModifier.PRIVATE)
        addSuperinterface(interfaceTypeName)
        addSuperinterface(superMapTypeName, CodeBlock.of(basePropertyName))
        addSuperinterface(KMap::class.asClassName().parameterizedBy(domainTypeName, rangeTypeName, interfaceTypeName))
        addSuperclassConstructorParameter(basePropertyName)
        primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(basePropertyName, superMapTypeName)
                .addParameter(
                    ParameterSpec.builder(enforceInvariantParameterName, Boolean::class).defaultValue("true")
                        .build()
                )
                .build()
        )
        addProperty(
            PropertySpec.builder(basePropertyName, superMapTypeName, KModifier.OVERRIDE)
                .initializer(basePropertyName).build()
        )
        if (requiresNonEmpty) {
            addProperty(
                PropertySpec.builder("card", nat::class.asTypeName()).addModifiers(KModifier.OVERRIDE)
                    .delegate("$basePropertyName::size").build()
            )
        }

        // N.B. it is important to have properties before init block
        val additionalInvariantParts = if (requiresNonEmpty) listOf("len > 0") else emptyList()
        // TODO -- should we get this from super interface -- Sequence1.atLeastOneElement()
        addInvariantFrom(
            interfaceClassDcl,
            false,
            enforceInvariantParameterName,
            processingState,
            additionalInvariantParts
        )

        addFunction(
            FunSpec.builder("construct").apply {
                addModifiers(KModifier.OVERRIDE)
                addParameter(basePropertyName, superMapTypeName)
                returns(interfaceTypeName)
                addStatement("return %N(%N)", implClassName, basePropertyName)
            }.build()
        )
        addFunction(
            FunSpec.builder("toString").addModifiers(KModifier.OVERRIDE)
                .returns(String::class)
                .addStatement("return \"%N\$%N\"", interfaceName, basePropertyName)
                .build()
        )
        addFunction(
            FunSpec.builder("hashCode").addModifiers(KModifier.OVERRIDE)
                .returns(Int::class).addStatement("return %N.hashCode()", basePropertyName).build()
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
                        "if (%N !is %T)", equalsParameterName, superInterface.asTypeName().parameterizedBy(
                            STAR, STAR
                        )
                    )
                    addStatement("return false")
                    endControlFlow()

                    addStatement("return %N == %N", basePropertyName, equalsParameterName)

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
            addParameter(basePropertyName, superMapTypeName)
            returns(interfaceTypeName)
            addStatement("return %N(%N)", implTypeSpec, basePropertyName)
        }.build()
    )
    addFunction(
        FunSpec.builder("mk_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(
                mapletsParameterName,
                Tuple2::class.asClassName().parameterizedBy(domainTypeName, rangeTypeName),
                KModifier.VARARG
            )
            returns(interfaceTypeName)
            addStatement(
                "return %N(%T().apply{ %N.forEach{ put(it._1, it._2) } })",
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
                Collection::class.asClassName().parameterizedBy(
                    Tuple2::class.asClassName().parameterizedBy(domainTypeName, rangeTypeName)
                )
            )
            returns(interfaceTypeName)
            addStatement(
                "return %N(%T().apply{ %N.forEach{ put(it._1, it._2) } })",
                implClassName,
                LinkedHashMap::class.asClassName().parameterizedBy(domainTypeName, rangeTypeName),
                mapletsParameterName
            )
        }.build()
    )
    addFunction(
        FunSpec.builder("is_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(basePropertyName, superMapTypeName)
            returns(Boolean::class)
            addStatement("return %N(%N, false).%N()", implClassName, basePropertyName, validityFunctionName)
        }.build()
    )
    addFunction(
        FunSpec.builder("is_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(
                mapletsParameterName,
                Tuple2::class.asClassName().parameterizedBy(domainTypeName, rangeTypeName),
                KModifier.VARARG
            )
            returns(Boolean::class)
            addStatement(
                "return %N(%T().apply{ %N.forEach{ put(it._1, it._2) } }, false).%N()",
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
            addParameter(basePropertyName, superMapTypeName)
            returns(interfaceTypeName)
            addStatement("return mk_%N(%N)", interfaceName, basePropertyName)
        }.build()
    )
}
