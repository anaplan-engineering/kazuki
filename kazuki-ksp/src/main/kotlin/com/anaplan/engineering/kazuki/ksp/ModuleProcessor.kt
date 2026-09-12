package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.ksp.type.*
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Visibility
import com.squareup.kotlinpoet.TypeSpec

internal class ModuleProcessor(
    typeGenerationContext: TypeGenerationContext,
    codeGenerator: CodeGenerator
) : ConstructProcessor(typeGenerationContext, codeGenerator) {

    override fun generateImplementation(clazz: KSClassDeclaration): Boolean {
        if (clazz.classKind == ClassKind.OBJECT) {
            processModuleObject(clazz)
        } else if (clazz.classKind != ClassKind.INTERFACE) {
            typeGenerationContext.processingState.errors.add("Module ${clazz.qualifiedName} must be an interface or an object")
        } else {
            processModuleClass(clazz)
        }
        return true
    }

    private fun KSClassDeclaration.kazukiType(): KazukiType {
        val superTypeNames = allSuperTypes.map { it.resolve().declaration.qualifiedName?.asString() }
        // TODO -- might be more than one!
        return if (Sequence1::class.qualifiedName in superTypeNames) {
            KazukiType.Sequence1Type
        } else if (Sequence::class.qualifiedName in superTypeNames) {
            KazukiType.SequenceType
        } else if (InjectiveMapping1::class.qualifiedName in superTypeNames) {
            KazukiType.InjectiveMapping1Type
        } else if (InjectiveMapping::class.qualifiedName in superTypeNames) {
            KazukiType.InjectiveMappingType
        } else if (Mapping1::class.qualifiedName in superTypeNames) {
            KazukiType.Mapping1Type
        } else if (Mapping::class.qualifiedName in superTypeNames) {
            KazukiType.MappingType
        } else if (Relation::class.qualifiedName in superTypeNames) {
            KazukiType.RelationType
        } else if (Set1::class.qualifiedName in superTypeNames) {
            KazukiType.Set1Type
        } else if (Set::class.qualifiedName in superTypeNames) {
            KazukiType.SetType
        } else if (classKind == ClassKind.ENUM_CLASS) {
            KazukiType.QuoteType
        } else {
            KazukiType.RecordType
        }
    }


    @OptIn(KspExperimental::class)
    private fun processModuleClass(clazz: KSClassDeclaration) {
        typeGenerationContext.logger.debug("Processing module: ${clazz.qualifiedName!!.asString()}")
        val makeable = clazz.getAnnotationsByType(Module::class).single().makeable
        val apiModifier = clazz.generatedApiModifier()
        val moduleTypeSpec = TypeSpec.objectBuilder(clazz.moduleName).apply {
            applyApiModifier(apiModifier)
            when (clazz.kazukiType()) {
                KazukiType.Sequence1Type -> addSeq1Type(clazz, makeable, typeGenerationContext, apiModifier)
                KazukiType.SequenceType -> addSeqType(clazz, makeable, typeGenerationContext, apiModifier)
                KazukiType.Relation1Type -> addRelation1Type(clazz, makeable, typeGenerationContext, apiModifier)
                KazukiType.RelationType -> addRelationType(clazz, makeable, typeGenerationContext, apiModifier)
                KazukiType.Set1Type -> addSet1Type(clazz, makeable, typeGenerationContext, apiModifier)
                KazukiType.SetType -> addSetType(clazz, makeable, typeGenerationContext, apiModifier)
                KazukiType.QuoteType -> processQuoteType(clazz, makeable, typeGenerationContext, apiModifier)
                KazukiType.RecordType -> addRecordType(clazz, makeable, typeGenerationContext, apiModifier)
                KazukiType.InjectiveMappingType -> addInjectiveMappingType(clazz, makeable, typeGenerationContext, apiModifier)
                KazukiType.InjectiveMapping1Type -> addInjectiveMapping1Type(clazz, makeable, typeGenerationContext, apiModifier)
                KazukiType.MappingType -> addMappingType(clazz, makeable, typeGenerationContext, apiModifier)
                KazukiType.Mapping1Type -> addMapping1Type(clazz, makeable, typeGenerationContext, apiModifier)
            }
        }.build()

        writeToFile(clazz, clazz.moduleName, moduleTypeSpec)
    }

    private fun processModuleObject(clazz: KSClassDeclaration) {
        // TODO - type extension
        val types =
            clazz.declarations.filterIsInstance<KSClassDeclaration>()
                .filter { it.getVisibility() == Visibility.PUBLIC }
                .groupBy { it.kazukiType() }

        val seq1Types = types[KazukiType.Sequence1Type] ?: emptyList()
        val seqTypes = types[KazukiType.SequenceType] ?: emptyList()
        val relation1Types = types[KazukiType.Relation1Type] ?: emptyList()
        val relationTypes = types[KazukiType.RelationType] ?: emptyList()
        val set1Types = types[KazukiType.Set1Type] ?: emptyList()
        val setTypes = types[KazukiType.SetType] ?: emptyList()
        val quoteTypes = types[KazukiType.QuoteType] ?: emptyList()
        val recordTypes = types[KazukiType.RecordType] ?: emptyList()
        val injectiveMappingType = types[KazukiType.InjectiveMappingType] ?: emptyList()
        val injectiveMapping1Type = types[KazukiType.InjectiveMapping1Type] ?: emptyList()
        val mappingType = types[KazukiType.MappingType] ?: emptyList()
        val mapping1Type = types[KazukiType.Mapping1Type] ?: emptyList()

        val moduleClassName = clazz.moduleName
        val moduleTypeSpec = TypeSpec.objectBuilder(moduleClassName).apply {
            seq1Types.forEach { addSeq1Type(it, true, typeGenerationContext, null) }
            seqTypes.forEach { addSeqType(it, true, typeGenerationContext, null) }
            relation1Types.forEach { addRelation1Type(it, true, typeGenerationContext, null) }
            relationTypes.forEach { addRelationType(it, true, typeGenerationContext, null) }
            setTypes.forEach { addSetType(it, true, typeGenerationContext, null) }
            set1Types.forEach { addSet1Type(it, true, typeGenerationContext, null) }
            quoteTypes.forEach { processQuoteType(it, true, typeGenerationContext, null) }
            recordTypes.forEach { addRecordType(it, true, typeGenerationContext, null) }
            injectiveMappingType.forEach { addInjectiveMappingType(it, true, typeGenerationContext, null) }
            injectiveMapping1Type.forEach { addInjectiveMapping1Type(it, true, typeGenerationContext, null) }
            mappingType.forEach { addMappingType(it, true, typeGenerationContext, null) }
            mapping1Type.forEach { addMapping1Type(it, true, typeGenerationContext, null) }
        }.build()

        writeToFile(clazz, moduleClassName, moduleTypeSpec)
    }


}
