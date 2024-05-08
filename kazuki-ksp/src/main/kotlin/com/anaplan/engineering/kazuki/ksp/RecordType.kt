package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.core.FunctionProvider
import com.anaplan.engineering.kazuki.core.Module
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
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

    val superRecords = interfaceClassDcl.allSuperTypes().filter { it.resolve().declaration.isAnnotationPresent(Module::class) }.map { superType ->
        val resolved = superType.resolve()
        val simpleName = resolved.toClassName().simpleName
        val propertyName = simpleName.first().lowercase() + simpleName.drop(1)

        propertyName to resolved
    }

    val properties = interfaceClassDcl.declarations.filterIsInstance<KSPropertyDeclaration>()
    if (properties.any { it.isMutable }) {
        val mutableProperties = properties.filter { it.isMutable }.map { it.simpleName.asString() }.toList()
        processingState.errors.add("Record type $interfaceTypeName may not have mutable properties: $mutableProperties")
    }

    val functionProviderProperties = properties.filter { it.isAnnotationPresent(FunctionProvider::class) }
    val recordProperties =
        (properties - functionProviderProperties).filter { !it.isMutable && it.isAbstract() }.toList()
    val implTypeSpec = TypeSpec.classBuilder("${interfaceClassDcl.simpleName.asString()}_Rec").apply {
        if (interfaceTypeArguments.isNotEmpty()) {
            addTypeVariables(interfaceTypeArguments)
        }
        addModifiers(KModifier.PRIVATE, KModifier.DATA)
        addSuperinterface(interfaceTypeName)
        superRecords.forEach { (propertyName, type) ->
            addSuperinterface(type.toTypeName(interfaceTypeParameterResolver), CodeBlock.of(propertyName))
            addProperty(PropertySpec.builder(
                propertyName,
                type.toTypeName(interfaceTypeParameterResolver)
            ).initializer(propertyName).build())
        }
        primaryConstructor(FunSpec.constructorBuilder().apply {
            superRecords.forEach { (propertyName, type) ->
                addParameter(propertyName, type.toTypeName(interfaceTypeParameterResolver))
            }
            recordProperties.forEach { property ->
                addParameter(property.simpleName.asString(), property.type.toTypeName(interfaceTypeParameterResolver))
            }
        }.build())
        recordProperties.forEach { property ->
            addProperty(
                PropertySpec.builder(
                    property.simpleName.asString(),
                    property.type.toTypeName(interfaceTypeParameterResolver),
                    KModifier.OVERRIDE
                )
                    .initializer(property.simpleName.asString())
                    .build()
            )
        }
        addFunctionProviders(functionProviderProperties, interfaceTypeParameterResolver)

        // N.B. it is important to have properties before init block
        addInvariantFrom(interfaceClassDcl, false, null, processingState)

        addFunction(
            FunSpec.builder("toString").addModifiers(KModifier.OVERRIDE)
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
                }.build()).build()
        )
    }.build()
    addType(implTypeSpec)

    addFunction(
        FunSpec.builder("mk_${interfaceClassDcl.simpleName.asString()}").apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments)
            }
            val parameters = mutableListOf<String>()
            val formatArgs = mutableListOf<Any>()
            formatArgs.add(implTypeSpec)
            superRecords.forEach { (_, type) ->
                val superClassDcl = type.declaration as KSClassDeclaration
                val superProperties = superClassDcl.declarations.filterIsInstance<KSPropertyDeclaration>()
                val superRecordProperties =
                    (superProperties - functionProviderProperties).filter { !it.isMutable && it.isAbstract() }.toList()
                superRecordProperties.forEach { property ->
                    addParameter(property.simpleName.asString(), property.type.toTypeName(interfaceTypeParameterResolver))
                }
                formatArgs.add(superClassDcl.simpleName.asString())
                formatArgs.addAll(superRecordProperties.map { it.simpleName.asString() })
                parameters.add("${superClassDcl.qualifiedName!!.asString()}_Module.mk_%N(${superRecordProperties.joinToString { "%N" }})")
            }
            recordProperties.forEach { property ->
                addParameter(property.simpleName.asString(), property.type.toTypeName(interfaceTypeParameterResolver))
            }
            returns(interfaceTypeName)
            formatArgs.addAll(recordProperties.map { it.simpleName.asString() })
            parameters.addAll(recordProperties.map { "%N" })
            addStatement("return %N(${parameters.joinToString()})", *formatArgs.toTypedArray())
        }.build()
    )

    // MEANINGFUL is/as for records?
}


