package com.anaplan.engineering.kazuki.ksp

import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.isLocal
import com.google.devtools.ksp.isOpen
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName

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

    val properties = interfaceClassDcl.declarations.filterIsInstance<KSPropertyDeclaration>()
    if (properties.any { it.isMutable }) {
        val mutableProperties = properties.filter { it.isMutable }.map { it.simpleName.asString() }.toList()
        processingState.errors.add("Record type $interfaceTypeName may not have mutable properties: $mutableProperties")
    }

    val recordProperties = properties.filter { !it.isMutable && it.isAbstract() }.toList()
    val implTypeSpec = TypeSpec.classBuilder("${interfaceClassDcl.simpleName.asString()}_Rec").apply {
        if (interfaceTypeArguments.isNotEmpty()) {
            addTypeVariables(interfaceTypeArguments)
        }
        addModifiers(KModifier.PRIVATE, KModifier.DATA)
        addSuperinterface(interfaceTypeName)
        primaryConstructor(FunSpec.constructorBuilder().apply {
            recordProperties.forEach { property ->
                addParameter(property.simpleName.asString(), property.type.toTypeName(interfaceTypeParameterResolver))
            }
        }.build())
        recordProperties.forEach { property ->
            addProperty(
                PropertySpec.builder(property.simpleName.asString(), property.type.toTypeName(interfaceTypeParameterResolver), KModifier.OVERRIDE)
                    .initializer(property.simpleName.asString())
                    .build()
            )
        }

        // N.B. it is important to have properties before init block
        addInvariantFrom(interfaceClassDcl, false, null, processingState)

        addFunction(FunSpec.builder("toString").addModifiers(KModifier.OVERRIDE)
            .returns(String::class).addCode(CodeBlock.builder().apply {
                beginControlFlow("val sb = %T().apply", StringBuilder::class)
                addStatement("append(\"%N(\")", interfaceType.declaration.simpleName.asString())
                if (recordProperties.isNotEmpty()) {
                    recordProperties.dropLast(1).forEach {
                        val propertyName = it.simpleName.asString()
                        addStatement("append(\"%N=\$%N, \")", propertyName, propertyName)
                    }
                    val lastPropertyName = recordProperties.last().simpleName.asString()
                    addStatement("append(\"%N=\$%N\")", lastPropertyName, lastPropertyName)
                }
                addStatement("append(\")\")")
                endControlFlow()
                addStatement("return sb.toString()")
            }.build()).build())

    }.build()
    addType(implTypeSpec)

    addFunction(
        FunSpec.builder("mk_${interfaceClassDcl.simpleName.asString()}").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            recordProperties.forEach { property ->
                addParameter(property.simpleName.asString(), property.type.toTypeName(interfaceTypeParameterResolver))
            }
            returns(interfaceTypeName)
            val formatArgs = listOf(implTypeSpec) + recordProperties.map { it.simpleName.asString() }
            addStatement("return %N(${recordProperties.joinToString { "%N" }})", *formatArgs.toTypedArray())
        }.build()
    )

    // MEANINGFUL is/as for records?
}
