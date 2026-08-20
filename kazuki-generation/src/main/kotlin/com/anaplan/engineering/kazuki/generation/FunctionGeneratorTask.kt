package com.anaplan.engineering.kazuki.generation

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@CacheableTask
abstract class FunctionGeneratorTask : DefaultTask() {

    companion object {
        private const val MaxInputCount = 11
    }

    val generationSrcDir: File
        @OutputDirectory
        get() = project.generationSrcDir()

    @TaskAction
    fun apply() {
        FileSpec.builder(RootPackageName, FileName)
            .addFileComment("This file is generated -- do not edit!")
            .apply {
                (0..MaxInputCount).forEach {
                    addNArgFunction(it)
                }
            }
            .build()
            .writeTo(generationSrcDir)
    }
}

private const val FileName = "Functions"
private const val KotlinFunctionPackage = "kotlin"
private const val CommandPropertyName = "command"
private const val PrePropertyName = "pre"
private const val PostPropertyName = "post"
private const val PostCommandPropertyName = "postCommand"
private const val MeasurePropertyName = "measure"
private const val InvocationsPropertyName = "Invocations"
private const val CachePropertyName = "cache"
private const val LogAsPropertyName = "logAs"
private const val InvocationCountPropertyName = "invocationCount"
private const val FunctionIdPropertyName = "functionId"

