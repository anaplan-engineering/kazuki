package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.core.FunctionProvider
import com.google.devtools.ksp.KSTypeNotPresentException
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeName

@OptIn(KspExperimental::class)
fun TypeSpec.Builder.addFunctionProviders(
    functionProviderProperties: Sequence<KSPropertyDeclaration>,
    interfaceTypeParameterResolver: TypeParameterResolver
) {
    functionProviderProperties.forEach { property ->
        val functionProvider = property.getAnnotationsByType(FunctionProvider::class).single()
        val providerQualifiedName = try {
            functionProvider.provider
            throw IllegalStateException("Expected to get a KSTypeNotPresentException")
        } catch (e: KSTypeNotPresentException) {
            e.ksType.declaration.qualifiedName!!.asString()
        }
        addProperty(
            PropertySpec.builder(
                property.simpleName.asString(),
                property.type.toTypeName(interfaceTypeParameterResolver),
                KModifier.OVERRIDE
            ).apply {
                initializer(
                    CodeBlock.builder().apply {
                        addStatement("$providerQualifiedName(this)")
                    }.build()
                )
            }.build()
        )
    }
}