@file:OptIn(KspExperimental::class)

package com.anaplan.engineering.kazuki.ksp.type

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.internal.*
import com.anaplan.engineering.kazuki.ksp.*
import com.anaplan.engineering.kazuki.ksp.InbuiltNames.coreInternalPackage
import com.anaplan.engineering.kazuki.ksp.InbuiltNames.corePackage
import com.anaplan.engineering.kazuki.ksp.type.property.PropertyProcessor
import com.anaplan.engineering.kazuki.ksp.type.property.addFunctionProviders
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName

internal fun TypeSpec.Builder.addRecordType(
    interfaceClassDcl: KSClassDeclaration,
    makeable: Boolean,
    typeGenerationContext: TypeGenerationContext,
) {
    // TODO -- fail if class·is·not interface
    val interfaceType = interfaceClassDcl.asType(emptyList())
    val interfaceTypeArguments =
        interfaceClassDcl.typeParameters.map { it.toTypeVariableName(interfaceClassDcl.typeParameters.toTypeParameterResolver()) }
    val interfaceTypeName = if (interfaceTypeArguments.isEmpty()) {
        interfaceClassDcl.toClassName()
    } else {
        interfaceClassDcl.toClassName().parameterizedBy(interfaceTypeArguments)
    }

    val properties = PropertyProcessor(interfaceClassDcl, typeGenerationContext, allowFields = true).process()

    val allTupleComponents = properties.tupleComponents
    val variableTupleComponents = properties.tupleComponents.filter { !it.fixed }
    typeGenerationContext.logger.debug("tuple components: $allTupleComponents")
    if (allTupleComponents.isEmpty()) {
        throw IllegalStateException("Record $interfaceTypeName must have fields")
    }
    val tupleClassName = ClassName(corePackage, "Tuple${allTupleComponents.size}")
    val tupleType = tupleClassName.parameterizedBy(
        allTupleComponents.map { it.typeName }
    )
    val internalTupleClassName = ClassName(coreInternalPackage, "_Tuple${allTupleComponents.size}")
    val internalTupleType = internalTupleClassName.parameterizedBy(
        allTupleComponents.map { it.typeName } + interfaceTypeName
    )
    val erasedTupleType = tupleClassName.parameterizedBy(
        allTupleComponents.map { STAR }
    )
    val interfaceName = interfaceClassDcl.simpleName.asString()
    val implClassName = "${interfaceName}_Rec"
    val implTypeArgs = if (interfaceTypeArguments.isEmpty()) {
        ""
    } else {
        "<" + interfaceTypeArguments.joinToString(", ") + ">"
    }

    // TODO -- confirm all inbuilt var/vals/params don't conflict
    fun findNonRecordName(defaultName: String): String =
        if (defaultName in allTupleComponents.map { it.name }) {
            findNonRecordName("_$defaultName")
        } else {
            defaultName
        }

    val otherParameterName = findNonRecordName("other")

    val implTypeSpec = TypeSpec.classBuilder(implClassName).apply {
        if (interfaceTypeArguments.isNotEmpty()) {
            addTypeVariables(interfaceTypeArguments)
        }
        addModifiers(KModifier.PRIVATE)
        addAnnotation(AnnotationSpec.builder(_Record::class).apply {
            addMember(allTupleComponents.joinToString(", ") { "\"" + it.name + "\"" })
        }.build())
        addSuperinterface(interfaceTypeName)
        addSuperinterface(internalTupleType)
        primaryConstructor(FunSpec.constructorBuilder().apply {
            variableTupleComponents.forEach { tc -> addParameter(tc.name, tc.typeName) }
            addParameter(
                ParameterSpec.builder(enforceInvariantParameterName, Boolean::class).defaultValue("true")
                    .build()
            )
        }.build())

        variableTupleComponents.forEach { tc ->
            addProperty(
                PropertySpec.builder(
                    tc.name,
                    tc.typeName,
                    KModifier.OVERRIDE,
                ).initializer(tc.name)
                    .build()
            )
        }

        addProperty(
            PropertySpec.builder(enforceInvariantParameterName, Boolean::class, KModifier.PRIVATE).initializer(
                enforceInvariantParameterName
            ).build()

        )

        addFunctionProviders(properties.functionProviders, makeable, typeGenerationContext)

        val comparableWith = addComparableWith(interfaceClassDcl, tupleClassName, typeGenerationContext)

        val hashPropertyName = findNonRecordName("hash")
        addProperty(
            PropertySpec.builder(hashPropertyName, Int::class, KModifier.PRIVATE).lazy(
                if (comparableWith.property == null) {
                    "${corePackage}.mk_(${allTupleComponents.joinToString(", ") { "_${it.index}" }}).hashCode()"
                } else {
                    "${comparableWith.property.simpleName.getShortName()}.hashCode()"
                }
            ).build()
        )

        // N.B. all other members must be set before tuple accessors
        allTupleComponents.forEach { tc ->
            addProperty(
                PropertySpec.builder(
                    "_${tc.index}",
                    tc.typeName,
                    KModifier.OVERRIDE
                ).initializer(tc.name)
                    .build()
            )
        }

        // N.B. all members must be set before init block
        val hasInvariantClauses = addInvariantFrom(interfaceClassDcl, typeGenerationContext)

        (1..allTupleComponents.size).forEach { conNary ->
            addFunction(FunSpec.builder(constructFunctionName).apply {
                addModifiers(KModifier.OVERRIDE)
                (1..conNary).forEach {
                    val tc = allTupleComponents[it - 1]
                    addParameter("t${tc.index}", tc.typeName)
                }
                val enforceInvariantParamName = findNonRecordName("enforceInvariant")
                addParameter(enforceInvariantParamName, Boolean::class.asTypeName())
                returns(interfaceTypeName)
                if (makeable) {
                    val fixedTupleComponents = allTupleComponents.take(conNary).filter { it.fixed }
                    val params =
                        (1..conNary)
                            .filter { !allTupleComponents[it - 1].fixed }
                            .map { "t${allTupleComponents[it - 1].index}" } +
                                (conNary + 1..allTupleComponents.size)
                                    .filter { !allTupleComponents[it - 1].fixed }
                                    .map { "_$it" }
                    beginControlFlow("if ($enforceInvariantParamName)")
                    if (hasInvariantClauses || fixedTupleComponents.isNotEmpty()) {
                        val candidateValName = findNonRecordName("candidate")
                        addStatement(
                            "val $candidateValName = %N$implTypeArgs(${params.joinToString(", ")}, false)",
                            implClassName,
                        )
                        val failedClausesValName = findNonRecordName("failedClauses")
                        if (fixedTupleComponents.isEmpty()) {
                            addStatement("val $failedClausesValName = $candidateValName.$failedInvariantClausesVariableName")
                        } else {
                            val clauses = fixedTupleComponents.joinToString(", ") {
                                "${_InvariantClause::class.qualifiedName}(\"${interfaceName}\",·\"${it.name}·==·\${this._${it.index}.prettyOrDefault()}\",·{ t${it.index}·==·this._${it.index} })"
                            }
                            val fixedValueClausesValName = findNonRecordName("fixedValueClauses")
                            addStatement("val $fixedValueClausesValName = listOf($clauses)")
                            val includedInvariantClauses =
                                if (hasInvariantClauses) "$candidateValName.$failedInvariantClausesVariableName + " else ""
                            addStatement("val $failedClausesValName = $includedInvariantClauses$fixedValueClausesValName.filter·{ !it.holds }")
                        }
                        beginControlFlow(
                            "${InbuiltNames.pre}(%P)",
                            "Cannot construct $interfaceName, would fail \${$failedClausesValName.joinToString(\" and \") { it.clauseName }}"
                        )
                        addStatement("$failedClausesValName.isEmpty()")
                        endControlFlow()
                    }
                    endControlFlow()
                    addStatement("return %N(${params.joinToString(", ")}, $enforceInvariantParamName)", implClassName)
                } else {
                    addStatement("throw UnsupportedOperationException()")
                }
            }.build())
        }


        addFunction(
            FunSpec.builder("toString").apply {
                addModifiers(KModifier.OVERRIDE)
                returns(String::class)
                addCode(CodeBlock.builder().apply {
                    val stringBuilderName = findNonRecordName("sb")
                    beginControlFlow("val $stringBuilderName = %T().apply", StringBuilder::class)
                    addStatement("append(%S)", interfaceType.declaration.simpleName.asString())
                    val useComparableForOutput =
                        comparableWith.property?.getAnnotationsByType(ComparableProperty::class)?.single()?.useForOutput
                    if (useComparableForOutput == true) {
                        addStatement("append(%P)", "\$${comparableWith.property.simpleName.asString()}")
                    } else {
                        addStatement("append(%S)", "(")
                        allTupleComponents.dropLast(1).forEach {
                            val propertyName = it.name
                            addStatement("append(%P)", "$propertyName=\$$propertyName, ")
                        }
                        val lastPropertyName = allTupleComponents.last().name
                        addStatement("append(%P)", "$lastPropertyName=\$$lastPropertyName")
                        addStatement("append(%S)", ")")
                    }
                    endControlFlow()
                    addStatement("return $stringBuilderName.toString()")
                }.build())
            }.build()
        )

        if (!interfaceClassDcl.hasSuperType(PrettyPrintable::class.qualifiedName!!)) {
            addFunction(
                FunSpec.builder(PrettyFunctionName).apply {
                    addModifiers(KModifier.OVERRIDE)
                    returns(String::class)
                    addCode(CodeBlock.builder().apply {
                        val stringBuilderName = findNonRecordName("sb")
                        beginControlFlow("val $stringBuilderName = %T().apply", StringBuilder::class)
                        val useComparableForOutput =
                            comparableWith.property?.getAnnotationsByType(ComparableProperty::class)
                                ?.single()?.useForOutput
                        if (useComparableForOutput == true) {
                            addStatement("append(%P)", "\$${comparableWith.property.simpleName.asString()}")
                        } else {
                            addStatement("append(%S)", "(")
                            allTupleComponents.dropLast(1).forEach {
                                val propertyName = it.name
                                val declaration = it.typeReference.resolve().declaration
                                if (declaration is KSTypeParameter) {
                                    beginControlFlow("if (%N is %T)", propertyName, PrettyPrintable::class)
                                    addStatement("append(%P)", "$propertyName=\${$propertyName.pretty()}, ")
                                    nextControlFlow("else")
                                    addStatement("append(%P)", "$propertyName=\$$propertyName, ")
                                    endControlFlow()
                                } else if (declaration is KSClassDeclaration &&
                                    (declaration.isModule || declaration.hasSuperType(PrettyPrintable::class.qualifiedName!!))
                                ) {
                                    if (declaration.isModule) {
                                        addStatement(
                                            "append(%P)",
                                            "$propertyName=\${${declaration.qualifiedModuleName}.$StaticPrettyFunctionName($propertyName)}, "
                                        )
                                    } else {
                                        addStatement("append(%P)", "$propertyName=\${$propertyName.pretty()}, ")
                                    }
                                } else {
                                    addStatement("append(%P)", "$propertyName=\$$propertyName, ")
                                }
                            }
                            val lastPropertyName = allTupleComponents.last().name
                            val declaration = allTupleComponents.last().typeReference.resolve().declaration
                            if (declaration is KSTypeParameter) {
                                beginControlFlow("if (%N is %T)", lastPropertyName, PrettyPrintable::class)
                                addStatement("append(%P)", "$lastPropertyName=\${$lastPropertyName.pretty()}")
                                nextControlFlow("else")
                                addStatement("append(%P)", "$lastPropertyName=\$$lastPropertyName")
                                endControlFlow()
                            } else if (declaration is KSClassDeclaration &&
                                (declaration.isModule || declaration.hasSuperType(PrettyPrintable::class.qualifiedName!!))
                            ) {
                                if (declaration.isModule) {
                                    addStatement(
                                        "append(%P)",
                                        "$lastPropertyName=\${${declaration.qualifiedModuleName}.$StaticPrettyFunctionName($lastPropertyName)}"
                                    )
                                } else {
                                    addStatement("append(%P)", "$lastPropertyName=\${$lastPropertyName.pretty()}")
                                }
                            } else {
                                addStatement("append(%P)", "$lastPropertyName=\$$lastPropertyName")
                            }
                            addStatement("append(%S)", ")")
                        }
                        endControlFlow()
                        addStatement("return $stringBuilderName.toString()")
                    }.build())
                }.build()
            )
        }

        addFunction(
            FunSpec.builder("hashCode").addModifiers(KModifier.OVERRIDE)
                .returns(Int::class).apply {
                    addStatement("return %N", hashPropertyName)
                }.build()
        )

        addFunction(
            FunSpec.builder("equals").addModifiers(KModifier.OVERRIDE)
                .addParameter(
                    ParameterSpec.builder(otherParameterName, Any::class.asTypeName().copy(nullable = true))
                        .build()
                )
                .addAnnotation(uncheckedCastAnnotation())
                .returns(Boolean::class).addCode(CodeBlock.builder().apply {
                    beginControlFlow("if (this === %N)", otherParameterName)
                    addStatement("return true")
                    endControlFlow()

                    beginControlFlow("if (null == %N)", otherParameterName)
                    addStatement("return false")
                    endControlFlow()

                    val erasedInternalTupleType = internalTupleClassName.parameterizedBy(
                        allTupleComponents.map { STAR } + STAR
                    )
                    beginControlFlow(
                        "if (%N !is %T)",
                        otherParameterName,
                        erasedInternalTupleType
                    )
                    addStatement("return false")
                    endControlFlow()

                    beginControlFlow(
                        "if (!(%N.%N.isInstance(this) && this.%N.isInstance(%N)))",
                        otherParameterName,
                        comparableWithPropertyName,
                        comparableWithPropertyName,
                        otherParameterName
                    )
                    addStatement("return false")
                    endControlFlow()

                    if (comparableWith.property == null) {
                        addStatement("return ${allTupleComponents.joinToString(" && ") { "_${it.index} == $otherParameterName._${it.index}" }}")
                    } else {
                        val comparablePropertyName = comparableWith.property.simpleName.getShortName()
                        addStatement(
                            "return this.%N == (%N as %T).%N",
                            comparablePropertyName,
                            otherParameterName,
                            comparableWith.comparableTypeLimitTypeName,
                            comparablePropertyName
                        )
                    }
                }.build()).build()
        )

    }.build()
    addType(implTypeSpec)

    addFunction(FunSpec.builder(asTupleFunctionName).apply {
        if (interfaceTypeArguments.isNotEmpty()) {
            addTypeVariables(interfaceTypeArguments)
        }
        addAnnotation(uncheckedCastAnnotation())
        receiver(interfaceTypeName)
        returns(tupleType)
        addCode(CodeBlock.builder().apply {
            beginControlFlow("if (this·is·%T)", erasedTupleType)
            addStatement("return this as %T", tupleType)
            nextControlFlow("else")
            addStatement(
                "throw %T(%S)",
                PreconditionFailure::class.asClassName(),
                "Cannot convert instance of $interfaceName created outside Kazuki"
            )
            endControlFlow()
        }.build())
    }.build()).build()

    val erasedInterfaceTypeName = if (interfaceTypeArguments.isEmpty()) {
        interfaceClassDcl.toClassName()
    } else {
        interfaceClassDcl.toClassName().parameterizedBy(interfaceTypeArguments.map { STAR })
    }
    addFunction(
        FunSpec.builder("is_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(otherParameterName, Any::class)
            addAnnotation(uncheckedCastAnnotation())
            returns(Boolean::class)
            addCode(CodeBlock.builder().apply {
                beginControlFlow(
                    "if (%N·is·%T)",
                    otherParameterName,
                    erasedInterfaceTypeName
                )
                addStatement("return true")
                endControlFlow()

                val erasedInternalTupleType = internalTupleClassName.parameterizedBy(
                    allTupleComponents.map { STAR } + STAR
                )
                beginControlFlow(
                    "if (%N·!is·%T)",
                    otherParameterName,
                    erasedInternalTupleType
                )
                addStatement("return false")
                endControlFlow()

                allTupleComponents.forEach { tc ->
                    val type = tc.typeReference.resolve()
                    if (type.declaration !is KSTypeParameter) {
                        beginControlFlow(
                            "if (%N._${tc.index}·!is·%T)",
                            otherParameterName,
                            type.starProjection().toTypeName()
                        )
                        addStatement("return false")
                        endControlFlow()
                    }
                }

                val candidateValName = findNonRecordName("candidate")
                addStatement(
                    "val %N = %N$implTypeArgs(${variableTupleComponents.joinToString { "%N.%N as %T" }}, false)",
                    candidateValName,
                    implClassName,
                    *variableTupleComponents.flatMap { listOf(otherParameterName, "_${it.index}", it.typeName) }
                        .toTypedArray()
                )

                allTupleComponents.filter { it.fixed }.forEach { tc ->
                    beginControlFlow(
                        "if (%N._${tc.index} != %N._${tc.index})",
                        otherParameterName,
                        candidateValName,
                    )
                    addStatement("return false")
                    endControlFlow()
                }

                beginControlFlow(
                    "if (!(%N.%N.isInstance(%N) && %N.%N.isInstance(%N)))",
                    otherParameterName,
                    comparableWithPropertyName,
                    candidateValName,
                    candidateValName,
                    comparableWithPropertyName,
                    otherParameterName
                )
                addStatement("return false")
                endControlFlow()

                addStatement("return %N.%N()", candidateValName, validityFunctionName)
            }.build())
        }.build()
    )

    addStaticPrettyFunction(interfaceTypeName, interfaceTypeArguments)
    addFunction(
        FunSpec.builder(PrettyFunctionName).apply {
            receiver(erasedInterfaceTypeName)
            returns(String::class)
            if (interfaceClassDcl.hasSuperType(PrettyPrintable::class.qualifiedName!!)) {
                addStatement("return this.pretty()")
            } else {
                beginControlFlow("if (this is %T)", PrettyPrintable::class)
                addStatement("return this.pretty()")
                nextControlFlow("else")
                addStatement("return this.toString()")
                endControlFlow()
            }
        }.build()
    )

    if (makeable) {
        addFunction(
            FunSpec.builder("fromTuple").apply {
                // TODO -- precondition on fixed
                if (interfaceTypeArguments.isNotEmpty()) {
                    addTypeVariables(interfaceTypeArguments)
                }
                addModifiers(KModifier.PRIVATE)
                addParameter(otherParameterName, tupleType)
                returns(interfaceTypeName)
                addCode(CodeBlock.builder().apply {
                    addStatement(
                        "return %N(${variableTupleComponents.joinToString { "%N.%N" }})",
                        implClassName,
                        *variableTupleComponents.flatMap { listOf(otherParameterName, "_${it.index}") }.toTypedArray()
                    )
                }.build())
            }.build()
        )

        addFunction(
            FunSpec.builder("as_$interfaceName").apply {
                if (interfaceTypeArguments.isNotEmpty()) {
                    addTypeVariables(interfaceTypeArguments)
                }
                addParameter(otherParameterName, Any::class.asClassName())
                returns(interfaceTypeName)
                addAnnotation(uncheckedCastAnnotation())
                addCode(CodeBlock.builder().apply {
                    beginControlFlow("if (%N·is·%T)", otherParameterName, erasedInterfaceTypeName)
                    addStatement("return %N·as·%T", otherParameterName, interfaceTypeName)
                    val typeArgs = if (interfaceTypeArguments.isEmpty()) {
                        ""
                    } else {
                        "<${interfaceTypeArguments.joinToString { "$it" }}>"
                    }
                    nextControlFlow("else if (!is_$interfaceName$typeArgs($otherParameterName))")
                    // TODO -- want to print value of other
                    addStatement(
                        "throw %T(%P)",
                        PreconditionFailure::class.asClassName(),
                        "\$$otherParameterName is not a $interfaceName"
                    )
                    nextControlFlow("else")
                    addStatement("return fromTuple($otherParameterName as %T)", tupleType)
                    endControlFlow()
                }.build())
            }.build()
        )

        addFunction(
            FunSpec.builder("to_$interfaceName").apply {
                if (interfaceTypeArguments.isNotEmpty()) {
                    addTypeVariables(interfaceTypeArguments)
                }
                addParameter(otherParameterName, Any::class.asClassName())
                returns(interfaceTypeName)
                addAnnotation(uncheckedCastAnnotation())
                addCode(CodeBlock.builder().apply {
                    val typeArgs = if (interfaceTypeArguments.isEmpty()) {
                        ""
                    } else {
                        "<${interfaceTypeArguments.joinToString { "$it" }}>"
                    }
                    beginControlFlow(
                        "${InbuiltNames.pre}(%P)",
                        "\$$otherParameterName.${PrettyFunctionName}() is not a $interfaceName"
                    )
                    addStatement(
                        "is_$interfaceName$typeArgs($otherParameterName)",
                    )
                    endControlFlow()
                    addStatement("return fromTuple($otherParameterName as %T)", tupleType)
                }.build())
            }.build()
        )

        allTupleComponents.forEach { tc ->
            addFunction(
                FunSpec.builder("component${tc.index}").apply {
                    if (interfaceTypeArguments.isNotEmpty()) {
                        addTypeVariables(interfaceTypeArguments)
                    }
                    receiver(interfaceTypeName)
                    addModifiers(KModifier.OPERATOR)
                    returns(tc.typeName)
                    addStatement("return this.%N", tc.name)
                }.build()
            )
        }

        addFunction(
            FunSpec.builder("mk_$interfaceName").apply {
                if (interfaceTypeArguments.isNotEmpty()) {
                    addTypeVariables(interfaceTypeArguments)
                }
                variableTupleComponents.forEach { tc -> addParameter(tc.name, tc.typeName) }
                returns(interfaceTypeName)
                addStatement(
                    "return %N(${variableTupleComponents.joinToString { "%N" }})",
                    implTypeSpec,
                    *variableTupleComponents.map { it.name }.toTypedArray()
                )
            }.build()
        )
    }
    // To call transform/conditional transform, one needs a concrete instance therefore they can be provided
    // even for non-makeable types
    addFunction(
        FunSpec.builder(InbuiltNames.transform).apply {
            val t =
                TypeVariableName(findUnusedGenericName(interfaceTypeArguments), bounds = listOf(interfaceTypeName))
            addTypeVariables(interfaceTypeArguments + t)
            receiver(t)
            variableTupleComponents.forEach { tc ->
                addParameter(ParameterSpec.builder(tc.name, tc.typeName).apply {
                    defaultValue("this.%N", tc.name)
                }.build())
            }
            returns(t)
            addAnnotation(uncheckedCastAnnotation())
            addCode(CodeBlock.builder().apply {
                val constructableClassName = ClassName(
                    coreInternalPackage,
                    "_Constructable${allTupleComponents.size}"
                )
                val constructableTypeName =
                    constructableClassName.parameterizedBy(allTupleComponents.map { it.typeName } + t)
                val erasedConstructableTypeName =
                    constructableClassName.parameterizedBy(allTupleComponents.map { STAR } + STAR)

                beginControlFlow(
                    "${InbuiltNames.pre}(%P)",
                    "Cannot set on instance of $interfaceName created outside Kazuki [\${this::class}]"
                )
                addStatement("this·is·%T", erasedConstructableTypeName)
                endControlFlow()

                addStatement(
                    "return (this·as·%T).$constructFunctionName(${allTupleComponents.joinToString { "%N" }}, true)",
                    constructableTypeName,
                    *allTupleComponents.map { it.name }.toTypedArray()
                )
            }.build())
        }.build()
    )
    addFunction(
        FunSpec.builder(InbuiltNames.conditionalTransform).apply {
            val t = TypeVariableName(
                findUnusedGenericName(interfaceTypeArguments),
                bounds = listOf(interfaceTypeName)
            )
            val o = TypeVariableName(findUnusedGenericName(interfaceTypeArguments + t))
            addTypeVariables(interfaceTypeArguments + t + o)
            receiver(t)
            val postTransformType = Function1::class.asTypeName().parameterizedBy(t, o)
            val onSuccessParamName = findNonRecordName("onSuccess")
            val onFailureParamName = findNonRecordName("onFailure")
            variableTupleComponents.forEach { tc ->
                addParameter(ParameterSpec.builder(tc.name, tc.typeName).apply {
                    defaultValue("this.%N", tc.name)
                }.build())
            }
            addParameter(onSuccessParamName, postTransformType)
            addParameter(onFailureParamName, postTransformType)
            returns(o)
            addAnnotation(uncheckedCastAnnotation())
            addCode(CodeBlock.builder().apply {
                val constructableClassName = ClassName(
                    coreInternalPackage,
                    "_Constructable${allTupleComponents.size}"
                )
                val constructableTypeName =
                    constructableClassName.parameterizedBy(allTupleComponents.map { it.typeName } + t)
                val erasedConstructableTypeName =
                    constructableClassName.parameterizedBy(allTupleComponents.map { STAR } + STAR)

                beginControlFlow(
                    "${InbuiltNames.pre}(%P)",
                    "Cannot set on instance of $interfaceName created outside Kazuki [\${this::class}]"
                )
                addStatement("this·is·%T", erasedConstructableTypeName)
                endControlFlow()

                val constructableValName = findNonRecordName("constructable")
                val candidateValName = findNonRecordName("candidate")
                addStatement("val $constructableValName = (this·as·%T)", constructableTypeName)
                addStatement(
                    "val $candidateValName = $constructableValName.$constructFunctionName(${allTupleComponents.joinToString { "%N" }}, false)",
                    *allTupleComponents.map { it.name }.toTypedArray()
                )
                addStatement("require(${candidateValName}·is·%T)", _Constructable::class.asTypeName())
                beginControlFlow("if ($candidateValName.$validityFunctionName())")
                addStatement(
                    "return $onSuccessParamName($constructableValName.$constructFunctionName(${allTupleComponents.joinToString { "%N" }}, true))",
                    *allTupleComponents.map { it.name }.toTypedArray()
                )
                nextControlFlow("else")
                addStatement("return $onFailureParamName(this)")
                endControlFlow()
            }.build())
        }.build()
    )
}


private const val constructFunctionName = "construct"
private const val asTupleFunctionName = "as_Tuple"
