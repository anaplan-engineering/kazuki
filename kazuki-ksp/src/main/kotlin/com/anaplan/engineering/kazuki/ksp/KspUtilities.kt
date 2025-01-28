@file:OptIn(KspExperimental::class, KspExperimental::class, KspExperimental::class)
@file:Suppress("UNCHECKED_CAST")

package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.core.Module
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName

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
) = resolveAncestorTypeParameters(ancestorQualifiedClassName).getTypeName(paramIndex)

class AncestorTypeParameters(
    private val typeParameters: List<KSTypeParameter>,
    private val typeDeclarations: List<KSDeclaration>,
    private val indexToTypeName: Map<Int, TypeName>
) {
    private val nameToTypeName by lazy {
        typeParameters.mapIndexed { i, p ->
            p.name.asString() to indexToTypeName[i]
        }.toMap()
    }

    val typeNames by lazy { typeParameters.indices.map { getTypeName(it) } }

    fun getTypeName(index: Int) = indexToTypeName[index]!!

    fun getTypeName(name: String) = nameToTypeName[name]!!

    fun getTypeDeclaration(index: Int) = typeDeclarations[index]

    override fun toString() = nameToTypeName.toString()

    fun getTypeName(typeParam: KSTypeParameter) = indexToTypeName[typeParameters.indexOf(typeParam)]!!

}

internal fun KSClassDeclaration.resolveAncestorTypeParameters(
    ancestorQualifiedClassName: String,
): AncestorTypeParameters {
    var childClassDcl = this
    var path = getSuperTypePathTo(ancestorQualifiedClassName)!!
    var argList: List<Any> = childClassDcl.typeParameters

    while (path.isNotEmpty()) {
        val parentType = path.first()
        val childTypeParams = childClassDcl.typeParameters
        val parentTypeArgs = parentType.element!!.typeArguments

        argList = parentTypeArgs.map { ta ->
            val declaration = ta.type!!.resolve().declaration
            if (declaration is KSTypeParameter) {
                argList[childTypeParams.indexOf(declaration)]
            } else {
                ta
            }
        }

        path = path.drop(1)
        childClassDcl = getClassDeclaration(parentType)
    }

    val childTypeParameters = childClassDcl.typeParameters
    if (childTypeParameters.size != argList.size) {
        throw IllegalStateException("Unexpected mismatch in resolved and unresolved type parameters of $ancestorQualifiedClassName")
    }
    val indexToTypeName = argList.mapIndexed { i, arg ->
        val typeParameterResolver = typeParameters.toTypeParameterResolver()
        val typeName = if (arg is KSTypeParameter) {
            arg.toTypeVariableName(typeParameterResolver)
        } else if (arg is KSTypeArgument) {
            arg.toTypeName(typeParameterResolver)
        } else {
            throw IllegalStateException("Unable to identify parameter $i of ancestor $ancestorQualifiedClassName")
        }
        i to typeName
    }.toMap()

    return AncestorTypeParameters(
        childTypeParameters,
        argList.map { it as? KSTypeParameter ?: (it as KSTypeArgument).type!!.resolve().declaration },
        indexToTypeName
    )
}

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
