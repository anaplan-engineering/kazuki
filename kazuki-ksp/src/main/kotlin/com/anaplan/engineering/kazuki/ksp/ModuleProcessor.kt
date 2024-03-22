package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.core.Map1
import com.anaplan.engineering.kazuki.core.Sequence
import com.anaplan.engineering.kazuki.core.Sequence1
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
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
        if (clazz.classKind == ClassKind.OBJECT) {
            processModuleObject(clazz)
        } else {
            processModuleClass(clazz)
        }
    }

    private fun KSClassDeclaration.kazukiType(): KazukiType {
        val superTypeNames = allSuperTypes().map { it.resolve().declaration.qualifiedName?.asString() }
        // TODO -- might be more than one!
        return if (Sequence1::class.qualifiedName in superTypeNames) {
            KazukiType.Sequence1Type
        } else if (Sequence::class.qualifiedName in superTypeNames) {
            KazukiType.SequenceType
        } else if (Map1::class.qualifiedName in superTypeNames) {
            KazukiType.Map1Type
        } else if (Map::class.qualifiedName in superTypeNames) {
            KazukiType.MapType
        } else if (classKind == ClassKind.ENUM_CLASS) {
            KazukiType.QuoteType
        } else {
            KazukiType.RecordType
        }
    }

    private fun processModuleClass(clazz: KSClassDeclaration) {

        val moduleClassName = "${clazz.simpleName.asString()}_Module"
        val moduleTypeSpec = TypeSpec.objectBuilder(moduleClassName).apply {
            when (clazz.kazukiType()) {
                KazukiType.Sequence1Type -> addSeq1Type(clazz, processingState)
                KazukiType.SequenceType -> addSeqType(clazz, processingState)
                KazukiType.QuoteType -> processQuoteType(clazz, processingState)
                KazukiType.RecordType -> addRecordType(clazz, processingState)
                KazukiType.MapType -> addMapType(clazz, processingState)
                KazukiType.Map1Type -> addMap1Type(clazz, processingState)
            }
        }.build()

        writeModule(clazz, moduleClassName, "", moduleTypeSpec)
    }

    private fun processModuleObject(clazz: KSClassDeclaration) {
        // TODO - type extension
        val types =
            clazz.declarations.filterIsInstance<KSClassDeclaration>().filter { it.getVisibility() == Visibility.PUBLIC }.groupBy { it.kazukiType() }

        val seq1Types = types[KazukiType.Sequence1Type] ?: emptyList()
        val seqTypes = types[KazukiType.SequenceType] ?: emptyList()
        val quoteTypes = types[KazukiType.QuoteType] ?: emptyList()
        val recordTypes = types[KazukiType.RecordType] ?: emptyList()

        val debug = """
           Debugging: 
           
           seqTypes = ${seqTypes.joinToString(",") { it.simpleName.asString() }}
           seq1Types = ${seq1Types.joinToString(",") { it.simpleName.asString() }}
           quoteTypes = ${quoteTypes.joinToString(",") { it.simpleName.asString() }}
           recordTypes = ${recordTypes.joinToString(",") { it.simpleName.asString() }}
           primInvs = ${processingState.primitiveInvariants}
        """

        val moduleClassName = "${clazz.simpleName.asString()}_Module"
        val moduleTypeSpec = TypeSpec.objectBuilder(moduleClassName).apply {
            seq1Types.forEach { addSeq1Type(it, processingState) }
            seqTypes.forEach { addSeqType(it, processingState) }
            quoteTypes.forEach { processQuoteType(it, processingState) }
            recordTypes.forEach { addRecordType(it, processingState) }
        }.build()

        writeModule(clazz, moduleClassName, debug, moduleTypeSpec)
    }

    private fun writeModule(
        clazz: KSClassDeclaration,
        moduleClassName: String,
        debug: String,
        moduleTypeSpec: TypeSpec
    ) {
        val clazzName =
            ClassName(packageName = clazz.packageName.asString(), clazz.simpleName.asString())
        val imports = clazz.declarations.filterIsInstance<KSClassDeclaration>().filter { it.getVisibility() == Visibility.PUBLIC }.map { it.simpleName.asString() }.toList()
        FileSpec.builder(clazz.packageName.asString(), moduleClassName).addImport(clazzName, imports)
            .addFileComment(debug).addType(moduleTypeSpec).build()
            .writeTo(codeGenerator, Dependencies(true, clazz.containingFile!!))
    }
}