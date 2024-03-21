package com.anaplan.engineering.kazuki.generation

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
abstract class FunctionGeneratorTask : DefaultTask() {

    companion object {
        private const val MaxInputCount = 10
    }

    val generationSrcDir: File
        @OutputDirectory
        get() = project.generationSrcDir()

    @TaskAction
    fun apply() {
        FileSpec.builder(PackageName, FileName)
            .addFileComment("This file is generated -- do not edit!")
            .apply {
                (1..MaxInputCount).forEach {
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

fun FileSpec.Builder.addNArgFunction(argCount: Int) {
    val className = "VFunction$argCount"
    val inputTypeNames = (1..argCount).map { TypeVariableName("I$it") }
    val outputTypeName = TypeVariableName("O")
    val booleanName = Boolean::class.asClassName()
    val superInterfaceName = ClassName(KotlinFunctionPackage, "Function$argCount")
        .parameterizedBy(inputTypeNames + outputTypeName)
    val preTypeName = ClassName(KotlinFunctionPackage, "Function$argCount")
        .parameterizedBy(inputTypeNames + booleanName)
    val postTypeName = ClassName(KotlinFunctionPackage, "Function${argCount + 1}")
        .parameterizedBy(inputTypeNames + outputTypeName + booleanName)
    val constructor = FunSpec.constructorBuilder().apply {
        addParameter(CommandPropertyName, superInterfaceName)
        addParameter(PrePropertyName, preTypeName)
        addParameter(PostPropertyName, postTypeName)
    }.build()

    addType(TypeSpec.classBuilder(className).apply<TypeSpec.Builder> {
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
        addFunction(FunSpec.builder("invoke").apply<FunSpec.Builder> {
            addModifiers(KModifier.OVERRIDE)
            (1..argCount).forEach {
                addParameter("i$it", TypeVariableName("I$it"))
            }
            returns(outputTypeName)
            addCode(CodeBlock.builder().apply<CodeBlock.Builder> {
                addComment("TODO -- validate primitive args and result")

//              val validParams = validatePrimitive(i2) && validatePrimitive(i2)
//              if (!validParams) {
//                  throw InvariantFailure()
//              }
                val inputs = (1..argCount).joinToString(", ") { "i$it" }
                val resultName = "result"

                beginControlFlow("if (!$PrePropertyName($inputs))")
                addStatement("throw PreconditionFailure()")
                endControlFlow()

                addStatement("val $resultName = $CommandPropertyName($inputs)")

//                val validResult = validatePrimitive(result)
//                if (!validResult) {
//                    throw InvariantFailure()
//                }

                beginControlFlow("if (!$PostPropertyName($inputs, $resultName))")
                addStatement("throw PostconditionFailure()")
                endControlFlow()

                addStatement("return $resultName")
            }.build())
        }.build())
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
        addCode("return $className($CommandPropertyName, $PrePropertyName, $PostPropertyName)")
        returns(ClassName(PackageName, className).parameterizedBy(inputTypeNames + outputTypeName))
    }.build())
}