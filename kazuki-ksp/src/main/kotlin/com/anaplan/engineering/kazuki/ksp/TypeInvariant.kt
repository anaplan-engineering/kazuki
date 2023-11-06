package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.core.Invariant
import com.anaplan.engineering.kazuki.core.InvariantFailure
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec

@OptIn(KspExperimental::class)
// TODO properties of collections of primitives with invariants
internal fun TypeSpec.Builder.addInvariantFrom(
    interfaceClassDcl: KSClassDeclaration,
    override: Boolean,
    processingState: KazukiSymbolProcessor.ProcessingState,
) {
    val validityFunctionName = "isValid"
    val invariantParts = mutableListOf<String>().apply {
        if (override) {
            add("super.$validityFunctionName()")
        }
        interfaceClassDcl.declarations.filterIsInstance<KSFunctionDeclaration>()
            .filter { it.isAnnotationPresent(Invariant::class) }
            .forEach { add("${it.simpleName.asString()}()") }
        interfaceClassDcl.declarations.filterIsInstance<KSPropertyDeclaration>()
            .map { it to processingState.primitiveInvariants[it.type.resolve().declaration.qualifiedName?.asString()] }
            .filter { it.second != null }
            .forEach { add("${it.second!!.qualifiedName!!.asString()}(${it.first.simpleName.asString()})") }
    }
    if (invariantParts.isNotEmpty()) {
        addInitializerBlock(CodeBlock.builder().apply {
            beginControlFlow("if (!$validityFunctionName())")
            addStatement("throw %T()", InvariantFailure::class)
            endControlFlow()
        }.build())
        addFunction(FunSpec.builder(validityFunctionName).apply {
            addModifiers(KModifier.PROTECTED)
            if (override) {
                addModifiers(KModifier.OVERRIDE)
            }
            returns(Boolean::class)
            addStatement("return ${invariantParts.joinToString(" && ")}")
        }.build())
    }
}