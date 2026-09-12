@file:OptIn(KspExperimental::class)

package com.anaplan.engineering.kazuki.ksp.type

import com.anaplan.engineering.kazuki.core.ConstructedBy
import com.anaplan.engineering.kazuki.core.ConstructorStyle
import com.anaplan.engineering.kazuki.core.ImplementedBy
import com.anaplan.engineering.kazuki.ksp.InbuiltNames.corePackage
import com.anaplan.engineering.kazuki.ksp.applyApiModifier
import com.anaplan.engineering.kazuki.ksp.resolveAncestorTypeArguments
import com.anaplan.engineering.kazuki.ksp.stripVariance
import com.anaplan.engineering.kazuki.ksp.type.property.TupleComponent
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver

internal fun canonicalMkIsInternal(
    abstractModule: KSClassDeclaration,
    publicMk: Boolean,
): Boolean {
    val constructedBy = abstractModule.getAnnotationsByType(ConstructedBy::class).singleOrNull()
    return !publicMk ||
        (constructedBy != null && constructedBy.hideCanonical && constructedBy.style != ConstructorStyle.DIRECT)
}

internal fun TypeSpec.Builder.addConstructedByMkFunctions(
    abstractModule: KSClassDeclaration,
    abstractInterfaceName: String,
    abstractInterfaceTypeName: TypeName,
    abstractInterfaceTypeArguments: List<com.squareup.kotlinpoet.TypeVariableName>,
    resolvedImplementedBy: ResolvedImplementedBy,
    apiModifier: KModifier?,
    typeGenerationContext: TypeGenerationContext,
) {
    if (!abstractModule.isAnnotationPresent(ImplementedBy::class)) {
        return
    }
    val constructedBy = abstractModule.getAnnotationsByType(ConstructedBy::class).singleOrNull() ?: return
    if (constructedBy.style == ConstructorStyle.DIRECT) {
        return
    }
    if (constructedBy.adapter.isBlank()) {
        typeGenerationContext.processingState.errors.add(
            "@ConstructedBy on '${abstractModule.simpleName.asString()}' with style ${constructedBy.style} requires adapter"
        )
        return
    }
    val repField = resolveRepField(resolvedImplementedBy, constructedBy.field, abstractModule, typeGenerationContext)
        ?: return
    val elementTypeName = resolveElementTypeName(
        repField,
        resolvedImplementedBy.concreteModule,
        abstractModule,
    )
    val adapterMember = MemberName(corePackage, constructedBy.adapter)
    val canonicalMkName = "mk_$abstractInterfaceName"
    when (constructedBy.style) {
        ConstructorStyle.VARARG_ELEMENTS -> addFunction(
            FunSpec.builder(canonicalMkName).apply {
                applyApiModifier(apiModifier)
                if (abstractInterfaceTypeArguments.isNotEmpty()) {
                    addTypeVariables(abstractInterfaceTypeArguments.map { it.stripVariance() })
                }
                addParameter("elements", elementTypeName, KModifier.VARARG)
                returns(abstractInterfaceTypeName)
                addStatement("return $canonicalMkName(%M(elements))", adapterMember)
            }.build(),
        )
        ConstructorStyle.ITERABLE -> addFunction(
            FunSpec.builder(canonicalMkName).apply {
                applyApiModifier(apiModifier)
                if (abstractInterfaceTypeArguments.isNotEmpty()) {
                    addTypeVariables(abstractInterfaceTypeArguments.map { it.stripVariance() })
                }
                addParameter(
                    "elements",
                    ClassName("kotlin.collections", "Iterable").parameterizedBy(elementTypeName),
                )
                returns(abstractInterfaceTypeName)
                addStatement("return $canonicalMkName(%M(elements))", adapterMember)
            }.build(),
        )
        ConstructorStyle.DIRECT -> Unit
    }
}

private fun resolveRepField(
    resolvedImplementedBy: ResolvedImplementedBy,
    fieldName: String,
    abstractModule: KSClassDeclaration,
    typeGenerationContext: TypeGenerationContext,
): TupleComponent? {
    val components = resolvedImplementedBy.concreteVariableComponents
    if (fieldName.isNotEmpty()) {
        return components.find { it.name == fieldName } ?: run {
            typeGenerationContext.processingState.errors.add(
                "@ConstructedBy field '$fieldName' not found on @ImplementedBy target of '${abstractModule.simpleName.asString()}'"
            )
            null
        }
    }
    if (components.size != 1) {
        typeGenerationContext.processingState.errors.add(
            "@ConstructedBy on '${abstractModule.simpleName.asString()}' requires field when @ImplementedBy target has ${components.size} representation fields"
        )
        return null
    }
    return components.single()
}

private fun resolveElementTypeName(
    repField: TupleComponent,
    concreteModule: KSClassDeclaration,
    abstractModule: KSClassDeclaration,
): TypeName {
    val repType = repField.typeReference.resolve()
    val elementTypeRef = repType.arguments.firstOrNull()?.type
        ?: throw IllegalStateException("Rep field ${repField.name} has no type arguments")
    val declaration = elementTypeRef.resolve().declaration
    return if (declaration is KSTypeParameter) {
        concreteModule.resolveAncestorTypeArguments(abstractModule.qualifiedName!!.asString())
            .getTypeName(declaration)
    } else {
        elementTypeRef.toTypeName(concreteModule.typeParameters.toTypeParameterResolver())
    }
}
