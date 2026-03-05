package com.anaplan.engineering.kazuki.generation

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import kotlin.reflect.KClass

@CacheableTask
abstract class TupleGeneratorTask : DefaultTask() {

    companion object {
        private const val MaxNary = 20
    }

    val generationSrcDir: File
        @OutputDirectory
        get() = project.generationSrcDir()

    @TaskAction
    fun apply() {
        FileSpec.builder(RootPackageName, FileName)
            .addFileComment("This file is generated -- do not edit!")
            .apply {
                (1..MaxNary).forEach {
                    addNAryTuple(it)
                }
            }
            .build()
            .writeTo(generationSrcDir)
        FileSpec.builder(InternalPackageName, FileName)
            .addFileComment("This file is generated -- do not edit!")
            .apply {
                addConstructableInternal()
                (1..MaxNary).forEach {
                    addNaryConstructableInternal(it)
                    addNAryTupleInternal(it)
                }
            }
            .build()
            .writeTo(generationSrcDir)
    }
}

private const val FileName = "Tuples"

private fun publicInterfaceName(nary: Int) = "Tuple$nary"
private fun internalInterfaceName(nary: Int) = "_${publicInterfaceName(nary)}"
private fun internalDataClassName(nary: Int) = "_${internalInterfaceName(nary)}"
private fun constructableInterfaceName(nary: Int) = "_Constructable$nary"

private const val constructFunctionName = "construct"
private const val isValidFunctionName = "isValid"

// No obvious way to share with core
private const val comparableWithPropertyName = "comparableWith"
private val comparableWithTypeName = KClass::class.asTypeName().parameterizedBy(STAR)

fun FileSpec.Builder.addNAryTuple(nary: Int) {
    val interfaceName = publicInterfaceName(nary)
    val className = internalDataClassName(nary)
    val typeNames = (1..nary).map { TypeVariableName("T$it") }

    addType(TypeSpec.interfaceBuilder(interfaceName).apply {
        addTypeVariables((1..nary).map { TypeVariableName("T$it", variance = KModifier.OUT) })
        addSuperinterface(TupleInterfaceName)
        (1..nary).forEach {
            addProperty(PropertySpec.builder("_$it", TypeVariableName("T$it")).build())
            addFunction(
                FunSpec.builder("component$it").addModifiers(KModifier.OPERATOR).addStatement("return _$it")
                    .returns(TypeVariableName("T$it")).build()
            )
        }
    }.build())

    val interfaceTypeName = ClassName(RootPackageName, interfaceName).parameterizedBy(typeNames)

    addFunction(FunSpec.builder("mk_").apply {
        addTypeVariables(typeNames)
        (1..nary).forEach {
            addParameter("_$it", TypeVariableName("T$it"))
        }
        addCode("return $InternalPackageName.$className(${(1..nary).joinToString(", ") { "_$it" }})")
        returns(interfaceTypeName)
    }.build())

    addFunction(
        FunSpec.builder("transform").apply {
            val t = TypeVariableName("T")
            addTypeVariables(typeNames + t.copy(bounds = listOf(interfaceTypeName)))
            receiver(t)
            (1..nary).forEach {
                addParameter(ParameterSpec.builder("_$it", TypeVariableName("T$it")).apply {
                    defaultValue("this._$it")
                }.build())
            }
            returns(t)
            addAnnotation(uncheckedCastAnnotation())
            addCode(CodeBlock.builder().apply {
                val constructableClassName = ClassName(
                    InternalPackageName,
                    "_Constructable${nary}"
                )
                val constructableTypeName =
                    constructableClassName.parameterizedBy(typeNames + t)
                val erasedConstructableTypeName =
                    constructableClassName.parameterizedBy(typeNames.map { STAR } + STAR)

                beginControlFlow(
                    "${RootPackageName}.pre(%P)",
                    "Cannot set on instance of $interfaceName created outside Kazuki [\${this::class}]"
                )
                addStatement("this·is·%T", erasedConstructableTypeName)
                endControlFlow()

                addStatement(
                    "return (this·as·%T).construct(${(1..nary).joinToString(", ") { "_$it" }}, true)",
                    constructableTypeName,
                )
            }.build())
        }.build()
    )

}


