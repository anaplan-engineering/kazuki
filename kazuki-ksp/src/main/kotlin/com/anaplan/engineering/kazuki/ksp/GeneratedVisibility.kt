package com.anaplan.engineering.kazuki.ksp

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.Visibility
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec

/**
 * KotlinPoet visibility for KSP-generated module API surface, derived from the source @Module declaration.
 * Returns null for public (KotlinPoet default).
 */
internal fun KSClassDeclaration.generatedApiModifier(): KModifier? = when (getVisibility()) {
    Visibility.PUBLIC -> null
    Visibility.INTERNAL -> KModifier.INTERNAL
    Visibility.PRIVATE -> KModifier.PRIVATE
    Visibility.PROTECTED -> KModifier.PROTECTED
    else -> null
}

internal fun TypeSpec.Builder.applyApiModifier(modifier: KModifier?): TypeSpec.Builder = apply {
    modifier?.let { addModifiers(it) }
}

internal fun FunSpec.Builder.applyApiModifier(modifier: KModifier?): FunSpec.Builder = apply {
    modifier?.let { addModifiers(it) }
}

internal fun KSDeclaration.isVisibleForImport(): Boolean =
    getVisibility() == Visibility.PUBLIC || getVisibility() == Visibility.INTERNAL
