package com.anaplan.engineering.kazuki.ksp.type

import com.anaplan.engineering.kazuki.core.PreconditionFailure
import com.anaplan.engineering.kazuki.core.internal.*
import com.anaplan.engineering.kazuki.ksp.InbuiltNames.corePackage
import com.anaplan.engineering.kazuki.ksp.lazy
import com.anaplan.engineering.kazuki.ksp.type.property.PropertyProcessor
import com.anaplan.engineering.kazuki.ksp.type.property.addFunctionProviders
import com.anaplan.engineering.kazuki.ksp.uncheckedCastAnnotation
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName

enum class FieldMatch {
    Matched,
    Hidden,
    BadIndex
}

@OptIn(KspExperimental::class)
internal fun TypeSpec.Builder.addAbstractionType(
    abstractClassDcl: KSClassDeclaration,
    concreteClassDcl: KSClassDeclaration,
    concreteRecordClassDcl: KSClassDeclaration,
    typeGenerationContext: TypeGenerationContext,
) {
    val abstractType = abstractClassDcl.asType(emptyList())
    val abstractTypeArguments =
        abstractClassDcl.typeParameters.map { it.toTypeVariableName(abstractClassDcl.typeParameters.toTypeParameterResolver()) }
    val abstractTypeName = if (abstractTypeArguments.isEmpty()) {
        abstractClassDcl.toClassName()
    } else {
        abstractClassDcl.toClassName().parameterizedBy(abstractTypeArguments)
    }

    val concreteType = concreteClassDcl.asType(emptyList())
    val concreteTypeArguments =
        concreteClassDcl.typeParameters.map { it.toTypeVariableName(concreteClassDcl.typeParameters.toTypeParameterResolver()) }
    val concreteTypeName = if (concreteTypeArguments.isEmpty()) {
        concreteClassDcl.toClassName()
    } else {
        concreteClassDcl.toClassName().parameterizedBy(concreteTypeArguments)
    }

    val abstractInterfaceName = abstractClassDcl.simpleName.asString()
    val abstractionClassName = "${abstractInterfaceName}_AbsRec"

    val abstractProperties =
        PropertyProcessor(abstractClassDcl, typeGenerationContext, allowFields = true).process()
    val concreteProperties =
        PropertyProcessor(concreteClassDcl, typeGenerationContext, allowFields = true).process()

    val recordAnnotation = concreteRecordClassDcl.getAnnotationsByType(_Record::class).single()
    var i = 1
    val concreteFields = recordAnnotation.fields.map { name ->
        val tc = abstractProperties.tupleComponents.find { it.name == name }
        if (tc == null) {
            name to FieldMatch.Hidden
        } else {
            val match = if (i == tc.index) FieldMatch.Matched else FieldMatch.BadIndex
            i++
            name to match
        }
    }
    val concreteFieldNames = concreteFields.map { it.first }.toSet()
    val hiddenTupleComponents = concreteFields.filter { (_, m) -> m == FieldMatch.Hidden }.map {
        concreteProperties.tupleComponents.single { tc -> tc.name == it.first }
    }

    val badlyIndexed = concreteFields.filter { (_, m) -> m == FieldMatch.BadIndex }
    val badProperties = abstractProperties.tupleComponents.filter { it.name !in concreteFieldNames }
    val fixedTupleComponents = abstractProperties.tupleComponents.filter { it.fixed }

    if (abstractProperties.functionProviders.isNotEmpty()) {
        typeGenerationContext.processingState.errors.add("Abstraction ${abstractClassDcl.qualifiedName?.asString()} may not have function providers")
        return
    }
    if (hiddenTupleComponents.isEmpty()) {
        typeGenerationContext.processingState.errors.add("Abstraction ${abstractClassDcl.qualifiedName?.asString()} does not hide any fields")
        return
    }
    if (fixedTupleComponents.isNotEmpty()) {
        val badPropertyNames = fixedTupleComponents.joinToString(", ") { it.name }
        typeGenerationContext.processingState.errors.add("Abstraction ${abstractClassDcl.qualifiedName?.asString()} may not have dynamic fields: $badPropertyNames")
        return
    }
    if (badProperties.isNotEmpty()) {
        val badPropertyNames = badProperties.joinToString(", ") { it.name }
        typeGenerationContext.processingState.errors.add("Abstraction ${abstractClassDcl.qualifiedName?.asString()} may not have extra fields: $badPropertyNames")
        return
    }
    if (badlyIndexed.isNotEmpty()) {
        val badPropertyNames = badlyIndexed.joinToString(", ") { it.first }
        typeGenerationContext.processingState.errors.add("Abstraction ${abstractClassDcl.qualifiedName?.asString()} must retain order of fields: $badPropertyNames")
        return
    }
    // TODO check match field types!

    val allTupleComponents = abstractProperties.tupleComponents

    val tupleClassName = ClassName(corePackage, "Tuple${allTupleComponents.size}")
    val tupleType = tupleClassName.parameterizedBy(
        allTupleComponents.map { it.typeName }
    )
    val erasedTupleType = tupleClassName.parameterizedBy(
        allTupleComponents.map { STAR }
    )
    val abstractTypeArgs = if (abstractTypeArguments.isEmpty()) {
        ""
    } else {
        "<" + abstractTypeArguments.joinToString(", ") + ">"
    }

    // TODO -- confirm all inbuilt var/vals/params don't conflict
    fun findNonRecordName(defaultName: String): String =
        if (defaultName in allTupleComponents.map { it.name }) {
            findNonRecordName("_$defaultName")
        } else {
            defaultName
        }

    val otherParameterName = findNonRecordName("other")

    val abstractionTypeSpec = TypeSpec.classBuilder(abstractionClassName).apply {
        if (abstractTypeArguments.isNotEmpty()) {
            addTypeVariables(abstractTypeArguments)
        }
        addModifiers(KModifier.PRIVATE)
        addAnnotation(AnnotationSpec.builder(_Record::class).apply {
            addMember(allTupleComponents.joinToString(", ") { "\"" + it.name + "\"" })
        }.build())
        addSuperinterface(abstractTypeName)
        addSuperinterface(concreteTypeName)
        addSuperinterface(tupleType)
        primaryConstructor(FunSpec.constructorBuilder().apply {
            allTupleComponents.forEach { tc -> addParameter(tc.name, tc.typeName) }
            hiddenTupleComponents.forEach { tc -> addParameter(tc.name, tc.typeName) }
            addParameter(
                ParameterSpec.builder(enforceInvariantParameterName, Boolean::class).defaultValue("true")
                    .build()
            )
        }.build())

        allTupleComponents.forEach { tc ->
            addProperty(
                PropertySpec.builder(
                    tc.name,
                    tc.typeName,
                    KModifier.OVERRIDE,
                ).initializer(tc.name)
                    .build()
            )
        }
        hiddenTupleComponents.forEach { tc ->
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

        addFunctionProviders(concreteProperties.functionProviders, false, typeGenerationContext)

        val comparableWith =
            addComparableWith(abstractClassDcl, tupleClassName, typeGenerationContext, requiresOverride = false)

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

        // TODO -- enforce no invariant on abstraction?
        val hasInvariantClauses =
            addInvariantFrom(concreteClassDcl, typeGenerationContext, validityRequiresOverride = false)

        addFunction(
            FunSpec.builder("toString").apply {
                addModifiers(KModifier.OVERRIDE)
                returns(String::class)
                addCode(CodeBlock.builder().apply {
                    val stringBuilderName = findNonRecordName("sb")
                    beginControlFlow("val $stringBuilderName = %T().apply", StringBuilder::class)
                    addStatement("append(%S)", abstractType.declaration.simpleName.asString())
                    addStatement("append(%S)", "(")
                    allTupleComponents.dropLast(1).forEach {
                        val propertyName = it.name
                        addStatement("append(%P)", "$propertyName=\$$propertyName, ")
                    }
                    val lastPropertyName = allTupleComponents.last().name
                    addStatement("append(%P)", "$lastPropertyName=\$$lastPropertyName")
                    addStatement("append(%S)", ")")
                    addStatement("append(%S)", "(hidden: ")
                    hiddenTupleComponents.dropLast(1).forEach {
                        val propertyName = it.name
                        addStatement("append(%P)", "$propertyName=\$$propertyName, ")
                    }
                    val lastHiddenPropertyName = hiddenTupleComponents.last().name
                    addStatement("append(%P)", "$lastHiddenPropertyName=\$$lastHiddenPropertyName")
                    addStatement("append(%S)", ")")
                    endControlFlow()
                    addStatement("return $stringBuilderName.toString()")
                }.build())
            }.build()
        )

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

                    val erasedInternalTupleType = tupleClassName.parameterizedBy(
                        allTupleComponents.map { STAR } + STAR
                    )
                    beginControlFlow(
                        "if (%N !is %T)",
                        otherParameterName,
                        // TODO -- erasedInternalTupleType -- do we need to worry about comparableWith
                        ClassName(abstractClassDcl.packageName.asString(), "${abstractClassDcl.simpleName.asString()}_Abstraction.$abstractionClassName")
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
        // TODO -- default pretty
    }.build()
    addType(abstractionTypeSpec)

    addFunction(FunSpec.builder("to$abstractInterfaceName").apply {
        if (abstractTypeArguments.isNotEmpty()) {
            addTypeVariables(abstractTypeArguments)
        }
        receiver(concreteTypeName)
        returns(abstractTypeName)
        addCode(CodeBlock.builder().apply {
            val tupleComponents = allTupleComponents + hiddenTupleComponents
            addStatement(
                "return %N(${tupleComponents.joinToString { "%N" }})",
                abstractionTypeSpec,
                *tupleComponents.map { it.name }.toTypedArray()
            )
        }.build())
    }.build()).build()

    addFunction(FunSpec.builder("xi$abstractInterfaceName").apply {
        if (abstractTypeArguments.isNotEmpty()) {
            addTypeVariables(abstractTypeArguments)
        }
        receiver(concreteTypeName)
        addParameter(otherParameterName, concreteTypeName)
        returns(Boolean::class)
        addCode(CodeBlock.builder().apply {
            addStatement(
                "return this.to$abstractInterfaceName() == ${otherParameterName}.to$abstractInterfaceName()",
            )
        }.build())
    }.build()).build()
}