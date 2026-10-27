package com.anaplan.engineering.kazuki.ksp.type

import com.anaplan.engineering.kazuki.core.PrettyPrintable
import com.anaplan.engineering.kazuki.core.Relation
import com.anaplan.engineering.kazuki.core.Set1
import com.anaplan.engineering.kazuki.core.Tuple2
import com.anaplan.engineering.kazuki.core.internal._KRelation
import com.anaplan.engineering.kazuki.core.internal._KazukiObject
import com.anaplan.engineering.kazuki.ksp.*
import com.anaplan.engineering.kazuki.ksp.type.property.PropertyProcessor
import com.anaplan.engineering.kazuki.ksp.type.property.addFunctionProviders
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeVariableName

internal fun TypeSpec.Builder.addRelationType(
    interfaceClassDcl: KSClassDeclaration,
    makeable: Boolean,
    typeGenerationContext: TypeGenerationContext,
    apiModifier: KModifier? = null,
) =
    if (makeable) {
        addRelationType(interfaceClassDcl, typeGenerationContext, false)
    } else {
        // TODO -- is_ / metadata?
    }

internal fun TypeSpec.Builder.addRelation1Type(
    interfaceClassDcl: KSClassDeclaration,
    makeable: Boolean,
    typeGenerationContext: TypeGenerationContext,
    apiModifier: KModifier? = null,
) =
    if (makeable) {
        addRelationType(interfaceClassDcl, typeGenerationContext, true)
    } else {
        // TODO -- is_ / metadata?
    }

