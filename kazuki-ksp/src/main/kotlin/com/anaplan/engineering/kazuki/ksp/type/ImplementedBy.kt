@file:OptIn(KspExperimental::class)

package com.anaplan.engineering.kazuki.ksp.type

import com.anaplan.engineering.kazuki.core.ImplementedBy
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.ksp.applyApiModifier
import com.anaplan.engineering.kazuki.ksp.getClassDeclaration
import com.anaplan.engineering.kazuki.ksp.qualifiedModuleName
import com.anaplan.engineering.kazuki.ksp.stripVariance
import com.anaplan.engineering.kazuki.ksp.resolveAncestorTypeArguments
import com.anaplan.engineering.kazuki.ksp.type.property.PropertyProcessor
import com.anaplan.engineering.kazuki.ksp.type.property.TupleComponent
import com.google.devtools.ksp.KSTypeNotPresentException
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver

internal data class ResolvedImplementedBy(
    val concreteModule: KSClassDeclaration,
    val concreteVariableComponents: List<TupleComponent>,
    val concreteMkFunctionName: String,
    val concreteModuleName: String,
)

@OptIn(KspExperimental::class)
internal fun validateAndResolveImplementedBy(
    abstractModule: KSClassDeclaration,
    makeable: Boolean,
    typeGenerationContext: TypeGenerationContext,
): ResolvedImplementedBy? {
    val implementedBy = abstractModule.getAnnotationsByType(ImplementedBy::class).singleOrNull() ?: return null
    if (makeable) {
        typeGenerationContext.processingState.errors.add(
            "@ImplementedBy on ${abstractModule.qualifiedName?.asString()} requires @Module(makeable = false)"
        )
        return null
    }
    val concreteDeclaration = try {
        implementedBy.value
        throw IllegalStateException("Expected to get a KSTypeNotPresentException")
    } catch (e: KSTypeNotPresentException) {
        e.ksType.declaration
    }
    val concreteModule = getClassDeclaration(concreteDeclaration)
    if (!concreteModule.isAnnotationPresent(Module::class)) {
        typeGenerationContext.processingState.errors.add(
            "@ImplementedBy target ${concreteModule.qualifiedName?.asString()} must be a @Module"
        )
        return null
    }
    if (!concreteModule.getAnnotationsByType(Module::class).single().makeable) {
        typeGenerationContext.processingState.errors.add(
            "@ImplementedBy target ${concreteModule.qualifiedName?.asString()} must be makeable"
        )
        return null
    }
    val concreteProperties = PropertyProcessor(concreteModule, typeGenerationContext, allowFields = true).process()
    val concreteVariableComponents = concreteProperties.tupleComponents.filter { !it.fixed }
    if (concreteVariableComponents.isEmpty()) {
        typeGenerationContext.processingState.errors.add(
            "@ImplementedBy target ${concreteModule.qualifiedName?.asString()} must have at least one non-fixed field"
        )
        return null
    }
    val concreteName = concreteModule.simpleName.asString()
    return ResolvedImplementedBy(
        concreteModule = concreteModule,
        concreteVariableComponents = concreteVariableComponents,
        concreteMkFunctionName = "mk_$concreteName",
        concreteModuleName = concreteModule.qualifiedModuleName,
    )
}

internal fun TypeSpec.Builder.addImplementedByMkFunction(
    abstractModule: KSClassDeclaration,
    abstractInterfaceName: String,
    abstractInterfaceTypeName: TypeName,
    abstractInterfaceTypeArguments: List<com.squareup.kotlinpoet.TypeVariableName>,
    resolvedImplementedBy: ResolvedImplementedBy,
    apiModifier: KModifier?,
) {
    val concreteModule = resolvedImplementedBy.concreteModule
    val ancestorTypeArguments =
        concreteModule.resolveAncestorTypeArguments(abstractModule.qualifiedName!!.asString())
    val mkParameterTypes = resolvedImplementedBy.concreteVariableComponents.map { component ->
        val declaration = component.typeReference.resolve().declaration
        if (declaration is KSTypeParameter) {
            ancestorTypeArguments.getTypeName(declaration)
        } else {
            component.typeName
        }
    }
    addFunction(
        FunSpec.builder("mk_$abstractInterfaceName").apply {
            applyApiModifier(apiModifier)
            if (abstractInterfaceTypeArguments.isNotEmpty()) {
                addTypeVariables(abstractInterfaceTypeArguments.map { it.stripVariance() })
            }
            resolvedImplementedBy.concreteVariableComponents.forEachIndexed { index, component ->
                addParameter(component.name, mkParameterTypes[index])
            }
            returns(abstractInterfaceTypeName)
            val concreteModuleObjectName = "${concreteModule.simpleName.asString()}_Module"
            addStatement(
                "return %M(${resolvedImplementedBy.concreteVariableComponents.joinToString { "%N" }})",
                MemberName(
                    concreteModule.packageName.asString(),
                    "$concreteModuleObjectName.${resolvedImplementedBy.concreteMkFunctionName}",
                ),
                *resolvedImplementedBy.concreteVariableComponents.map { it.name }.toTypedArray(),
            )
        }.build()
    )
}
