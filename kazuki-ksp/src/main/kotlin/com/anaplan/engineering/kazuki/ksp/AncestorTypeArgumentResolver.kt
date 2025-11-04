package com.anaplan.engineering.kazuki.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName

internal class AncestorTypeArgumentResolver(private val logger: KazukiSymbolProcessor.KazukiLogger? = null) {

    fun resolveTypeArguments(
        classDcl: KSClassDeclaration,
        ancestorQualifiedClassName: String,
    ): AncestorTypeArguments {
        val typeArguments =
            classDcl.typeParameters.map { it.toTypeVariableName(classDcl.typeParameters.toTypeParameterResolver()) }
        val ancestorTypeArguments = classDcl.getSuperTypePathTo(ancestorQualifiedClassName)!!.fold(
            AncestorTypeArguments(classDcl.typeParameters, typeArguments),
            ::resolveTypeArgumentsToParent,
        )
        logger?.debug("Resolved ancestorTypeArguments for $classDcl: $ancestorTypeArguments")
        return ancestorTypeArguments
    }

    private fun resolveTypeArgumentsToParent(
        previous: AncestorTypeArguments,
        typeReference: KSTypeReference,
    ): AncestorTypeArguments {
        val typeArguments = typeReference.element!!.typeArguments
        val classDcl = getClassDeclaration(typeReference)

        val resolvedTypeArguments = typeArguments.map { typeArgument ->
            resolveTypeName(typeArgument, previous.resolvedTypeParameters)
        }
        return AncestorTypeArguments(
            classDcl.typeParameters,
            resolvedTypeArguments,
        )
    }

    private fun resolveTypeName(
        typeArgument: KSTypeArgument,
        resolvedParameters: Map<KSTypeParameter, TypeName>,
    ): TypeName {
        val resolved = typeArgument.type!!.resolve()
        return when (val declaration = resolved.declaration) {
            is KSTypeParameter -> {
                resolvedParameters[declaration]!!
            }

            is KSClassDeclaration, is KSTypeAlias -> {
                val args = resolved.arguments.map { resolveTypeName(it, resolvedParameters) }
                val className = ClassName(declaration.packageName.asString(), declaration.simpleName.asString())
                if (args.isEmpty()) {
                    className
                } else {
                    className.parameterizedBy(args)
                }
            }

            else -> throw IllegalStateException("$declaration/${declaration::class}")
        }
    }

}