package com.anaplan.engineering.kazuki.generation

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
abstract class TupleGeneratorTask : DefaultTask() {

    companion object {
        private const val MaxNary = 10
    }

    val generationSrcDir: File
        @OutputDirectory
        get() = project.generationSrcDir()

    @TaskAction
    fun apply() {
        FileSpec.builder(PackageName, FileName)
            .addFileComment("This file is generated -- do not edit!")
            .apply {
                (1..MaxNary).forEach {
                    addNAryTuple(it)
                }
            }
            .build()
            .writeTo(generationSrcDir)
    }
}

private const val FileName = "Tuples"

fun FileSpec.Builder.addNAryTuple(nary: Int) {
    val interfaceName = "Tuple$nary"
    val className = "_$interfaceName"
    val typeNames = (1..nary).map { TypeVariableName("T$it") }
    val constructor = FunSpec.constructorBuilder().apply {
        (1..nary).forEach {
            addParameter("_$it", TypeVariableName("T$it"))
        }
    }.build()

    addType(TypeSpec.interfaceBuilder(interfaceName).apply {
        addTypeVariables(typeNames)
        (1..nary).forEach {
            addProperty(PropertySpec.builder("_$it", TypeVariableName("T$it")).build())
            addFunction(FunSpec.builder("component$it").addModifiers(KModifier.OPERATOR).addStatement("return _$it").returns(TypeVariableName("T$it")).build())        }
    }.build())

    addType(TypeSpec.classBuilder(className).apply {
        addModifiers(KModifier.DATA, KModifier.PRIVATE)
        addTypeVariables(typeNames)
        addSuperinterface(ClassName(PackageName, interfaceName).parameterizedBy(typeNames))
        primaryConstructor(constructor)
        (1..nary).forEach {
            addProperty(
                PropertySpec.builder("_$it", TypeVariableName("T$it")).addModifiers(KModifier.OVERRIDE)
                    .initializer("_$it").build()
            )
        }
        // TODO -- toSequence
        // TODO -- toString
    }.build())

    addFunction(FunSpec.builder("mk_").apply {
        addTypeVariables(typeNames)
        (1..nary).forEach {
            addParameter("_$it", TypeVariableName("T$it"))
        }
        addCode("return $className(${(1..nary).joinToString(", ") { "_$it" }})")
        returns(ClassName(PackageName, interfaceName).parameterizedBy(typeNames))
    }.build())
}