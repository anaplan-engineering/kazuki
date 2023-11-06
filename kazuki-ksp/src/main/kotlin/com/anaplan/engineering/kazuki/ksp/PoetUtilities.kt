package com.anaplan.engineering.kazuki.ksp

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.PropertySpec

internal fun PropertySpec.Builder.lazy(format: String, vararg args: Any?) =
    delegate(
        CodeBlock.builder().beginControlFlow("lazy").add(format, *args).endControlFlow().build()
    )