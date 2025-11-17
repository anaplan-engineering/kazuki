package com.anaplan.engineering.kazuki.ksp.type.property

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName

// Workaround for `KSType.toClassName()` bailing out on type arguments.
// See https://github.com/square/kotlinpoet/commit/fc64ac36e925d21e4280a7f0c475dc00f8d4a396
internal fun KSType.toClassNameUnsafe(): ClassName {
    return when (val decl = declaration) {
        is KSClassDeclaration -> decl.toClassName()
        is KSTypeAlias -> decl.toClassName()
        is KSTypeParameter -> error("Cannot convert KSTypeParameter to ClassName: '$this'")
        else -> error("Could not compute ClassName for '$this'")
    }.copy(nullable = isMarkedNullable) as ClassName
}
