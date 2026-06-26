package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.core.Abstraction
import com.anaplan.engineering.kazuki.core.FunctionProvider
import com.anaplan.engineering.kazuki.core.property
import com.anaplan.engineering.kazuki.ksp.type.TypeGenerationContext
import com.anaplan.engineering.kazuki.ksp.type.addAbstractionType
import com.google.devtools.ksp.KSTypeNotPresentException
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getKotlinClassByName
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.TypeSpec

internal class AbstractionProcessor(
    typeGenerationContext: TypeGenerationContext,
    codeGenerator: CodeGenerator
) : ConstructProcessor(typeGenerationContext, codeGenerator) {

    override fun generateImplementation(clazz: KSClassDeclaration) =
        if (clazz.classKind != ClassKind.INTERFACE) {
            typeGenerationContext.processingState.errors.add("Abstraction ${clazz.qualifiedName} must be an interface")
            true
        } else {
            processAbstraction(clazz)
        }

    @OptIn(KspExperimental::class)
    private fun processAbstraction(clazz: KSClassDeclaration): Boolean {
        typeGenerationContext.logger.debug("Processing abstraction: ${clazz.qualifiedName!!.asString()}")
        val abstraction = clazz.getAnnotationsByType(Abstraction::class).single()
        val concreteDeclaration = try {
            abstraction.of
            throw IllegalStateException("Expected to get a KSTypeNotPresentException")
        } catch (e: KSTypeNotPresentException) {
            e.ksType.declaration
        }
        val concreteInterfaceName = concreteDeclaration.qualifiedName?.asString()
        val concreteInterfaceClassDcl = typeGenerationContext.resolver.getKotlinClassByName(concreteInterfaceName!!)
        if (concreteInterfaceClassDcl == null) {
            typeGenerationContext.logger.debug("Cannot identify interface for concrete module ${concreteDeclaration.qualifiedName?.asString()} needed for abstraction ${clazz.qualifiedName?.asString()} in current processing round")
            return false
        }
        val concreteClassName = concreteDeclaration.simpleName.asString()
        val concreteRecordName = "${concreteDeclaration.packageName.asString()}.${concreteClassName}_Module.${concreteClassName}_Rec"
        val concreteRecordClassDcl = typeGenerationContext.resolver.getKotlinClassByName(concreteRecordName)
        if (concreteRecordClassDcl == null) {
            typeGenerationContext.logger.debug("Cannot identify record for concrete module ${concreteDeclaration.qualifiedName?.asString()} needed for abstraction ${clazz.qualifiedName?.asString()} in current processing round")
            return false
        }
        val abstractionTypeSpec = TypeSpec.objectBuilder(clazz.abstractionName).apply {
            addAbstractionType(clazz, concreteInterfaceClassDcl,  concreteRecordClassDcl, typeGenerationContext)
        }.build()
        writeToFile(clazz, clazz.abstractionName, abstractionTypeSpec)
        return true
    }
}
