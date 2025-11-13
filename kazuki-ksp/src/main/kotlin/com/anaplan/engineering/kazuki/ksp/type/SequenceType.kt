package com.anaplan.engineering.kazuki.ksp.type

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.internal._KSequence
import com.anaplan.engineering.kazuki.core.internal._KazukiObject
import com.anaplan.engineering.kazuki.ksp.InvalidInternalStateType
import com.anaplan.engineering.kazuki.ksp.InbuiltNames
import com.anaplan.engineering.kazuki.ksp.hasSuperType
import com.anaplan.engineering.kazuki.ksp.lazy
import com.anaplan.engineering.kazuki.ksp.resolveAncestorTypeArguments
import com.anaplan.engineering.kazuki.ksp.stripVariance
import com.anaplan.engineering.kazuki.ksp.type.property.PropertyProcessor
import com.anaplan.engineering.kazuki.ksp.type.property.addFunctionProviders
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeVariableName

internal fun TypeSpec.Builder.addSeqType(
    interfaceClassDcl: KSClassDeclaration,
    makeable: Boolean,
    typeGenerationContext: TypeGenerationContext,
) =
    if (makeable) {
        addSequenceType(interfaceClassDcl, typeGenerationContext, false)
    } else {
        // TODO -- is_ / metadata?
    }

internal fun TypeSpec.Builder.addSeq1Type(
    interfaceClassDcl: KSClassDeclaration,
    makeable: Boolean,
    typeGenerationContext: TypeGenerationContext,
) =
    if (makeable) {
        addSequenceType(interfaceClassDcl, typeGenerationContext, true)
    } else {
        // TODO -- is_ / metadata?
    }

