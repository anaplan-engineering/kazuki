package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.core.Invariant
import com.anaplan.engineering.kazuki.core.InvariantFailure
import com.anaplan.engineering.kazuki.core.Module
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec

internal const val validityFunctionName = "isValid"

@OptIn(KspExperimental::class)
// TODO properties of collections of primitives with invariants
internal fun TypeSpec.Builder.addInvariantFrom(
    interfaceClassDcl: KSClassDeclaration,
    override: Boolean,
    enforceInvariantVariableName: String?,
    processingState: KazukiSymbolProcessor.ProcessingState,
    additionalInvariantParts: List<String> = emptyList(),
) {
    val invariantParts = mutableListOf<String>().apply {
        if (override) {
            add("super.$validityFunctionName()")
        }
        interfaceClassDcl.getAllFunctions()
            .filter { it.isAnnotationPresent(Invariant::class) }
            .forEach { add("${it.simpleName.asString()}()") }
        interfaceClassDcl.declarations.filterIsInstance<KSPropertyDeclaration>()
            .map { it to processingState.primitiveInvariants[it.type.resolve().declaration.qualifiedName?.asString()] }
            .filter { it.second != null }
            .forEach { add("${it.second!!.qualifiedName!!.asString()}(${it.first.simpleName.asString()})") }
        addAll(additionalInvariantParts)
    }
    if (invariantParts.isEmpty()) {
        addFunction(FunSpec.builder(validityFunctionName).apply {
            addModifiers(KModifier.INTERNAL)
            if (override) {
                addModifiers(KModifier.OVERRIDE)
            }
            returns(Boolean::class)
            addStatement("return true")
        }.build())
    } else {
        addInitializerBlock(CodeBlock.builder().apply {
            beginControlFlow(
                if (enforceInvariantVariableName == null) {
                    "if (!$validityFunctionName())"
                } else {
                    "if ($enforceInvariantVariableName && !$validityFunctionName())"
                }
            )
            addStatement("throw %T()", InvariantFailure::class)
            endControlFlow()
        }.build())
        addFunction(FunSpec.builder(validityFunctionName).apply {
            addModifiers(KModifier.INTERNAL)
            if (override) {
                addModifiers(KModifier.OVERRIDE)
            }
            returns(Boolean::class)
            addStatement("return ${invariantParts.joinToString(" && ")}")
        }.build())
    }
}