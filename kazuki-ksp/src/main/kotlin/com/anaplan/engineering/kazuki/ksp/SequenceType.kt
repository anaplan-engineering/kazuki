package com.anaplan.engineering.kazuki.ksp

import com.anaplan.engineering.kazuki.core.Sequence1
import com.anaplan.engineering.kazuki.core.Set1
import com.anaplan.engineering.kazuki.core.nat
import com.anaplan.engineering.kazuki.core.nat1
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toTypeName

internal fun TypeSpec.Builder.addSeq1Type(
    interfaceClassDcl: KSClassDeclaration,
    processingState: KazukiSymbolProcessor.ProcessingState,
) {
    val interfaceName = interfaceClassDcl.simpleName.asString()
    val interfaceTypeName = interfaceClassDcl.asType(emptyList()).toTypeName()

    val properties = interfaceClassDcl.declarations.filterIsInstance<KSPropertyDeclaration>()
    if (properties.firstOrNull() != null) {
        val propertyNames = properties.map { it.simpleName.asString() }.toList()
        processingState.errors.add("Sequence type $interfaceTypeName may not have properties: $propertyNames")
    }

    val seqType =
        interfaceClassDcl.superTypes.single { it.resolve().declaration.qualifiedName?.asString() == Sequence1::class.qualifiedName }
            .resolve()
    val elementType = seqType.arguments.single().type!!.resolve()
    val elementTypeDcl = elementType.declaration
    val elementClassName =
        ClassName(packageName = elementTypeDcl.packageName.asString(), elementTypeDcl.simpleName.asString())
    val elementParameterName = "elements"
    val superListTypeName = List::class.asClassName().parameterizedBy(elementClassName)
    val implTypeSpec = TypeSpec.classBuilder("${interfaceName}_Seq1").apply {
        addModifiers(KModifier.PRIVATE)
        addSuperinterface(interfaceTypeName)
        addSuperinterface(superListTypeName, CodeBlock.of(elementParameterName))
        addSuperclassConstructorParameter(elementParameterName)
        primaryConstructor(FunSpec.constructorBuilder()
            .addParameter(elementParameterName, superListTypeName)
            .build()
        )
        addProperty(
            PropertySpec.builder(elementParameterName, superListTypeName, KModifier.OPEN)
                .initializer(elementParameterName).build()
        )
        // TODO - get operator override
        addProperty(PropertySpec.builder("len", nat::class.asTypeName()).addModifiers(KModifier.OVERRIDE)
            .lazy("size").build())
        addProperty(PropertySpec.builder("elems", Set1::class.asClassName().parameterizedBy(elementClassName))
            .addModifiers(
                KModifier.OVERRIDE)
            .lazy("%M(%N)", InbuiltNames.mkSet1, elementParameterName).build())
        addProperty(PropertySpec.builder("inds", Set1::class.asClassName().parameterizedBy(nat1::class.asClassName()))
            .addModifiers(
                KModifier.OVERRIDE)
            .lazy("%M(1 .. len)", InbuiltNames.mkSet1).build())

        // N.B. it is important to have properties before init block
        addInvariantFrom(interfaceClassDcl, false, processingState)

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

                beginControlFlow("if (%N !is %T)", equalsParameterName, Sequence1::class.asTypeName().parameterizedBy(
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
//    addFunction(
//        FunSpec.builder("is_$interfaceName").apply {
//            addParameter(elementParameterName, Sequence::class, KModifier.VARARG)
//            returns(interfaceTypeName)
//            addStatement("return %N(%N.toList())", implTypeSpec, elementParameterName)
//        }.build()
//    )
}
