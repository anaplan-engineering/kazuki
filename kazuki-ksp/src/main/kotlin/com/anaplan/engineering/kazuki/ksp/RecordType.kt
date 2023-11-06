package com.anaplan.engineering.kazuki.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName

internal fun TypeSpec.Builder.addRecordType(
    interfaceClassDcl: KSClassDeclaration,
    processingState: KazukiSymbolProcessor.ProcessingState,
) {
    val interfaceType = interfaceClassDcl.asType(emptyList())
    val interfaceTypeName = interfaceType.toTypeName()

    val properties = interfaceClassDcl.declarations.filterIsInstance<KSPropertyDeclaration>()
    if (properties.any { it.isMutable }) {
        val mutableProperties = properties.filter { it.isMutable }.map { it.simpleName.asString() }.toList()
        processingState.errors.add("Record type $interfaceTypeName may not have mutable properties: $mutableProperties")
    }

    val immutableProperties = properties.filter { !it.isMutable }.toList()
    val implTypeSpec = TypeSpec.classBuilder("${interfaceClassDcl.simpleName.asString()}_Rec").apply {
        addModifiers(KModifier.PRIVATE, KModifier.DATA)
        addSuperinterface(interfaceTypeName)
        primaryConstructor(FunSpec.constructorBuilder().apply {
            immutableProperties.forEach { property ->
                addParameter(property.simpleName.asString(), property.type.toTypeName())
            }
        }.build())
        immutableProperties.forEach { property ->
            addProperty(
                PropertySpec.builder(property.simpleName.asString(), property.type.toTypeName(), KModifier.OVERRIDE)
                    .initializer(property.simpleName.asString())
                    .build()
            )
        }

        // N.B. it is important to have properties before init block
        addInvariantFrom(interfaceClassDcl, false, processingState)

        addFunction(FunSpec.builder("toString").addModifiers(KModifier.OVERRIDE)
            .returns(String::class).addCode(CodeBlock.builder().apply {
                beginControlFlow("val sb = %T().apply", StringBuilder::class)
                addStatement("append(\"%N(\")", interfaceType.declaration.simpleName.asString())
                if (immutableProperties.isNotEmpty()) {
                    immutableProperties.dropLast(1).forEach {
                        val propertyName = it.simpleName.asString()
                        addStatement("append(\"%N=\$%N, \")", propertyName, propertyName)
                    }
                    val lastPropertyName = immutableProperties.last().simpleName.asString()
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
            immutableProperties.forEach { property ->
                addParameter(property.simpleName.asString(), property.type.toTypeName())
            }
            returns(interfaceTypeName)
            val formatArgs = listOf(implTypeSpec) + immutableProperties.map { it.simpleName.asString() }
            addStatement("return %N(${immutableProperties.joinToString { "%N" }})", *formatArgs.toTypedArray())
        }.build()
    )
}
