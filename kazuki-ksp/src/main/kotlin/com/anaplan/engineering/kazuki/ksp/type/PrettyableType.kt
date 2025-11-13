package com.anaplan.engineering.kazuki.ksp.type

import com.anaplan.engineering.kazuki.core.PrettyPrintable
import com.anaplan.engineering.kazuki.core.internal._KazukiObject
import com.anaplan.engineering.kazuki.ksp.stripVariance
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import kotlin.collections.isNotEmpty

val StaticPrettyFunctionName = "_pretty"


internal fun TypeSpec.Builder.addStaticPrettyFunction(
    interfaceTypeName: TypeName,
    interfaceTypeArguments: List<TypeVariableName>,
) {
    addFunction(
        FunSpec.builder(StaticPrettyFunctionName).apply {
            if (interfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(interfaceTypeArguments.map { it.stripVariance() })
            }
            val parameterName = "obj"
            addParameter(parameterName, interfaceTypeName.copy(nullable = true))
            returns(String::class)
            beginControlFlow("if (%N is %T)", parameterName, PrettyPrintable::class)
            addStatement("return %N.pretty()", parameterName)
            nextControlFlow("else")
            addStatement("return %N.toString()", parameterName)
            endControlFlow()
        }.build()
    )
}