@OptIn(KspExperimental::class)
private fun TypeSpec.Builder.addSequenceType(
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
    val superInterface = if (requiresNonEmpty) Sequence1::class else Sequence::class
    val ancestorTypeParameters = interfaceClassDcl.resolveAncestorTypeArguments(superInterface.qualifiedName!!)
    val elementTypeName = ancestorTypeParameters.getTypeName(0)
    val elementsPropertyName = "elements"

    val listTypeName = List::class.asClassName().parameterizedBy(elementTypeName)
    val collectionTypeName = Collection::class.asClassName().parameterizedBy(elementTypeName)
    val suffix = if (requiresNonEmpty) "Seq1" else "Seq"
    val implClassName = "${interfaceName}_$suffix"
    val implTypeSpec = TypeSpec.classBuilder(implClassName).apply {
        if (interfaceTypeArguments.isNotEmpty()) {
            addTypeVariables(interfaceTypeArguments)
        }
        addModifiers(KModifier.PRIVATE)
        addSuperinterface(interfaceTypeName)
        addSuperinterface(_KSequence::class.asClassName().parameterizedBy(elementTypeName, interfaceTypeName))
        addSuperinterface(collectionTypeName, CodeBlock.of(elementsPropertyName))
        addSuperclassConstructorParameter(elementsPropertyName)
        primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(elementsPropertyName, listTypeName)
                .addParameter(
                    ParameterSpec.builder(enforceInvariantParameterName, Boolean::class).defaultValue("true")
                        .build()
                )
                .build()
        )
        addProperty(
            PropertySpec.builder(elementsPropertyName, listTypeName, KModifier.OVERRIDE)
                .initializer(elementsPropertyName).build()
        )
        val lenPropertyName = "len"
        val lenConverter = if (requiresNonEmpty) InbuiltNames.toNat1 else InbuiltNames.toNat
        addProperty(
            PropertySpec.builder(lenPropertyName, nat::class.asTypeName()).addModifiers(KModifier.OVERRIDE)
                .lazy("%N.size.%M()", elementsPropertyName, lenConverter).build()
        )
        val correspondingSetInterface = if (requiresNonEmpty) Set1::class else Set::class
        val correspondingSetConstructor = if (requiresNonEmpty) InbuiltNames.asSet1 else InbuiltNames.asSet
        addProperty(
            PropertySpec.builder(
                "elems",
                correspondingSetInterface.asClassName().parameterizedBy(elementTypeName)
            )
                .addModifiers(
                    KModifier.OVERRIDE
                )
                .lazy("%M(%N)", correspondingSetConstructor, elementsPropertyName).build()
        )
        addProperty(
            PropertySpec.builder(
                "inds",
                correspondingSetInterface.asClassName().parameterizedBy(nat1::class.asClassName())
            ).addModifiers(KModifier.OVERRIDE).lazy("%M(1uL .. len)", correspondingSetConstructor).build()
        )
        val comparableWith = addComparableWith(interfaceClassDcl, Sequence::class.asClassName(), typeGenerationContext)
        addFunctionProviders(properties.functionProviders, true, typeGenerationContext)

        // N.B. it is important to have properties before init block
        // TODO -- should we get this from super interface -- Sequence1.atLeastOneElement()
        val additionalInvariantParts = if (requiresNonEmpty) {
            listOf(FreeformInvariant("atLeastOneElement", "::atLeastOneElement"))
        } else {
            emptyList()
        }
        addInitializerBlock(CodeBlock.builder().apply {
            beginControlFlow(
                "assert (%N !is %T)",
                elementsPropertyName,
                _KazukiObject::class.asTypeName()
            )
            addStatement("%S", InvalidInternalStateType)
            endControlFlow()
        }.build())
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
                    addStatement("it.${InbuiltNames.prettyOrDefault}()")
                    endControlFlow()
                    addStatement("return %P", "<\$elementText>")
                }.build()
            )
        }

        addFunction(
            FunSpec.builder("construct").apply {
                addModifiers(KModifier.OVERRIDE)
                addParameter(elementsPropertyName, listTypeName)
                returns(interfaceTypeName)
                addStatement("return %N(%N)", implClassName, elementsPropertyName)
            }.build()
        )
        addFunction(
            FunSpec.builder("get").apply {
                val indexParameterName = "index"
                addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                addParameter(ParameterSpec.builder(indexParameterName, nat1::class.asTypeName()).build())
                returns(elementTypeName)
                addCode(CodeBlock.builder().apply {
                    beginControlFlow("if (%N < 1u || %N > %N)", indexParameterName, indexParameterName, lenPropertyName)
                    addStatement(
                        "throw %T(%P)",
                        PreconditionFailure::class,
                        "Index \$$indexParameterName is not valid for sequence of length \$$lenPropertyName"
                    )
                    endControlFlow()
                    addStatement(
                        "return %N.get((%N - 1u).%M())", elementsPropertyName, indexParameterName,
                        InbuiltNames.safeToInt
                    )
                }.build())
            }.build()
        )
        addFunction(
            FunSpec.builder("indexOf").apply {
                val elementParameterName = "element"
                addModifiers(KModifier.OVERRIDE)
                addParameter(ParameterSpec.builder(elementParameterName, elementTypeName).build())
                returns(nat1::class.asTypeName())
                addCode(CodeBlock.builder().apply {
                    beginControlFlow("if (%N !in %N)", elementParameterName, elementsPropertyName)
                    addStatement("throw %T()", PreconditionFailure::class)
                    endControlFlow()
                    addStatement(
                        "return %N.indexOf(%N).%M() + 1u", elementsPropertyName, elementParameterName,
                        InbuiltNames.toNat
                    )
                }.build())
            }.build()
        )
        addFunction(
            FunSpec.builder("lastIndexOf").apply {
                val elementParameterName = "element"
                addModifiers(KModifier.OVERRIDE)
                addParameter(ParameterSpec.builder(elementParameterName, elementTypeName).build())
                returns(nat1::class.asTypeName())
                addCode(CodeBlock.builder().apply {
                    beginControlFlow("if (%N !in %N)", elementParameterName, elementsPropertyName)
                    addStatement("throw %T()", PreconditionFailure::class)
                    endControlFlow()
                    addStatement(
                        "return %N.lastIndexOf(%N).%M() + 1u",
                        elementsPropertyName,
                        elementParameterName,
                        InbuiltNames.toNat
                    )
                }.build())
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
                    val hashPropertyName = if (comparableWith.property == null) {
                        elementsPropertyName
                    } else {
                        comparableWith.property.simpleName.getShortName()
                    }
                    addStatement("return %N.hashCode()", hashPropertyName)
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
                        _KSequence::class.asClassName().parameterizedBy(STAR, STAR)
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
                            "return %N == %N.%N",
                            elementsPropertyName,
                            equalsParameterName,
                            elementsPropertyName
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
    addFunction(
        FunSpec.builder("mk_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(elementsPropertyName, elementTypeName, KModifier.VARARG)
            returns(interfaceTypeName)
            addStatement("return %N(%N.toList())", implTypeSpec, elementsPropertyName)
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
            val kSequenceStarType = _KSequence::class.asClassName().parameterizedBy(STAR, STAR)
            val listStarType = List::class.asClassName().parameterizedBy(STAR)
            addParameter(elementsPropertyName, collectionTypeName)
            returns(Boolean::class)
            addStatement(
                "return (%N·is·%T)·||·((%N·is·%T·||·%N·is·%T)·&&·%N$implTypeArgs(%T(%N.size).apply·{ addAll(%N) }, false ).%N())",
                elementsPropertyName,
                erasedInterfaceTypeName,
                elementsPropertyName,
                kSequenceStarType,
                elementsPropertyName,
                listStarType,
                implClassName,
                ArrayList::class.asClassName().parameterizedBy(elementTypeName),
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
            addParameter(elementsPropertyName, collectionTypeName)
            returns(interfaceTypeName)
            addCode(CodeBlock.builder().apply {
                beginControlFlow("if (%N is %T)", elementsPropertyName, erasedInterfaceTypeName)
                // TODO -- make cast optional on whether there is a generic
                addStatement("return %N as %T", elementsPropertyName, interfaceTypeName)
                nextControlFlow("else")
                addStatement(
                    "return %N(%T(%N.size).apply·{ addAll(%N) })",
                    implTypeSpec,
                    ArrayList::class.asClassName().parameterizedBy(elementTypeName),
                    elementsPropertyName,
                    elementsPropertyName
                )
                endControlFlow()
            }.build())
        }.build()
    )
}