fun FileSpec.Builder.addNAryTupleInternal(nary: Int) {
    val internalInterfaceName = internalInterfaceName(nary)
    val className = internalDataClassName(nary)
    val typeNames = (1..nary).map { TypeVariableName("T$it") }
    val constructor = FunSpec.constructorBuilder().apply {
        (1..nary).forEach {
            addParameter("_$it", TypeVariableName("T$it"))
        }
    }.build()

    val publicInterfaceName = publicInterfaceName(nary)
    val publicInterfaceClassName = ClassName(RootPackageName, publicInterfaceName)
    addType(TypeSpec.interfaceBuilder(internalInterfaceName).apply {
        addTypeVariables(typeNames + TypeVariableName("T"))
        addSuperinterface(publicInterfaceClassName.parameterizedBy(typeNames))
        (1..nary).forEach { conNary ->
            val constructableTypeArgs = (1..conNary).map { TypeVariableName("T$it") } + TypeVariableName("T")
            addSuperinterface(
                ClassName(InternalPackageName, constructableInterfaceName(conNary)).parameterizedBy(
                    constructableTypeArgs
                )
            )
        }
        addProperty(
            PropertySpec.builder(comparableWithPropertyName, comparableWithTypeName, KModifier.OPEN).build()
        )
    }.build())

    addType(TypeSpec.classBuilder(className).apply {
        addModifiers(KModifier.DATA, KModifier.INTERNAL)
        addTypeVariables(typeNames)
        addSuperinterface(
            ClassName(InternalPackageName, internalInterfaceName)
                .parameterizedBy(typeNames + publicInterfaceClassName.parameterizedBy(typeNames))
        )
        primaryConstructor(constructor)
        (1..nary).forEach {
            addProperty(
                PropertySpec.builder("_$it", TypeVariableName("T$it")).addModifiers(KModifier.OVERRIDE)
                    .initializer("_$it").build()
            )
        }
        (1..nary).forEach { conNary ->
            addFunction(FunSpec.builder(constructFunctionName).apply {
                addModifiers(KModifier.OVERRIDE)
                (1..conNary).forEach { addParameter("t$it", TypeVariableName("T$it")) }
                addParameter("enforceInvariant", Boolean::class.asTypeName())
                val constructedType = ClassName(InternalPackageName, className).parameterizedBy(typeNames)
                returns(constructedType)
                val params = (1..conNary).map { "t$it" } + (conNary + 1..nary).map { "_$it" }
                addStatement("return %T(${params.joinToString(",")})", constructedType)
            }.build())
        }
        addProperty(
            PropertySpec.builder(comparableWithPropertyName, comparableWithTypeName, KModifier.OPEN, KModifier.OVERRIDE)
                .initializer(CodeBlock.of("$publicInterfaceName::class")).build()
        )
        addFunction(FunSpec.builder(PrettyFunctionName).apply {
            addModifiers(KModifier.OVERRIDE)
            returns(String::class)
            addStatement(
                "return %P",
                "(${(1..nary).joinToString(", ") { "$it=\${$PrettyOrDefaultFunctionName(_$it)}" }})"
            )
        }.build())
        addFunction(FunSpec.builder("toString").apply {
            addModifiers(KModifier.OVERRIDE)
            returns(String::class)
            addStatement("return %P", "(${(1..nary).joinToString(", ") { "\$_$it" }})")
        }.build())
    }.build())

}

private val ConstructableInterfaceName = "_Constructable"
private val ConstructableInterfaceClassName = ClassName(InternalPackageName, ConstructableInterfaceName)

fun FileSpec.Builder.addNaryConstructableInternal(nary: Int) {
    val naryConstructableInterfaceName = constructableInterfaceName(nary)
    val typeNames = (1..nary).map { TypeVariableName("T$it") } + TypeVariableName("T")

    addType(TypeSpec.interfaceBuilder(naryConstructableInterfaceName).apply {
        addTypeVariables(typeNames)
        addSuperinterface(ConstructableInterfaceClassName)
        addFunction(FunSpec.builder(constructFunctionName).apply {
            addModifiers(KModifier.ABSTRACT)
            (1..nary).forEach { addParameter("t$it", TypeVariableName("T$it")) }
            addParameter("enforceInvariant", Boolean::class.asTypeName())
            returns(TypeVariableName("T"))
        }.build())
    }.build())
}

fun FileSpec.Builder.addConstructableInternal() {
    val booleanTypeName = Boolean::class.asTypeName()
    addType(TypeSpec.interfaceBuilder(ConstructableInterfaceName).apply {
        addFunction(FunSpec.builder(isValidFunctionName).apply {
            addStatement("return true")
            returns(booleanTypeName)
        }.build())

    }.build())
}

internal fun uncheckedCastAnnotation() = AnnotationSpec.builder(Suppress::class).apply {
    addMember("names = arrayOf(%S)", "UNCHECKED_CAST")
}.build()