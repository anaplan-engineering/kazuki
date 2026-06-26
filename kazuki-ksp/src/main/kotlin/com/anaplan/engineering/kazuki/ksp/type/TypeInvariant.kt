package com.anaplan.engineering.kazuki.ksp.type

import com.anaplan.engineering.kazuki.core.Invariant
import com.anaplan.engineering.kazuki.core.InvariantFailure
import com.anaplan.engineering.kazuki.core.internal._InvariantClause
import com.anaplan.engineering.kazuki.ksp.lazy
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

internal const val validityFunctionName = "isValid"
internal const val invariantClausesPropertyName = "invariantClauses"
internal const val enforceInvariantParameterName = "enforceInvariant"
internal const val failedInvariantClausesVariableName = "failedInvariantClauses"

internal sealed interface InvariantClause {
    val name: String
    val functionString: String
}

// TODO - optionally get name from annotation?
internal class InvariantFunction(fn: KSFunctionDeclaration) : InvariantClause {
    override val name = fn.simpleName.asString()
    override val functionString = "::${this.name}"
}

internal class InvariantPrimitiveProperty(
    validationFn: KSFunctionDeclaration,
    property: KSPropertyDeclaration
) : InvariantClause {
    override val name = property.simpleName.asString()
    override val functionString = "{ ${validationFn.qualifiedName!!.asString()}(${this.name}) }"
}

internal class FreeformInvariant(override val name: String, override val functionString: String) : InvariantClause

@OptIn(KspExperimental::class)
// TODO properties of collections of primitives with invariants
internal fun TypeSpec.Builder.addInvariantFrom(
    interfaceClassDcl: KSClassDeclaration,
    typeGenerationContext: TypeGenerationContext,
    additionalInvariantParts: List<InvariantClause> = emptyList(),
    validityRequiresOverride: Boolean = true,
): Boolean {
    val invariantClauses = mutableListOf<InvariantClause>().apply {
        interfaceClassDcl.getAllFunctions()
            .filter { it.isAnnotationPresent(Invariant::class) }
            .forEach { add(InvariantFunction(it)) }
        interfaceClassDcl.declarations.filterIsInstance<KSPropertyDeclaration>()
            .map { it to typeGenerationContext.primitiveInvariants[it.type.resolve().declaration.qualifiedName?.asString()] }
            .filter { it.second != null }
            .forEach { add(InvariantPrimitiveProperty(it.second!!, it.first)) }
        addAll(additionalInvariantParts)
    }
    return if (invariantClauses.isEmpty()) {
        addFunction(FunSpec.builder(validityFunctionName).apply {
            addModifiers(KModifier.OVERRIDE)
            returns(Boolean::class)
            addStatement("return true")
        }.build())
        false
    } else {
        val moduleName = interfaceClassDcl.simpleName.asString()
        addProperty(
            PropertySpec.builder(
                invariantClausesPropertyName,
                List::class.parameterizedBy(_InvariantClause::class)
            ).apply {
                addModifiers(KModifier.PRIVATE)
                val clauses =
                    invariantClauses.joinToString(", ") { "${_InvariantClause::class.qualifiedName}(\"${moduleName}\",·\"${it.name}\",·${it.functionString})" }
                initializer("listOf($clauses)")
            }.build()
        )
        addProperty(
            PropertySpec.builder(
                failedInvariantClausesVariableName,
                List::class.parameterizedBy(_InvariantClause::class)
            ).apply {
                addModifiers(KModifier.PRIVATE)
                lazy("$invariantClausesPropertyName.filter·{ !it.holds }")
            }.build()
        )
        addInitializerBlock(CodeBlock.builder().apply {
            beginControlFlow("if ($enforceInvariantParameterName)")
            beginControlFlow("if ($failedInvariantClausesVariableName.isNotEmpty())")
            addStatement("val msg = $failedInvariantClausesVariableName.joinToString(\"·and·\")·{ it.clauseName }")
            addStatement("throw %T(%P)", InvariantFailure::class, "$moduleName invariant failed for \${pretty()} in: \$msg")
            endControlFlow()
            endControlFlow()
        }.build())
        addFunction(FunSpec.builder(validityFunctionName).apply {
            if (validityRequiresOverride) {
                addModifiers(KModifier.OVERRIDE)
            }
            returns(Boolean::class)
            addStatement("return $invariantClausesPropertyName.all·{ it.holds }")
        }.build())
        true
    }
}