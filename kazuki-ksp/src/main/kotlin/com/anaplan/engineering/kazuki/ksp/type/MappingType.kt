package com.anaplan.engineering.kazuki.ksp.type

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.internal.*
import com.anaplan.engineering.kazuki.ksp.*
import com.anaplan.engineering.kazuki.ksp.type.property.PropertyProcessor
import com.anaplan.engineering.kazuki.ksp.type.property.addFunctionProviders
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeVariableName

internal fun TypeSpec.Builder.addMappingType(
    interfaceClassDcl: KSClassDeclaration,
    makeable: Boolean,
    typeGenerationContext: TypeGenerationContext,
) = if (makeable) {
    addMappingType(interfaceClassDcl, typeGenerationContext, false, false)
} else {
    // TODO -- is_ / metadata?
}

internal fun TypeSpec.Builder.addMapping1Type(
    interfaceClassDcl: KSClassDeclaration,
    makeable: Boolean,
    typeGenerationContext: TypeGenerationContext,
) = if (makeable) {
    addMappingType(interfaceClassDcl, typeGenerationContext, true, false)
} else {
    // TODO -- is_ / metadata?
}


internal fun TypeSpec.Builder.addInjectiveMappingType(
    interfaceClassDcl: KSClassDeclaration,
    makeable: Boolean,
    typeGenerationContext: TypeGenerationContext,
) = if (makeable) {
    addMappingType(interfaceClassDcl, typeGenerationContext, false, true)
} else {
    // TODO -- is_ / metadata?
}


internal fun TypeSpec.Builder.addInjectiveMapping1Type(
    interfaceClassDcl: KSClassDeclaration,
    makeable: Boolean,
    typeGenerationContext: TypeGenerationContext,
) = if (makeable) {
    addMappingType(interfaceClassDcl, typeGenerationContext, true, true)
} else {
    // TODO -- is_ / metadata?
}


