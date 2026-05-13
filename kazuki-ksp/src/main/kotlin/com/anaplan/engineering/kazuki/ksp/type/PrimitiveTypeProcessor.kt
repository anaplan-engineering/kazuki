package com.anaplan.engineering.kazuki.ksp.type

import com.anaplan.engineering.kazuki.core.PrimitiveInvariant
import com.google.devtools.ksp.KSTypeNotPresentException
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.ksp.writeTo

class PrimitiveTypeProcessor(
    private val typeGenerationContext: TypeGenerationContext,
    private val codeGenerator: CodeGenerator,
) {
    @OptIn(KspExperimental::class)
    // TODO - verify that invariant returns boolean
    internal fun processPrimitiveType(invariant: KSFunctionDeclaration): TypeAliasSpec {
        val type = invariant.getAnnotationsByType(PrimitiveInvariant::class).single()
        if (invariant.returnType?.resolve()?.declaration?.qualifiedName?.asString() != Boolean::class.qualifiedName) {
            typeGenerationContext.errors.add("Primitive invariant ${invariant.qualifiedName?.asString()} must return Boolean")
        }
        val baseQualifiedName = try {
            type.base.qualifiedName!!
        } catch (e: KSTypeNotPresentException) {
            // KSP 2 preserves type aliases in the symbol model rather than resolving them to
            // the underlying class as KSP 1 did. Follow the alias chain until we reach the
            // actual class so that names like "com.anaplan.engineering.kazuki.core.nat" are
            // unwrapped to "kotlin.ULong" before the when-match below.
            var declaration = e.ksType.declaration
            while (declaration is KSTypeAlias) {
                declaration = declaration.type.resolve().declaration
            }
            declaration.qualifiedName!!.asString()
        }
        val base = when (baseQualifiedName) {
            Int::class.qualifiedName -> Int::class
            UInt::class.qualifiedName -> UInt::class
            Long::class.qualifiedName -> Long::class
            ULong::class.qualifiedName -> ULong::class
            else -> throw IllegalArgumentException("Non-primitive type in primitive invariant ${type.base.qualifiedName}")
        }
        val typeAliasSpec = TypeAliasSpec.builder(type.name, base).build()
        FileSpec.builder(invariant.packageName.asString(), type.name).addTypeAlias(typeAliasSpec).build()
            .writeTo(codeGenerator, Dependencies(true))
        return typeAliasSpec
    }
}