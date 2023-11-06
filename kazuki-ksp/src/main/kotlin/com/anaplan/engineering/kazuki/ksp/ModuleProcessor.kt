package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.core.Sequence1
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Visibility
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo

internal class ModuleProcessor(
    private val processingState: KazukiSymbolProcessor.ProcessingState,
    private val codeGenerator: CodeGenerator
) {

    fun processModule(clazz: KSClassDeclaration) {
        // TODO - type extension
        val types =
            clazz.declarations.filterIsInstance<KSClassDeclaration>().filter { it.getVisibility() == Visibility.PUBLIC }
        val seq1Types =
            types.filter { it.superTypes.any { it.resolve().declaration.qualifiedName?.asString() == Sequence1::class.qualifiedName } }
        val quoteTypes = types.filter { it.classKind == ClassKind.ENUM_CLASS }
        val recordTypes = types - seq1Types - quoteTypes

        val debug = """
           Debugging: 
           
           types = ${types.joinToString(",") { it.simpleName.asString() }}
           seq1Types = ${seq1Types.joinToString(",") { it.simpleName.asString() }}
           quoteTypes = ${quoteTypes.joinToString(",") { it.simpleName.asString() }}
           recordTypes = ${recordTypes.joinToString(",") { it.simpleName.asString() }}
           primInvs = ${processingState.primitiveInvariants}
        """

        val moduleClassName = "${clazz.simpleName.asString()}_Module"
        val moduleTypeSpec = TypeSpec.objectBuilder(moduleClassName).apply {
            seq1Types.forEach { addSeq1Type(it, processingState) }
            quoteTypes.forEach { processQuoteType(it, processingState) }
            recordTypes.forEach { addRecordType(it, processingState) }
        }.build()

        val clazzName =
            ClassName(packageName = clazz.packageName.asString(), clazz.simpleName.asString())
        val imports = types.map { it.simpleName.asString() }.toList()
        FileSpec.builder(clazz.packageName.asString(), moduleClassName).addImport(clazzName, imports)
            .addFileComment(debug).addType(moduleTypeSpec).build()
            .writeTo(codeGenerator, Dependencies(true, clazz.containingFile!!))
    }
}