@OptIn(KspExperimental::class)
private fun TypeSpec.Builder.addMappingType(
    interfaceClassDcl: KSClassDeclaration,
    typeGenerationContext: TypeGenerationContext,
    requiresNonEmpty: Boolean,
    injective: Boolean,
) {
    val logger = typeGenerationContext.logger
    val interfaceName = interfaceClassDcl.simpleName.asString()
    val interfaceTypeArguments = interfaceClassDcl.typeParameters.map { it.toTypeVariableName().stripVariance() }
    val interfaceTypeName = if (interfaceTypeArguments.isEmpty()) {
        interfaceClassDcl.toClassName()
    } else {
        interfaceClassDcl.toClassName().parameterizedBy(interfaceTypeArguments)
    }
    val properties = PropertyProcessor(interfaceClassDcl, typeGenerationContext).process()
    val superInterface = if (injective) {
        if (requiresNonEmpty) InjectiveMapping1::class else InjectiveMapping::class
    } else {
        if (requiresNonEmpty) Mapping1::class else Mapping::class
    }
    val mappingType =
        interfaceClassDcl.allSuperTypes.single { it.resolve().declaration.qualifiedName?.asString() == superInterface.qualifiedName }
            .resolve()
    val ancestorTypeParameters = interfaceClassDcl.resolveAncestorTypeArguments(superInterface.qualifiedName!!, logger)
    val domainTypeName = ancestorTypeParameters.getTypeName(0)
    val rangeTypeName = ancestorTypeParameters.getTypeName(1)
    logger.debug("Creating mapping=$interfaceName ${superInterface.simpleName} $domainTypeName->$rangeTypeName")
    val baseMapPropertyName = "baseMap"
    val baseSetPropertyName = "elements"
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
        val mappingClass = if (injective) _KInjectiveMapping::class else _KMapping::class
        addSuperinterfaces(
            listOf(
                mappingClass.asClassName().parameterizedBy(domainTypeName, rangeTypeName, interfaceTypeName),
                _KSet::class.asClassName().parameterizedBy(tupleType, interfaceTypeName),
                InbuiltNames.ConstructableInterfaceClassName
            )
        )
        addSuperclassConstructorParameter(baseMapPropertyName)
        primaryConstructor(
            FunSpec.constructorBuilder().addParameter(baseMapPropertyName, mapType).addParameter(
                ParameterSpec.builder(enforceInvariantParameterName, Boolean::class).defaultValue("true").build()
            ).build()
        )
        addProperty(
            PropertySpec.builder(baseMapPropertyName, mapType, KModifier.OVERRIDE).initializer(baseMapPropertyName)
                .build()
        )
        val setType = if (requiresNonEmpty) Set1::class else Set::class
        val setFunction = if (requiresNonEmpty) InbuiltNames.asSet1 else InbuiltNames.asSet
        addProperty(
            PropertySpec.builder("dom", setType.asClassName().parameterizedBy(domainTypeName), KModifier.OVERRIDE)
                .delegate(CodeBlock.builder().apply {
                    beginControlFlow("lazy")
                    addStatement("%M(%N.keys)", setFunction, baseMapPropertyName)
                    endControlFlow()
                }.build()).build()
        )
        addProperty(
            PropertySpec.builder("rng", setType.asClassName().parameterizedBy(rangeTypeName), KModifier.OVERRIDE)
                .delegate(CodeBlock.builder().apply {
                    beginControlFlow("lazy")
                    addStatement("%M(%N.values)", setFunction, baseMapPropertyName)
                    endControlFlow()
                }.build()).build()
        )
        addProperty(
            PropertySpec.builder("size", Int::class.asTypeName()).addModifiers(KModifier.OVERRIDE)
                .delegate("$baseMapPropertyName::size").build()
        )
        addProperty(
            PropertySpec.builder(baseSetPropertyName, Set::class.asClassName().parameterizedBy(tupleType))
                .addModifiers(KModifier.OVERRIDE)
                .getter(
                    FunSpec.getterBuilder().addStatement("return super.$baseSetPropertyName").build()
                ).build()
        )
        if (requiresNonEmpty) {
            addProperty(
                PropertySpec.builder("card", nat1::class.asTypeName()).addModifiers(KModifier.OVERRIDE)
                    .lazy("%N.size.%M()", baseMapPropertyName, InbuiltNames.toNat1).build()
            )
        }
        // TODO -- inverse should be Mapping1 for IM1
        if (injective) {
            addProperty(
                PropertySpec.builder(
                    "inverse", Mapping::class.asTypeName().parameterizedBy(rangeTypeName, domainTypeName)
                ).addModifiers(KModifier.OVERRIDE).delegate(
                    CodeBlock.builder().apply {
                        beginControlFlow("lazy")
                        addStatement("${InbuiltNames.corePackage}.as_Mapping($baseSetPropertyName.map { (d, r) -> mk_(r, d) })")
                        endControlFlow()
                    }.build()
                ).build()
            )
        }
        // TODO -- should this be Set? -- need _KRelation to extened _KSet if so
        val comparableWith = addComparableWith(interfaceClassDcl, Relation::class.asClassName(), typeGenerationContext)
        addFunctionProviders(properties.functionProviders, true, typeGenerationContext)

        val hashPropertyName = "hash"
        val delegatedHashObjectName = if (comparableWith.property == null) {
            baseSetPropertyName
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
                baseMapPropertyName,
                _KazukiObject::class.asTypeName()
            )
            addStatement("%S", InvalidInternalStateType)
            endControlFlow()
        }.build())

        // N.B. it is important to have properties before init block
        val additionalInvariantParts = if (requiresNonEmpty) {
            listOf(FreeformInvariant("nonEmpty", "{ card·>·0uL }"))
        } else {
            emptyList()
        }
        // TODO -- should we get this from super interface -- Sequence1.atLeastOneElement()
        addInvariantFrom(
            interfaceClassDcl, typeGenerationContext, additionalInvariantParts
        )

        if (!interfaceClassDcl.hasSuperType(PrettyPrintable::class.qualifiedName!!)) {
            addFunction(
                FunSpec.builder("pretty").apply {
                    addModifiers(KModifier.OVERRIDE)
                    returns(String::class)
                    beginControlFlow("val elementText = $baseSetPropertyName.joinToString(%S)", ", ")
                    addStatement("val d = it._1.${InbuiltNames.prettyOrDefault}()")
                    addStatement("val r = it._2.${InbuiltNames.prettyOrDefault}()")
                    val separator = if (injective) "↔" else "↦"
                    addStatement("%P", "\$d $separator \$r")
                    endControlFlow()
                    addStatement("return %P", "{\$elementText}")
                }.build()
            )
        }

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
                    baseMapPropertyName, mapType
                )
                returns(interfaceTypeName)
                addStatement("return %N(%N)", implClassName, baseMapPropertyName)
            }.build()
        )
        addFunction(
            FunSpec.builder("construct").apply {
                addModifiers(KModifier.OVERRIDE)
                addParameter(
                    baseSetPropertyName, Set::class.asClassName().parameterizedBy(tupleType)
                )
                returns(interfaceTypeName)
                addStatement("return super.construct(%N)", baseSetPropertyName)
            }.build()
        )
        addFunction(
            FunSpec.builder("toString").addModifiers(KModifier.OVERRIDE).returns(String::class)
                .addStatement("return \"%N\$%N\"", interfaceName, baseMapPropertyName).build()
        )
        addFunction(
            FunSpec.builder("hashCode").addModifiers(KModifier.OVERRIDE)
                .returns(Int::class).apply {
                    addStatement("return %N", hashPropertyName)
                }.build()
        )
        addFunction(
            FunSpec.builder("isEmpty").addModifiers(KModifier.OVERRIDE).returns(Boolean::class)
                .addStatement("return %N.isEmpty()", baseMapPropertyName).build()
        )
        addFunction(
            FunSpec.builder("iterator").addModifiers(KModifier.OVERRIDE).returns(
                Iterator::class.asClassName().parameterizedBy(tupleType)
            ).addStatement("return %N.iterator()", baseSetPropertyName).build()
        )
        val equalsParameterName = "other"
        addFunction(
            FunSpec.builder("equals").addModifiers(KModifier.OVERRIDE).addParameter(
                ParameterSpec.builder(equalsParameterName, Any::class.asTypeName().copy(nullable = true)).build()
            ).returns(Boolean::class).addCode(CodeBlock.builder().apply {
                beginControlFlow("if (this === %N)", equalsParameterName)
                addStatement("return true")
                endControlFlow()

                beginControlFlow(
                    "if (%N !is %T)", equalsParameterName,
                    // TODO -- see comment above about Set
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
                    addStatement(
                        "return %N == %N.%N", baseSetPropertyName, equalsParameterName, baseSetPropertyName
                    )
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
    val mapletsParameterName = "maplets"
    addFunction(
        FunSpec.builder("as_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(
                mapletsParameterName, Iterable::class.asClassName().parameterizedBy(
                    tupleType
                )
            )
            returns(interfaceTypeName)
            beginControlFlow(
                "val %N = %T().apply",
                baseMapPropertyName,
                LinkedHashMap::class.asClassName().parameterizedBy(domainTypeName, rangeTypeName),
            )
            beginControlFlow("%N.forEach", mapletsParameterName)
            addStatement("(d, r) -> ")
            beginControlFlow(
                "${InbuiltNames.pre}(%P)",
                "\${d.prettyOrDefault()} cannot map to \${get(d).prettyOrDefault()} and \${r.prettyOrDefault()}"
            )
            addStatement("if (containsKey(d)) get(d) == r else true")
            endControlFlow()
            addStatement("put(d, r)")
            endControlFlow()
            endControlFlow()
            if (requiresNonEmpty) {
                beginControlFlow("${InbuiltNames.pre}(%S)", "Cannot create empty $interfaceName")
                addStatement("%N.isNotEmpty()", baseMapPropertyName)
                endControlFlow()
            }
            if (injective) {
                beginControlFlow("${InbuiltNames.pre}(%S)", "$interfaceName map may not have duplicates in range")
                addStatement("%N.keys.size == %N.values.toSet().size", baseMapPropertyName, baseMapPropertyName)
                endControlFlow()
            }
            addStatement("return %N(%N)", implClassName, baseMapPropertyName)
        }.build()
    )
    addFunction(
        FunSpec.builder("mk_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(
                mapletsParameterName, tupleType, KModifier.VARARG
            )
            returns(interfaceTypeName)
            addStatement("return as_$interfaceName(%N.toList())", mapletsParameterName)
        }.build()
    )
    // TDOO - check for duplicate domains
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
            addParameter(
                mapletsParameterName, Iterable::class.asClassName().parameterizedBy(
                    tupleType
                )
            )
            returns(Boolean::class)
            addStatement(
                "return %N$implTypeArgs(%T().apply·{ %N.forEach{ put(it._1, it._2) } }, false).%N()",
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
                mapletsParameterName, tupleType, KModifier.VARARG
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
}
