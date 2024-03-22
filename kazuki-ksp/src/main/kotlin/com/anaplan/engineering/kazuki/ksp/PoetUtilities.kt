package com.anaplan.engineering.kazuki.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.PropertySpec

internal fun PropertySpec.Builder.lazy(format: String, vararg args: Any?) =
    delegate(
        CodeBlock.builder().beginControlFlow("lazy").add(format, *args).endControlFlow().build()
    )

internal fun KSClassDeclaration.allSuperTypes(): Set<KSTypeReference> =
    superTypes.flatMap {
        val st = it.resolve().declaration
        mutableSetOf(it).apply {
            if (st is KSClassDeclaration) {
                addAll(st.allSuperTypes())
            }
        }
    }.toSet()