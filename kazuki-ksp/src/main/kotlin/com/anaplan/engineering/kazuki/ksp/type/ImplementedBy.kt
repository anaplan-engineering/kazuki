@file:OptIn(KspExperimental::class)

package com.anaplan.engineering.kazuki.ksp.type

import com.anaplan.engineering.kazuki.core.ImplementedBy
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.ksp.applyApiModifier
import com.anaplan.engineering.kazuki.ksp.getClassDeclaration
import com.anaplan.engineering.kazuki.ksp.hasSuperType
import com.anaplan.engineering.kazuki.ksp.qualifiedModuleName
import com.anaplan.engineering.kazuki.ksp.stripVariance
import com.anaplan.engineering.kazuki.ksp.resolveAncestorTypeArguments
import com.anaplan.engineering.kazuki.ksp.type.getComparableProperty
import com.anaplan.engineering.kazuki.ksp.type.property.PropertyProcessor
import com.anaplan.engineering.kazuki.ksp.type.property.TupleComponent
import com.anaplan.engineering.kazuki.ksp.type.property.getFunctionProviderProperties
import com.google.devtools.ksp.KSTypeNotPresentException
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Visibility
import com.squareup.kotlinpoet.ksp.toTypeName
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
    if (abstractModule.getVisibility() != Visibility.PUBLIC) {
        typeGenerationContext.processingState.errors.add(
            "ADT interface '${abstractModule.qualifiedName?.asString()}' must be public; ADT @ImplementedBy ${concreteModule.simpleName.asString()} must be internal"
        )
        return null
    }
    if (!concreteModule.isAnnotationPresent(Module::class)) {
        typeGenerationContext.processingState.errors.add(
            "@ImplementedBy target ${concreteModule.qualifiedName?.asString()} must be a @Module"
        )
        return null
    }
    if (!concreteModule.hasSuperType(abstractModule.qualifiedName!!.asString())) {
        typeGenerationContext.processingState.errors.add(
            "@ImplementedBy target ${concreteModule.qualifiedName?.asString()} must extend ${abstractModule.qualifiedName?.asString()}"
        )
        return null
    }
    if (concreteModule.getVisibility() != Visibility.INTERNAL) {
        typeGenerationContext.processingState.errors.add(
            "@ImplementedBy target ${concreteModule.qualifiedName?.asString()} must be internal; ADT implementations must not be publicly constructible"
        )
        return null
    }
    if (!concreteModule.getAnnotationsByType(Module::class).single().makeable) {
        typeGenerationContext.processingState.errors.add(
            "@ImplementedBy target ${concreteModule.qualifiedName?.asString()} must be makeable"
        )
        return null
    }
    val localVariableComponents = localNonFixedTupleComponents(concreteModule, typeGenerationContext)
    if (localVariableComponents.isEmpty()) {
        typeGenerationContext.processingState.errors.add(
            "@ImplementedBy target ${concreteModule.qualifiedName?.asString()} must declare at least one internal representation field on the ADT implementation itself"
        )
        return null
    }
    val concreteProperties = PropertyProcessor(concreteModule, typeGenerationContext, allowFields = true).process()
    val concreteVariableComponents = concreteProperties.tupleComponents.filter { !it.fixed }
    val concreteName = concreteModule.simpleName.asString()
    return ResolvedImplementedBy(
        concreteModule = concreteModule,
        concreteVariableComponents = concreteVariableComponents,
        concreteMkFunctionName = "mk_$concreteName",
        concreteModuleName = concreteModule.qualifiedModuleName,
    )
}

private fun localNonFixedTupleComponents(
    module: KSClassDeclaration,
    typeGenerationContext: TypeGenerationContext,
): List<TupleComponent> {
    val typeParameterResolver = module.typeParameters.toTypeParameterResolver()
    val localProperties = module.declarations.filterIsInstance<KSPropertyDeclaration>()
    val functionProviderProperties = getFunctionProviderProperties(module, typeGenerationContext)
    val comparableProperty = getComparableProperty(module, typeGenerationContext)
    val recordProperties =
        (localProperties - comparableProperty - functionProviderProperties.map { it.property }.toSet()).filterNotNull()
    return recordProperties.mapIndexed { index, property ->
        TupleComponent(
            index + 1,
            property.simpleName.asString(),
            property.type,
            property.type.toTypeName(typeParameterResolver),
            !property.isAbstract(),
        )
    }.filter { !it.fixed }.toList()
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
