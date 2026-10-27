package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.ksp.type.TypeGenerationContext
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo

internal abstract class ConstructProcessor(
    protected val typeGenerationContext: TypeGenerationContext,
    protected  val codeGenerator: CodeGenerator
) {
    abstract fun generateImplementation(clazz: KSClassDeclaration): Boolean

    protected fun writeToFile(
        clazz: KSClassDeclaration,
        className: String,
        typeSpec: TypeSpec
    ) {
        val clazzName =
            ClassName(packageName = clazz.packageName.asString(), clazz.simpleName.asString())
        val imports =
            clazz.declarations.filterIsInstance<KSClassDeclaration>().filter { it.isVisibleForImport() }
                .map { it.simpleName.asString() }.toList()
        FileSpec.builder(clazz.packageName.asString(), className)
            .addImport(clazzName, imports)
            .addImport(InbuiltNames.corePackage, InbuiltNames.prettyOrDefault)
            .addType(typeSpec).build()
            .writeTo(codeGenerator, Dependencies(true, clazz.containingFile!!))
    }
}