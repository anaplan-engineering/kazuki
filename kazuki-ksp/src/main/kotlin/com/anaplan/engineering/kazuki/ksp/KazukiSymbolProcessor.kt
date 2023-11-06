package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.core.*
import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*

class KazukiSymbolProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {

    // TODO - auto generate tests for equals, hashcode, toString? -- maybe do with opt-in property
    private val processingState = ProcessingState()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val allModules =
            resolver.getSymbolsWithAnnotation(Module::class.qualifiedName.orEmpty())
                .filterIsInstance<KSClassDeclaration>().groupBy { it.validate() }

        val primitiveTypeProcessor = PrimitiveTypeProcessor(processingState, environment.codeGenerator)
        initializePrimitiveInvariants(resolver, primitiveTypeProcessor)

        val moduleProcessor = ModuleProcessor(processingState, environment.codeGenerator)
        allModules[true]?.forEach { moduleProcessor.processModule(it) }

        if (processingState.hasErrors()) {
            processingState.errors.forEach {
                environment.logger.error(it)
            }
            return emptyList()
        }
        return allModules[false] ?: emptyList()
    }


    class ProcessingState {
        val primitiveInvariants: MutableMap<String, KSFunctionDeclaration> = mutableMapOf()
        val errors: MutableList<String> = mutableListOf()

        fun hasErrors() = errors.isNotEmpty()
    }

    // TOOD - load from libs
    private fun initializePrimitiveInvariants(resolver: Resolver, primitiveTypeProcessor: PrimitiveTypeProcessor) =
        processingState.primitiveInvariants.putAll(
            resolver.getSymbolsWithAnnotation(PrimitiveInvariant::class.qualifiedName.orEmpty())
                .filterIsInstance<KSFunctionDeclaration>()
                .filter(KSNode::validate).associateBy {
                    val primitiveTypeAlias = primitiveTypeProcessor.processPrimitiveType(it)
                    "${it.packageName.asString()}.${primitiveTypeAlias.name}"
                }
        )



}

class KazukiSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) =
        KazukiSymbolProcessor(environment)

}