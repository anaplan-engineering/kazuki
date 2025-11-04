@file:OptIn(KspExperimental::class, KspExperimental::class, KspExperimental::class)
@file:Suppress("UNCHECKED_CAST")

package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.core.Module
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName

// TODO -- extract KSP utilities to separate project and test independently!

internal val KSClassDeclaration.allSuperTypes
    get(): List<KSTypeReference> {

        fun getAllSuperTypes(reference: KSTypeReference): List<KSTypeReference> {
            val st = reference.resolve().declaration
            return if (st is KSTypeAlias) {
                getAllSuperTypes(st.type)
            } else {
                mutableListOf(reference).apply {
                    if (st is KSClassDeclaration) {
                        addAll(st.allSuperTypes)
                    }
                }
            }
        }

        return superTypes.flatMap { getAllSuperTypes(it) }.toList()
    }

internal fun KSClassDeclaration.hasSuperType(qualifiedClassName: String) =
    allSuperTypes.any { it.resolve().declaration.qualifiedName?.asString() == qualifiedClassName }

internal fun KSClassDeclaration.getSuperTypePathTo(qualifiedClassName: String): List<KSTypeReference>? =
    superTypes.map {
        it.getSuperTypePathTo(qualifiedClassName)
    }.filterNotNull().firstOrNull() ?: throw IllegalStateException("No super type path found to $qualifiedClassName")

internal fun KSTypeReference.getSuperTypePathTo(qualifiedClassName: String): List<KSTypeReference>? {
    val declaration = resolve().declaration
    if (declaration is KSTypeAlias) {
        return declaration.type.getSuperTypePathTo(qualifiedClassName)
    }
    if (declaration !is KSClassDeclaration) {
        return null
    }
    return if (declaration.qualifiedName?.asString() == qualifiedClassName) {
        listOf(this)
    } else {
        declaration.superTypes.map {
            val superPath = it.getSuperTypePathTo(qualifiedClassName)
            if (superPath == null) {
                null
            } else {
                listOf(this) + superPath
            }
        }.filterNotNull().firstOrNull()
    }
}


internal val KSClassDeclaration.superModules
    get() = allSuperTypes.filter {
        it.resolve().declaration.isAnnotationPresent(
            Module::class
        )
    }

internal val KSClassDeclaration.isModule
    get() = isAnnotationPresent(Module::class)

internal val KSClassDeclaration.moduleName
    get() = "${simpleName.asString()}_Module"

internal val KSClassDeclaration.qualifiedModuleName
    get() = "${this.packageName.asString()}.${moduleName}"

internal fun KSClassDeclaration.resolveTypeNameOfAncestorGenericParameter(
    ancestorQualifiedClassName: String,
    paramIndex: Int
) = resolveAncestorTypeArguments(ancestorQualifiedClassName).getTypeName(paramIndex)

class AncestorTypeArguments(
    private val typeParameters: List<KSTypeParameter>,
    val typeArguments: List<TypeName>
) {
    fun getTypeName(index: Int) = typeArguments[index]

    fun getTypeName(typeParam: KSTypeParameter) = typeArguments[typeParameters.indexOf(typeParam)]

    val resolvedTypeParameters by lazy {
        require(typeParameters.size == typeArguments.size)
        (0..<typeParameters.size).associate {
            typeParameters[it] to typeArguments[it]
        }
    }

    override fun toString() = typeArguments.toString()
}


internal fun KSClassDeclaration.resolveAncestorTypeArguments(
    ancestorQualifiedClassName: String,
    logger: KazukiSymbolProcessor.KazukiLogger? = null,
) = AncestorTypeArgumentResolver(logger).resolveTypeArguments(this, ancestorQualifiedClassName)

internal fun findUnusedGenericName(usedTypeVariableNames: List<TypeVariableName>): String {
    val candidates = ('A'..'Z').map { "_$it" }
    val usedNames = usedTypeVariableNames.map { it.name }.toSet()
    return (candidates - usedNames).first()
}

internal fun getClassDeclaration(reference: KSTypeReference) =
    getClassDeclaration(reference.resolve().declaration)

internal fun getClassDeclaration(declaration: KSDeclaration): KSClassDeclaration =
    when (declaration) {
        is KSClassDeclaration -> declaration
        is KSTypeAlias -> getClassDeclaration(declaration.type)
        else -> throw IllegalStateException("Unexpected declaration: $declaration")
    }
