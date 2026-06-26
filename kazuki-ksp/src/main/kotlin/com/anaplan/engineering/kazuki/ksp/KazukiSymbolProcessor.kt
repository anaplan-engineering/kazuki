package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.core.Abstraction
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.PrimitiveInvariant
import com.anaplan.engineering.kazuki.ksp.KazukiSymbolProcessor.KazukiLogger
import com.anaplan.engineering.kazuki.ksp.type.PrimitiveTypeProcessor
import com.anaplan.engineering.kazuki.ksp.type.TypeGenerationContext
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.validate
import kotlin.reflect.KClass

@OptIn(KspExperimental::class)
class KazukiSymbolProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {

    // TODO - auto generate tests for equals, hashcode, toString? -- maybe do with opt-in property
    private val processingState = KazukiProcessingState(environment)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val allModules = getAnnotatedClassesByValidity(resolver, Module::class)
        val allAbstractions = getAnnotatedClassesByValidity(resolver, Abstraction::class)

        val typeGenerationContext = TypeGenerationContext(processingState, resolver)
        val primitiveTypeProcessor = PrimitiveTypeProcessor(typeGenerationContext, environment.codeGenerator)
        initializePrimitiveInvariants(resolver, primitiveTypeProcessor)

        val moduleProcessor = ModuleProcessor(typeGenerationContext, environment.codeGenerator)
        val unprocessedModules = allModules[true]?.filter { !moduleProcessor.generateImplementation(it) } ?: emptyList()
        val abstractionProcessor = AbstractionProcessor(typeGenerationContext, environment.codeGenerator)
        val unprocessedAbstractions = allAbstractions[true]?.filter { !abstractionProcessor.generateImplementation(it) } ?: emptyList()

        if (processingState.hasErrors()) {
            processingState.errors.forEach {
                environment.logger.error(it)
            }
            return emptyList()
        }

        return mutableListOf<KSClassDeclaration>().apply {
            addAll(unprocessedModules)
            addAll(unprocessedAbstractions)
            val modules = allModules[false]
            if (modules != null) {
                addAll(modules)
            }
            val abstractions = allAbstractions[false]
            if (abstractions != null) {
                addAll(abstractions)
            }
        }
    }

    private fun getAnnotatedClassesByValidity(
        resolver: Resolver,
        klass: KClass<*>
    ): Map<Boolean, List<KSClassDeclaration>> = resolver.getSymbolsWithAnnotation(klass.qualifiedName.orEmpty())
        .filterIsInstance<KSClassDeclaration>().groupBy { it.validate() }

    class KazukiLogger(private val environment: SymbolProcessorEnvironment): KSPLogger by environment.logger {

        enum class Level {
            DEBUG, INFO, WARN, ERROR
        }

        companion object {
            const val DebugLevelPropertyName = "com.anaplan.engineering.kazuki.compile.debug.level"
        }

        // Enables redirection of Kazuki debug logging so that we can view this info without enabling compiler debug
        // logging more widely
        private val debugRedirect by lazy {
            val property = environment.options[DebugLevelPropertyName] ?: Level.DEBUG.name
            val level = Level.valueOf(property.uppercase())
            environment.logger.info("Kazuki debug level logging will be redirected to $level")
            when (level) {
                Level.DEBUG -> { m: String, s: KSNode? -> logging(m, s) }
                Level.INFO -> { m: String, s: KSNode? -> info(m, s) }
                Level.WARN -> { m: String, s: KSNode? -> warn(m, s) }
                Level.ERROR -> { m: String, s: KSNode? -> error(m, s) }
            }
        }

        fun debug(message: String, symbol: KSNode? = null) = debugRedirect(message, symbol)
    }

    private class KazukiProcessingState(environment: SymbolProcessorEnvironment): ProcessingState {
        override val primitiveInvariants: MutableMap<String, KSFunctionDeclaration> = mutableMapOf()
        override val errors: MutableList<String> = mutableListOf()
        override val logger = KazukiLogger(environment)
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

interface ProcessingState {
    val primitiveInvariants: MutableMap<String, KSFunctionDeclaration>
    val errors: MutableList<String>
    val logger: KazukiLogger

    fun hasErrors() = errors.isNotEmpty()
}

internal const val InvalidInternalStateType = "Internal state should not be a Kazuki-generated object"