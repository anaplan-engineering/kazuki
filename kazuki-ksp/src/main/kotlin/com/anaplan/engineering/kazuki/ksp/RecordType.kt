package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.core.FunctionProvider
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.PreconditionFailure
import com.anaplan.engineering.kazuki.ksp.InbuiltNames.corePackage
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName

@OptIn(KspExperimental::class)
internal fun TypeSpec.Builder.addRecordType(
    interfaceClassDcl: KSClassDeclaration,
    processingState: KazukiSymbolProcessor.ProcessingState,
) {
    // TODO -- fail if class is not interface
    val interfaceType = interfaceClassDcl.asType(emptyList())
    val interfaceTypeArguments = interfaceClassDcl.typeParameters.map { it.toTypeVariableName() }
    val interfaceTypeName = if (interfaceTypeArguments.isEmpty()) {
        interfaceClassDcl.toClassName()
    } else {
        interfaceClassDcl.toClassName().parameterizedBy(interfaceTypeArguments)
    }
    val interfaceTypeParameterResolver = interfaceClassDcl.typeParameters.toTypeParameterResolver()

    data class TupleComponent(
        val index: Int,
        val name: String,
        val typeReference: KSTypeReference,
        val typeName: TypeName = typeReference.toTypeName(interfaceTypeParameterResolver)
    )

    val superRecords =
        interfaceClassDcl.allSuperTypes().filter { it.resolve().declaration.isAnnotationPresent(Module::class) }

    val properties = interfaceClassDcl.declarations.filterIsInstance<KSPropertyDeclaration>()
    if (properties.any { it.isMutable }) {
        val mutableProperties = properties.filter { it.isMutable }.map { it.simpleName.asString() }.toList()
        processingState.errors.add("Record type $interfaceTypeName may not have mutable properties: $mutableProperties")
    }

    val functionProviderProperties = properties.filter { it.isAnnotationPresent(FunctionProvider::class) }
    val recordProperties =
        (properties - functionProviderProperties).filter { !it.isMutable && it.isAbstract() }.toList()

    val debug = mutableListOf<String>()
    var index = 1
    val allInterfaceProperties = interfaceClassDcl.getAllProperties().toList()
    val tupleComponents = superRecords.flatMap { type ->
        val superClassDcl = type.resolve().declaration as KSClassDeclaration
        val superProperties = superClassDcl.declarations.filterIsInstance<KSPropertyDeclaration>()
        val superRecordProperties =
            (superProperties - functionProviderProperties).filter { !it.isMutable && it.isAbstract() }.toList()
        superRecordProperties.map { superProperty -> allInterfaceProperties.find { interfaceProperty -> superProperty.simpleName == interfaceProperty.simpleName }!! }
            .map { property ->
                property.type.resolve()
                TupleComponent(
                    index++,
                    property.simpleName.asString(),
                    property.type
                )
            }
    } + recordProperties.map { property ->
        TupleComponent(
            index++,
            property.simpleName.asString(),
            property.type
        )
    }
    if (tupleComponents.isEmpty()) {
        throw IllegalStateException("Cannot have empty record")
    }
    val tupleType = ClassName(corePackage, "Tuple${tupleComponents.size}").parameterizedBy(
        tupleComponents.map { it.typeName }
    )
    val erasedTupleType = ClassName(corePackage, "Tuple${tupleComponents.size}").parameterizedBy(
        tupleComponents.map { STAR }
    )
    val compatibleSuperTypes =
        (superRecords.map { it.resolve().starProjection().toTypeName() } + interfaceType.starProjection().toTypeName())

    val interfaceName = interfaceClassDcl.simpleName.asString()
    val implClassName = "${interfaceName}_Rec"
    val implTypeSpec = TypeSpec.classBuilder(implClassName).apply {
        if (interfaceTypeArguments.isNotEmpty()) {
            addTypeVariables(interfaceTypeArguments)
        }
        addModifiers(KModifier.PRIVATE, KModifier.DATA)
        addSuperinterface(interfaceTypeName)
        addSuperinterface(tupleType)
        primaryConstructor(FunSpec.constructorBuilder().apply {
            debug.forEach {
                addComment(it)
            }
            tupleComponents.forEach { tc -> addParameter(tc.name, tc.typeName) }
            addParameter(
                ParameterSpec.builder(enforceInvariantParameterName, Boolean::class).defaultValue("true")
                    .build()
            )
        }.build())

        tupleComponents.forEach { tc ->
            addProperty(
                PropertySpec.builder(
                    tc.name,
                    tc.typeName,
                    KModifier.OVERRIDE,
                ).initializer(tc.name)
                    .build()
            )
            addProperty(
                PropertySpec.builder(
                    "_${tc.index}",
                    tc.typeName,
                    KModifier.OVERRIDE
                ).initializer(tc.name)
                    .build()
            )
        }
        addProperty(
            PropertySpec.builder(enforceInvariantParameterName, Boolean::class, KModifier.PRIVATE).initializer(
                enforceInvariantParameterName
            ).build()
        )
        addFunctionProviders(functionProviderProperties, interfaceTypeParameterResolver)

        // N.B. it is important to have properties before init block
        addInvariantFrom(interfaceClassDcl, false, enforceInvariantParameterName, processingState)

        addFunction(
            FunSpec.builder("toString").addModifiers(KModifier.OVERRIDE)
                .returns(String::class).addCode(CodeBlock.builder().apply {
                    beginControlFlow("val sb = %T().apply", StringBuilder::class)
                    addStatement("append(\"%N(\")", interfaceType.declaration.simpleName.asString())
                    tupleComponents.dropLast(1).forEach {
                        val propertyName = it.name
                        addStatement("append(\"%N=\$%N, \")", propertyName, propertyName)
                    }
                    val lastPropertyName = tupleComponents.last().name
                    addStatement("append(\"%N=\$%N\")", lastPropertyName, lastPropertyName)
                    addStatement("append(\")\")")
                    endControlFlow()
                    addStatement("return sb.toString()")
                }.build()).build()
        )

        addFunction(
            FunSpec.builder("equals").addModifiers(KModifier.OVERRIDE)
                .addParameter(
                    ParameterSpec.builder(otherParameterName, Any::class.asTypeName().copy(nullable = true))
                        .build()
                )
                .returns(Boolean::class).addCode(CodeBlock.builder().apply {
                    beginControlFlow("if (this === %N)", otherParameterName)
                    addStatement("return true")
                    endControlFlow()

                    beginControlFlow("if (null == %N)", otherParameterName)
                    addStatement("return false")
                    endControlFlow()

                    // Tuple type check repeated for smart-cast
                    beginControlFlow(
                        "if (%N is %T && $implClassName.$isRelatedFunctionName($otherParameterName))",
                        otherParameterName,
                        erasedTupleType
                    )
                    addStatement("return ${tupleComponents.joinToString(" && ") { "_${it.index} == $otherParameterName._${it.index}" }}")
                    endControlFlow()

                    addStatement("return false")

                }.build()).build()
        )
        
        addType(TypeSpec.companionObjectBuilder().apply {
            addFunction(
                FunSpec.builder(isRelatedFunctionName).apply {
                    addParameter(otherParameterName, Any::class)
                    returns(Boolean::class)
                    addModifiers(KModifier.INTERNAL)
                    addStatement(
                        "return ${compatibleSuperTypes.joinToString(" || ") { "%N is %T" }}",
                        *compatibleSuperTypes.flatMap { listOf(otherParameterName, it) }.toTypedArray()
                    )
                }.build()
            )
        }.build())

    }.build()
    addType(implTypeSpec)

    addFunction(FunSpec.builder("as_Tuple").apply {
        if (interfaceTypeArguments.isNotEmpty()) {
            addTypeVariables(interfaceTypeArguments)
        }
        receiver(interfaceTypeName)
        returns(tupleType)
        addCode(CodeBlock.builder().apply {
            beginControlFlow("if (this is %T)", erasedTupleType)
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

    addFunction(
        FunSpec.builder("is_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addParameter(otherParameterName, Any::class)
            returns(Boolean::class)
            addCode(CodeBlock.builder().apply {
                beginControlFlow("if (%N !is %T)", otherParameterName, erasedTupleType)
                addStatement("return false")
                endControlFlow()

                beginControlFlow("if (!$implClassName.$isRelatedFunctionName($otherParameterName))")
                addStatement("return false")
                endControlFlow()

                tupleComponents.forEach { tc ->
                    val type = tc.typeReference.resolve()
                    if (type.declaration !is KSTypeParameter) {
                        beginControlFlow(
                            "if (%N._${tc.index} !is %T)",
                            otherParameterName,
                            type.starProjection().toTypeName()
                        )
                        addStatement("return false")
                        endControlFlow()
                    }
                }

                addStatement(
                    "return %N(${tupleComponents.joinToString { "%N.%N as %T" }}, false).%N()",
                    implClassName,
                    *tupleComponents.flatMap { listOf(otherParameterName, "_${it.index}", it.typeName) }.toTypedArray(),
                    validityFunctionName
                )
            }.build())
        }.build()
    )

    addFunction(
        FunSpec.builder("as_$interfaceName").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            addModifiers(KModifier.PRIVATE)
            addParameter(otherParameterName, tupleType)
            returns(interfaceTypeName)
            addCode(CodeBlock.builder().apply {
                addStatement(
                    "return %N(${tupleComponents.joinToString { "%N.%N" }})",
                    implClassName,
                    *tupleComponents.flatMap { listOf(otherParameterName, "_${it.index}") }.toTypedArray()
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
            addCode(CodeBlock.builder().apply {
                val typeArgs = if (interfaceTypeArguments.isEmpty()) {
                    ""
                } else {
                    "<${interfaceTypeArguments.joinToString { "$it" }}>"
                }
                beginControlFlow("if (!is_$interfaceName$typeArgs($otherParameterName))")
                // TODO -- want to print value of other
                addStatement(
                    "throw %T(%S)",
                    PreconditionFailure::class.asClassName(),
                    "${otherParameterName} is not a $interfaceName"
                )
                nextControlFlow("else")
                addStatement("return as_$interfaceName($otherParameterName as %T)", tupleType)
                endControlFlow()
            }.build())
        }.build()
    )

    tupleComponents.forEach { tc ->
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
            tupleComponents.forEach { tc -> addParameter(tc.name, tc.typeName) }
            returns(interfaceTypeName)
            addStatement(
                "return %N(${tupleComponents.joinToString { "%N" }})",
                implTypeSpec,
                *tupleComponents.map { it.name }.toTypedArray()
            )
        }.build()
    )

}

private const val otherParameterName = "other"
private const val isRelatedFunctionName = "isRelated"
private const val enforceInvariantParameterName = "enforceInvariant"