@OptIn(KspExperimental::class)
private fun TypeSpec.Builder.addRelationType(
    interfaceClassDcl: KSClassDeclaration,
    typeGenerationContext: TypeGenerationContext,
    requiresNonEmpty: Boolean
) {
    val interfaceName = interfaceClassDcl.simpleName.asString()
    val interfaceTypeArguments = interfaceClassDcl.typeParameters.map { it.toTypeVariableName().stripVariance() }
    val interfaceTypeName = if (interfaceTypeArguments.isEmpty()) {
        interfaceClassDcl.toClassName()
    } else {
        interfaceClassDcl.toClassName().parameterizedBy(interfaceTypeArguments)
    }
    val erasedInterfaceTypeName = if (interfaceTypeArguments.isEmpty()) {
        interfaceClassDcl.toClassName()
    } else {
        interfaceClassDcl.toClassName().parameterizedBy(interfaceTypeArguments.map { STAR })
    }
    val properties = PropertyProcessor(interfaceClassDcl, typeGenerationContext).process()

    // TODO
//    val superInterface = if (requiresNonEmpty) Relation1::class else Relation::class
    val superInterface = Relation::class

    val ancestorTypeParameters = interfaceClassDcl.resolveAncestorTypeArguments(superInterface.qualifiedName!!)
    val domainTypeName = ancestorTypeParameters.getTypeName(0)
    val rangeTypeName = ancestorTypeParameters.getTypeName(1)
    val tupleType = Tuple2::class.asClassName().parameterizedBy(domainTypeName, rangeTypeName)
    val elementsPropertyName = "elements"
    val elementsTypeName = Set::class.asClassName().parameterizedBy(tupleType)
    val superRelationTypeName = Relation::class.asClassName().parameterizedBy(domainTypeName, rangeTypeName)
    val suffix = if (requiresNonEmpty) "Relation1" else "Relation"
    val implClassName = "${interfaceName}_$suffix"
    val implTypeSpec = TypeSpec.classBuilder(implClassName).apply {
        if (interfaceTypeArguments.isNotEmpty()) {
            addTypeVariables(interfaceTypeArguments)
        }
        addModifiers(KModifier.PRIVATE)
        addSuperinterface(interfaceTypeName)
        addSuperinterface(_KRelation::class.asClassName().parameterizedBy(domainTypeName, rangeTypeName, interfaceTypeName))
        addSuperinterface(Set::class.asClassName().parameterizedBy(tupleType), CodeBlock.of(elementsPropertyName))
        addSuperinterface(InbuiltNames.ConstructableInterfaceClassName)
        addSuperclassConstructorParameter(elementsPropertyName)
        primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(elementsPropertyName, elementsTypeName)
                .addParameter(
                    ParameterSpec.builder(enforceInvariantParameterName, Boolean::class).defaultValue("true")
                        .build()
                )
                .build()
        )
        addProperty(
            PropertySpec.builder(elementsPropertyName, elementsTypeName, KModifier.OVERRIDE)
                .initializer(elementsPropertyName).build()
        )
        val setType = if (requiresNonEmpty) Set1::class else Set::class
        val setFunction = if (requiresNonEmpty) InbuiltNames.set1 else InbuiltNames.set
        addProperty(
            PropertySpec.builder("dom", setType.asClassName().parameterizedBy(domainTypeName), KModifier.OVERRIDE)
                .delegate(CodeBlock.builder().apply {
                    beginControlFlow("lazy")
                    addStatement("%M(%N)·{ it._1 }", setFunction, elementsPropertyName)
                    endControlFlow()
                }.build()).build()
        )
        addProperty(
            PropertySpec.builder("rng", setType.asClassName().parameterizedBy(rangeTypeName), KModifier.OVERRIDE)
                .delegate(CodeBlock.builder().apply {
                    beginControlFlow("lazy")
                    addStatement("%M(%N)·{ it._2 }", setFunction, elementsPropertyName)
                    endControlFlow()
                }.build()).build()
        )
        addProperty(
            PropertySpec.builder("size", Int::class.asTypeName()).addModifiers(KModifier.OVERRIDE)
                .delegate("$elementsPropertyName::size").build()
        )
        val comparableWith = addComparableWith(interfaceClassDcl, Relation::class.asClassName(), typeGenerationContext)
        addFunctionProviders(properties.functionProviders, true, typeGenerationContext)

        val hashPropertyName = "hash"
        val delegatedHashObjectName = if (comparableWith.property == null) {
            elementsPropertyName
        } else {
            comparableWith.property.simpleName.getShortName()
        }
        addProperty(
            PropertySpec.builder(hashPropertyName, Int::class, KModifier.PRIVATE)
                .lazy("%N.hashCode()", delegatedHashObjectName)
                .build()
        )

        addInitializerBlock(CodeBlock.builder().apply {
            beginControlFlow(
                "assert (%N !is %T)",
                elementsPropertyName,
                _KazukiObject::class.asTypeName()
            )
            addStatement("%S", InvalidInternalStateType)
            endControlFlow()
        }.build())

        // N.B. it is important to have properties before init block
        val additionalInvariantParts = if (requiresNonEmpty) {
            listOf(FreeformInvariant("atLeastOneElement", "::atLeastOneElement"))
        } else {
            emptyList()
        }
        addInvariantFrom(
            interfaceClassDcl,
            typeGenerationContext,
            additionalInvariantParts
        )

        if (!interfaceClassDcl.hasSuperType(PrettyPrintable::class.qualifiedName!!)) {
            addFunction(
                FunSpec.builder("pretty").apply {
                    addModifiers(KModifier.OVERRIDE)
                    returns(String::class)
                    beginControlFlow("val elementText = $elementsPropertyName.joinToString(%S)", ", ")
                    addStatement("val d = it._1.${InbuiltNames.prettyOrDefault}()")
                    addStatement("val r = it._2.${InbuiltNames.prettyOrDefault}()")
                    val separator = "↦"
                    addStatement("%P", "\$d $separator \$r")
                    endControlFlow()
                    addStatement("return %P", "{\$elementText}")
                }.build()
            )
        }

        addFunction(
            FunSpec.builder("construct").apply {
                addModifiers(KModifier.OVERRIDE)
                addParameter(elementsPropertyName, elementsTypeName)
                returns(interfaceTypeName)
                addStatement("return %N(%N)", implClassName, elementsPropertyName)
            }.build()
        )
        addFunction(
            FunSpec.builder("toString").addModifiers(KModifier.OVERRIDE)
                .returns(String::class)
                .addStatement("return \"%N\$%N\"", interfaceName, elementsPropertyName)
                .build()
        )
        addFunction(
            FunSpec.builder("hashCode").addModifiers(KModifier.OVERRIDE)
                .returns(Int::class).apply {
                    addStatement("return %N", hashPropertyName)
                }.build()
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
                        "if (%N !is %T)",
                        equalsParameterName,
                        _KRelation::class.asClassName().parameterizedBy(STAR, STAR, STAR)
                    )
                    addStatement("return false")
                    endControlFlow()

                    beginControlFlow(
                        "if (!(%N.%N.isInstance(this) && this.%N.isInstance(%N)))",
                        equalsParameterName,
                        comparableWithPropertyName,
                        comparableWithPropertyName,
                        equalsParameterName
                    )
                    addStatement("return false")
                    endControlFlow()

                    if (comparableWith.property == null) {
                        addStatement("return %N == %N", elementsPropertyName, equalsParameterName)
                    } else {
                        val comparablePropertyName = comparableWith.property.simpleName.getShortName()
                        addStatement(
                            "return this.%N == (%N as %T).%N",
                            comparablePropertyName,
                            equalsParameterName,
                            comparableWith.comparableTypeLimitTypeName,
                            comparablePropertyName
                        )
                    }

                }.build()).build()
        )
    }.build()
    addType(implTypeSpec)

    addStaticPrettyFunction(interfaceTypeName, interfaceTypeArguments)
    addFunction(
        FunSpec.builder("mk_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(elementsPropertyName, elementsTypeName)
            returns(interfaceTypeName)
            addStatement("return %N(%N)", implTypeSpec, elementsPropertyName)
        }.build()
    )
    addFunction(
        FunSpec.builder("mk_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(elementsPropertyName, tupleType, KModifier.VARARG)
            returns(interfaceTypeName)
            addStatement("return %N(%N.toSet())", implTypeSpec, elementsPropertyName)
        }.build()
    )
    addFunction(
        FunSpec.builder("is_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            val implTypeArgs = if (interfaceTypeArguments.isEmpty()) {
                ""
            } else {
                "<" + interfaceTypeArguments.joinToString(", ") + ">"
            }
            addParameter(elementsPropertyName, superRelationTypeName)
            returns(Boolean::class)
            addStatement(
                "return (%N is %T)·|| %N$implTypeArgs(%T(%N.size).apply·{ addAll(%N) }, false ).%N()",
                elementsPropertyName,
                erasedInterfaceTypeName,
                implClassName,
                HashSet::class.asClassName().parameterizedBy(tupleType),
                elementsPropertyName,
                elementsPropertyName,
                validityFunctionName
            )
        }.build()
    )
    addFunction(
        FunSpec.builder("as_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(elementsPropertyName, superRelationTypeName)
            returns(interfaceTypeName)
            addCode(CodeBlock.builder().apply {
                beginControlFlow("if (%N is %T)", elementsPropertyName, erasedInterfaceTypeName)
                addStatement("return %N as %T", elementsPropertyName, interfaceTypeName)
                nextControlFlow("else")
                addStatement("return %N(%T(%N.size).apply·{ addAll(%N) })",
                    "mk_$interfaceName",
                    HashSet::class.asClassName().parameterizedBy(tupleType),
                    elementsPropertyName,
                    elementsPropertyName
                )
                endControlFlow()
            }.build())
        }.build()
    )
}