fun FileSpec.Builder.addNArgFunction(argCount: Int) {
    val className = "VFunction$argCount"
    val inputTypeNames = (1..argCount).map { TypeVariableName("I$it") }
    val outputTypeName = TypeVariableName("O")
    val booleanName = Boolean::class.asClassName()
    val stringName = String::class.asClassName()
    val nullableStringName = stringName.copy(nullable = true)
    val natName = ULong::class.asClassName()
    val atomicIntName = AtomicInteger::class.asClassName()
    val superInterfaceName = ClassName(KotlinFunctionPackage, "Function$argCount")
        .parameterizedBy(inputTypeNames + outputTypeName)
    val preTypeName = ClassName(KotlinFunctionPackage, "Function$argCount")
        .parameterizedBy(inputTypeNames + booleanName)
    val postCommandTypeName = ClassName(KotlinFunctionPackage, "Function${argCount + 1}")
        .parameterizedBy(inputTypeNames + outputTypeName + booleanName)
    val postTypeName = ClassName(KotlinFunctionPackage, "Function${argCount + 1}")
        .parameterizedBy(inputTypeNames + outputTypeName + booleanName)
    val measureTypeName = ClassName(KotlinFunctionPackage, "Function$argCount")
        .parameterizedBy(inputTypeNames + natName).copy(nullable = true)
    val constructor = FunSpec.constructorBuilder().apply {
        addParameter(CommandPropertyName, superInterfaceName)
        addParameter(PrePropertyName, preTypeName)
        addParameter(PostPropertyName, postTypeName)
        addParameter(PostCommandPropertyName, postCommandTypeName)
        addParameter(MeasurePropertyName, measureTypeName)
        addParameter(CachePropertyName, booleanName)
        addParameter(LogAsPropertyName, nullableStringName)
    }.build()

    addType(TypeSpec.classBuilder(className).apply {
        addTypeVariables(inputTypeNames + outputTypeName)
        addSuperinterface(superInterfaceName)
        primaryConstructor(constructor)
        addProperty(
            PropertySpec.builder(CommandPropertyName, superInterfaceName, KModifier.PRIVATE)
                .initializer(CommandPropertyName).build()
        )
        addProperty(
            PropertySpec.builder(PrePropertyName, preTypeName).initializer(PrePropertyName).build()
        )
        addProperty(
            PropertySpec.builder(PostPropertyName, postTypeName).initializer(PostPropertyName)
                .build()
        )
        addProperty(
            PropertySpec.builder(PostCommandPropertyName, postCommandTypeName).initializer(PostCommandPropertyName)
                .addKdoc("It is not always possible or desirable to specify a total post condition, but we may still want to make some assertions about the result of the command on animation. The 'postCommand' function enables the specifier to provide these checks, whilst indicating that they have not intended to provide a total post condition.")
                .build()
        )
        addProperty(
            PropertySpec.builder(MeasurePropertyName, measureTypeName).initializer(MeasurePropertyName)
                .build()
        )
        addProperty(
            PropertySpec.builder(CachePropertyName, booleanName, KModifier.PRIVATE).initializer(CachePropertyName)
                .build()
        )
        addProperty(
            PropertySpec.builder(LogAsPropertyName, nullableStringName, KModifier.PRIVATE)
                .initializer(LogAsPropertyName)
                .build()
        )
        addProperty(
            PropertySpec.builder(InvocationCountPropertyName, atomicIntName, KModifier.PRIVATE)
                .initializer(CodeBlock.builder().apply {
                    addStatement("%T()", atomicIntName)
                }.build()).build()
        )
        addProperty(
            PropertySpec.builder(FunctionIdPropertyName, stringName, KModifier.PRIVATE).build()
        )
        addInitializerBlock(
            CodeBlock.builder().apply {
                addStatement("val frame = StackWalker.getInstance(setOf(StackWalker.Option.SHOW_HIDDEN_FRAMES), 6).walk·{ it.limit(4).reduce·{ _, r -> r }.get() }")
                addStatement(
                    "%N = \"\${frame.className}(\${frame.fileName}:\${frame.lineNumber})\"",
                    FunctionIdPropertyName
                )
                addStatement("%T.createInstance(%N)", EvaluationProfilerName, FunctionIdPropertyName)
            }.build()
        )
        addType(TypeSpec.companionObjectBuilder().apply {
            val invocationsTypeName = ConcurrentHashMap::class.asClassName().parameterizedBy(
                ClassName(RootPackageName, className).parameterizedBy((0..argCount).map { STAR }),
                natName
            )
            addProperty(
                PropertySpec.builder(InvocationsPropertyName, invocationsTypeName)
                    .initializer("%T()", invocationsTypeName)
                    .build()
            )
        }.build())
        addFunction(FunSpec.builder("invoke").apply {
            addModifiers(KModifier.OVERRIDE)
            (1..argCount).forEach {
                addParameter("i$it", TypeVariableName("I$it"))
            }
            returns(outputTypeName)
            addCode(CodeBlock.builder().apply {
                val inputs = (1..argCount).joinToString(", ") { "i$it" }

                val invocationIdVariableName = "invocation"
                addStatement("val %N = %N.andIncrement", invocationIdVariableName, InvocationCountPropertyName)

                val logVariableName = "log"
                addStatement(
                    "val %N = %N != null && %T.isDebugEnabled",
                    logVariableName,
                    LogAsPropertyName,
                    KazukiLogName
                )
                beginControlFlow("if (%N)", logVariableName)
                addStatement(
                    "%T.debug(\"{} [{}] Invoke: {}\", %N, %N, mk_($inputs).pretty())",
                    KazukiLogName,
                    LogAsPropertyName,
                    invocationIdVariableName
                )
                endControlFlow()

                beginControlFlow("if (%N)", CachePropertyName)
                val keyVariableName = "key"
                addStatement(
                    "val %N = %T(this, mk_($inputs))",
                    keyVariableName,
                    CacheKeyName
                )
                beginControlFlow("if (%T.cache.contains(%N))", EvaluationCacheName, keyVariableName)
                val resultVariableName = "result"
                addStatement(
                    "val %N = %T.cache[%N] as %T",
                    resultVariableName,
                    EvaluationCacheName,
                    keyVariableName,
                    outputTypeName
                )
                beginControlFlow("if (%N)", logVariableName)
                addStatement(
                    "%T.debug(\"{} [{}] Cached result: {}\", %N, %N, %N)",
                    KazukiLogName,
                    LogAsPropertyName,
                    invocationIdVariableName,
                    resultVariableName
                )
                endControlFlow()
                addStatement("return %N", resultVariableName)
                endControlFlow()
                endControlFlow()

                val lastMeasureValName = "lastMeasure"
                val currentMeasureValName = "currentMeasure"
                val initialRecursionValName = "initialRecursion"
                val resultValName = "result"

                addComment("TODO -- detect recursion without measure?")
                addStatement("val %N = %N[this]", lastMeasureValName, InvocationsPropertyName)
                addStatement(
                    "val %N = %N != null && %N == null",
                    initialRecursionValName,
                    MeasurePropertyName,
                    lastMeasureValName
                )

                // not thread safe - could include current thread in tracked object -- but doesn't account for MT in command
                beginControlFlow("if (%N != null)", MeasurePropertyName)
                addStatement("val %N = %N.invoke($inputs)", currentMeasureValName, MeasurePropertyName)
                beginControlFlow("if (%N)", logVariableName)
                addStatement(
                    "%T.debug(\"{} [{}] Measure: current={} last={}\", %N, %N, %N, %N)",
                    KazukiLogName,
                    LogAsPropertyName,
                    invocationIdVariableName,
                    currentMeasureValName,
                    lastMeasureValName,
                )
                endControlFlow()
                beginControlFlow(
                    "if (%N != null && %N >= %N)",
                    lastMeasureValName,
                    currentMeasureValName,
                    lastMeasureValName
                )
                addStatement("%N.remove(this)", InvocationsPropertyName)
                addStatement("throw MeasureFailure()")
                endControlFlow()
                addStatement("%N.put(this, %N)", InvocationsPropertyName, currentMeasureValName)
                endControlFlow()

                beginControlFlow("try")
                addStatement(
                    "%T.startInvocation(%N, this, %N)",
                    EvaluationProfilerName,
                    FunctionIdPropertyName,
                    invocationIdVariableName
                )

                addComment("TODO -- validate primitive args and result")
//              val validParams = validatePrimitive(i2) && validatePrimitive(i2)
//              if (!validParams) {
//                  throw InvariantFailure()
//              }

                beginControlFlow("if (!$PrePropertyName($inputs))")
                addStatement("val name = %N ?: %N", LogAsPropertyName, FunctionIdPropertyName)
                addStatement("val msg = \"In \$name\${mk_($inputs).pretty()}\"")
                addStatement("throw PreconditionFailure(msg)")
                endControlFlow()

                addStatement("val $resultValName = $CommandPropertyName($inputs)")

//                val validResult = validatePrimitive(result)
//                if (!validResult) {
//                    throw InvariantFailure()
//                }

                val postInputs = ((1..argCount).map { "i$it" } + resultValName).joinToString(", ")
                beginControlFlow("if (!$PostCommandPropertyName($postInputs))")
                addStatement("val name = %N ?: %N", LogAsPropertyName, FunctionIdPropertyName)
                addStatement("val msg = \"In \$name\${mk_($inputs).pretty()}=\${%N.prettyOrDefault()}\"", resultValName)
                addStatement("throw AnimationConditionFailure(msg)")
                endControlFlow()

                beginControlFlow("if (!$PostPropertyName($postInputs))")
                addStatement("val name = %N ?: %N", LogAsPropertyName, FunctionIdPropertyName)
                addStatement("val msg = \"In \$name\${mk_($inputs).pretty()}=\${%N.prettyOrDefault()}\"", resultValName)
                addStatement("throw PostconditionFailure(msg)")
                endControlFlow()

                beginControlFlow("if (%N)", logVariableName)
                addStatement(
                    "%T.debug(\"{} [{}] Result: {}\", %N, %N, %N)",
                    KazukiLogName,
                    LogAsPropertyName,
                    invocationIdVariableName,
                    resultVariableName
                )
                endControlFlow()

                beginControlFlow("if (%N)", CachePropertyName)
                addStatement(
                    "val %N = %T(this, mk_($inputs))",
                    keyVariableName,
                    CacheKeyName
                )
                addStatement("%T.cache[%N] = %N", EvaluationCacheName, keyVariableName, resultVariableName)
                endControlFlow()

                addStatement("return $resultValName")

                nextControlFlow("finally")
                beginControlFlow("if (%N)", initialRecursionValName)
                addStatement("%N.remove(this)", InvocationsPropertyName)
                endControlFlow()
                addStatement(
                    "%T.endInvocation(%N, this, %N)",
                    EvaluationProfilerName,
                    FunctionIdPropertyName,
                    invocationIdVariableName
                )
                endControlFlow()
            }.build())
        }.build())

        val closureClassName = "VFunction${argCount - 1}"
        val allInputs = (1..argCount).joinToString(", ") { "i$it" }
        (1..argCount).forEach { i ->
            val closedParamName = "i$i"
            val closedTypeVarName = TypeVariableName("I$i")
            val closureTypeName = ClassName(RootPackageName, closureClassName)
                .parameterizedBy((inputTypeNames - closedTypeVarName) + outputTypeName)
            addFunction(FunSpec.builder("close$i").apply {
                val otherInputs = ((1..argCount) - i ).joinToString(", ") { "i$it" }
                val postInputs = if (argCount == 1) "r" else "$otherInputs, r"
                addParameter(ParameterSpec.builder(closedParamName, closedTypeVarName).build())
                addCode("""
                    return $closureClassName(
                    $CommandPropertyName = { $otherInputs -> this.$CommandPropertyName($allInputs) }, 
                    $PrePropertyName = { $otherInputs -> this.$PrePropertyName($allInputs) }, 
                    $PostPropertyName = { $postInputs -> this.$PostPropertyName($allInputs, r) }, 
                    $PostCommandPropertyName = { $postInputs -> this.$PostCommandPropertyName($allInputs, r) }, 
                    $MeasurePropertyName = if (this.$MeasurePropertyName == null) null else { { $otherInputs -> this.$MeasurePropertyName!!($allInputs) } }, 
                    $CachePropertyName = this.$CachePropertyName, 
                    $LogAsPropertyName = this.$LogAsPropertyName
                )""")
                returns(closureTypeName)
            }.build())

            if (i == 1) {
                addFunction(FunSpec.builder("close").apply {
                    addParameter(ParameterSpec.builder(closedParamName, closedTypeVarName).build())
                    addCode("return close1(i$i)")
                    returns(closureTypeName)
                }.build())
            }
        }
    }.build())

    addFunction(FunSpec.builder("function").apply {
        addTypeVariables(inputTypeNames + outputTypeName)
        addParameter(ParameterSpec.builder(CommandPropertyName, superInterfaceName).build())
        addParameter(ParameterSpec.builder(PrePropertyName, preTypeName).apply {
            val inputs = (1..argCount).joinToString(", ") { "_" }
            defaultValue("{ $inputs -> true }")
        }.build())
        addParameter(ParameterSpec.builder(PostPropertyName, postTypeName).apply {
            val inputs = (1..argCount + 1).joinToString(", ") { "_" }
            defaultValue("{ $inputs -> true }")
        }.build())
        addParameter(ParameterSpec.builder(PostCommandPropertyName, postCommandTypeName).apply {
            val inputs = (1..argCount + 1).joinToString(", ") { "_" }
            defaultValue("{ $inputs -> true }")
        }.build())
        addParameter(ParameterSpec.builder(MeasurePropertyName, measureTypeName).apply {
            defaultValue("null")
        }.build())
        addParameter(ParameterSpec.builder(CachePropertyName, booleanName).apply {
            defaultValue("false")
        }.build())
        addParameter(ParameterSpec.builder(LogAsPropertyName, nullableStringName).apply {
            defaultValue("null")
        }.build())
        addCode("return $className($CommandPropertyName, $PrePropertyName, $PostPropertyName, $PostCommandPropertyName, $MeasurePropertyName, $CachePropertyName, $LogAsPropertyName)")
        returns(ClassName(RootPackageName, className).parameterizedBy(inputTypeNames + outputTypeName))
    }.build())
}