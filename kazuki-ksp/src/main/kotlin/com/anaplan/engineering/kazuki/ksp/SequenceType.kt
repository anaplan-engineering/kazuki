package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.core.Sequence1
import com.anaplan.engineering.kazuki.core.Sequence
import com.anaplan.engineering.kazuki.core.Set1
import com.anaplan.engineering.kazuki.core.nat
import com.anaplan.engineering.kazuki.core.nat1
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toTypeName

internal fun TypeSpec.Builder.addSeqType(
    interfaceClassDcl: KSClassDeclaration,
    processingState: KazukiSymbolProcessor.ProcessingState,
) = addSeqType(interfaceClassDcl, processingState, false)

internal fun TypeSpec.Builder.addSeq1Type(
    interfaceClassDcl: KSClassDeclaration,
    processingState: KazukiSymbolProcessor.ProcessingState,
) = addSeqType(interfaceClassDcl, processingState, true)

private fun TypeSpec.Builder.addSeqType(
    interfaceClassDcl: KSClassDeclaration,
    processingState: KazukiSymbolProcessor.ProcessingState,
    requiresNonEmpty: Boolean
) {
    val interfaceName = interfaceClassDcl.simpleName.asString()
    val interfaceTypeName = interfaceClassDcl.asType(emptyList()).toTypeName()

    val properties = interfaceClassDcl.declarations.filterIsInstance<KSPropertyDeclaration>()
    if (properties.firstOrNull() != null) {
        val propertyNames = properties.map { it.simpleName.asString() }.toList()
        processingState.errors.add("Sequence type $interfaceTypeName may not have properties: $propertyNames")
    }

    val superInterface = if (requiresNonEmpty) Sequence1::class else Sequence::class
    val seqType =
        interfaceClassDcl.superTypes.single { it.resolve().declaration.qualifiedName?.asString() == superInterface.qualifiedName }
            .resolve()
    val elementType = seqType.arguments.single().type!!.resolve()
    val elementTypeDcl = elementType.declaration
    val elementClassName =
        ClassName(packageName = elementTypeDcl.packageName.asString(), elementTypeDcl.simpleName.asString())
    val elementParameterName = "elements"
    val enforceInvariantParameterName = "enforceInvariant"
    val superListTypeName = List::class.asClassName().parameterizedBy(elementClassName)
    val suffix = if (requiresNonEmpty) "Seq1" else "Seq"
    val implClassName = "${interfaceName}_$suffix"
    val implTypeSpec = TypeSpec.classBuilder(implClassName).apply {
        addModifiers(KModifier.PRIVATE)
        addSuperinterface(interfaceTypeName)
        addSuperinterface(superListTypeName, CodeBlock.of(elementParameterName))
        addSuperclassConstructorParameter(elementParameterName)
        primaryConstructor(FunSpec.constructorBuilder()
            .addParameter(elementParameterName, superListTypeName)
            .addParameter(ParameterSpec.builder(enforceInvariantParameterName, Boolean::class).defaultValue("true")
                .build())
            .build()
        )
        addProperty(
            PropertySpec.builder(elementParameterName, superListTypeName, KModifier.OPEN)
                .initializer(elementParameterName).build()
        )
        // TODO - get operator override
        addProperty(PropertySpec.builder("len", nat::class.asTypeName()).addModifiers(KModifier.OVERRIDE)
            .lazy("size").build())
        val correspondingSetInterface = if (requiresNonEmpty) Set1::class else Set::class
        val correspondingSetConstructor = if (requiresNonEmpty) InbuiltNames.mkSet1 else InbuiltNames.mkSet
        addProperty(PropertySpec.builder("elems",
            correspondingSetInterface.asClassName().parameterizedBy(elementClassName))
            .addModifiers(
                KModifier.OVERRIDE)
            .lazy("%M(%N)", correspondingSetConstructor, elementParameterName).build())
        addProperty(PropertySpec.builder("inds",
            correspondingSetInterface.asClassName().parameterizedBy(nat1::class.asClassName()))
            .addModifiers(
                KModifier.OVERRIDE)
            .lazy("%M(1 .. len)", correspondingSetConstructor).build())

        // N.B. it is important to have properties before init block
        val additionalInvariantParts = if (requiresNonEmpty) listOf("len > 0") else emptyList()
        addInvariantFrom(interfaceClassDcl,
            false,
            enforceInvariantParameterName,
            processingState,
            additionalInvariantParts)

        addFunction(
            FunSpec.builder("get").apply {
                val indexParameterName = "index"
                addModifiers(KModifier.OVERRIDE, KModifier.OPERATOR)
                addParameter(ParameterSpec.builder(indexParameterName, nat1::class.asTypeName()).build())
                returns(elementClassName)
                addStatement("return %N.get(%N - 1)", elementParameterName, indexParameterName)
            }.build()
        )
        addFunction(FunSpec.builder("toString").addModifiers(KModifier.OVERRIDE)
            .returns(String::class)
            .addStatement("return \"%N\$%N\"", interfaceName, elementParameterName)
            .build())
        addFunction(FunSpec.builder("hashCode").addModifiers(KModifier.OVERRIDE)
            .returns(Int::class).addStatement("return %N.hashCode()", elementParameterName).build())
        val equalsParameterName = "other"
        addFunction(FunSpec.builder("equals").addModifiers(KModifier.OVERRIDE)
            .addParameter(ParameterSpec.builder(equalsParameterName, Any::class.asTypeName().copy(nullable = true))
                .build())
            .returns(Boolean::class).addCode(CodeBlock.builder().apply {
                beginControlFlow("if (this === %N)", equalsParameterName)
                addStatement("return true")
                endControlFlow()

                beginControlFlow("if (%N !is %T)", equalsParameterName, superInterface.asTypeName().parameterizedBy(
                    STAR))
                addStatement("return false")
                endControlFlow()

                addStatement("return %N == %N", elementParameterName, equalsParameterName)

            }.build()).build())
    }.build()
    addType(implTypeSpec)

    addFunction(
        FunSpec.builder("mk_$interfaceName").apply {
            addParameter(elementParameterName, superListTypeName)
            returns(interfaceTypeName)
            addStatement("return %N(%N)", implTypeSpec, elementParameterName)
        }.build()
    )
    addFunction(
        FunSpec.builder("mk_$interfaceName").apply {
            addParameter(elementParameterName, elementType.toTypeName(), KModifier.VARARG)
            returns(interfaceTypeName)
            addStatement("return %N(%N.toList())", implTypeSpec, elementParameterName)
        }.build()
    )
    // TODO -- need to be able to access invariant and use it to validate
    addFunction(
        FunSpec.builder("is_$interfaceName").apply {
            addParameter(elementParameterName, superListTypeName)
            returns(Boolean::class)
            addStatement("return %N(%N, false).%N()", implClassName, elementParameterName, validityFunctionName)
        }.build()
    )
    addFunction(
        FunSpec.builder("as_$interfaceName").apply {
            addParameter(elementParameterName, superListTypeName)
            returns(interfaceTypeName)
            addStatement("return %N(%N)", "mk_$interfaceName", elementParameterName)
        }.build()
    )
}
