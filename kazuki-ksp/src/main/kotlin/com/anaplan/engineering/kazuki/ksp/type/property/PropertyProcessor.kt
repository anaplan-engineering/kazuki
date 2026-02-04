package com.anaplan.engineering.kazuki.ksp.type.property

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.internal.*
import com.anaplan.engineering.kazuki.ksp.getClassDeclaration
import com.anaplan.engineering.kazuki.ksp.superModules
import com.anaplan.engineering.kazuki.ksp.type.TypeGenerationContext
import com.anaplan.engineering.kazuki.ksp.type.getComparableProperty
import com.google.devtools.ksp.*
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver

@OptIn(KspExperimental::class)
internal class PropertyProcessor(
    private val classDcl: KSClassDeclaration,
    private val typeGenerationContext: TypeGenerationContext,
    private val allowFields: Boolean = false
) {

    private val typeParameterResolver = classDcl.typeParameters.toTypeParameterResolver()

    internal fun process(): Properties {
        val properties = classDcl.declarations.filterIsInstance<KSPropertyDeclaration>()
        if (properties.any { it.isMutable }) {
            val mutableProperties = properties.filter { it.isMutable }.map { it.simpleName.asString() }.toList()
            typeGenerationContext.errors.add("Module $classDcl may not have mutable properties: $mutableProperties")
        }

        val comparableProperty = getComparableProperty(classDcl, typeGenerationContext)
        val functionProviderProperties = getFunctionProviderProperties(classDcl, typeGenerationContext)
        val recordProperties = (properties - comparableProperty - functionProviderProperties.map { it.property }.toSet()).filterNotNull()

        val allInterfaceProperties = classDcl.getAllProperties().toList()
        val propertyBuilders = classDcl.superModules.reversed().map { type ->
            val superClassDcl = getClassDeclaration(type)
            val superProperties = if (superClassDcl.containingFile == null && allowFields) {
                // Properties in class file have arbitrary order so identify correct order from generated record
                // TODO - We only care about order if we're allowing fields, but is it bad to assume record?
                val superClassName = superClassDcl.simpleName.asString()
                val name = "${superClassDcl.packageName.asString()}.${superClassName}_Module.${superClassName}_Rec"
                val superClassRecordDcl = typeGenerationContext.resolver.getKotlinClassByName(name)!!
                val recordAnnotation = superClassRecordDcl.getAnnotationsByType(_Record::class).single()
                recordAnnotation.fields.map { f -> (superClassDcl.declarations.find { it.simpleName.asString() == f } as? KSPropertyDeclaration) }
                    .filterNotNull()
            } else {
                superClassDcl.declarations.filterIsInstance<KSPropertyDeclaration>().toList()
            }
            val superFunctionProviderProperties =
                superProperties.filter { it.isAnnotationPresent(FunctionProvider::class) }
            val superFunctionComparableProperties =
                superProperties.filter { it.isAnnotationPresent(ComparableProperty::class) }
            val superRecordProperties = (superProperties - superFunctionProviderProperties - superFunctionComparableProperties).toList()
            superRecordProperties.map { superProperty -> allInterfaceProperties.find { interfaceProperty -> superProperty.simpleName == interfaceProperty.simpleName }!! }
                .associate { property ->
                    property.type.resolve()
                    val name = property.simpleName.asString()
                    name to PropertyBuilder(
                        name,
                        !property.isAbstract(),
                        property.type,
                        property.type.toTypeName(typeParameterResolver)
                    )
                }
        } + recordProperties.associate { property ->
            val name = property.simpleName.asString()
            name to PropertyBuilder(
                name,
                !property.isAbstract(),
                property.type,
                property.type.toTypeName(typeParameterResolver)
            )
        }
        val resolvedPropertyBuilders = propertyBuilders.fold(mapOf<String, PropertyBuilder>()) { acc, it ->
            acc + it
        }.map { (_, v) -> v }
        val tupleComponents = resolvedPropertyBuilders.mapIndexed { i, b -> b.buildTuple(i + 1) }
        if (!allowFields && tupleComponents.isNotEmpty()) {
            val propertyNames = tupleComponents.joinToString(", ") { it.name }
            typeGenerationContext.errors.add("Type $classDcl may not have fields: $propertyNames")
        }
        return Properties(functionProviderProperties, tupleComponents)
    }

    private data class PropertyBuilder(
        val name: String,
        val isDynamic: Boolean,
        val typeReference: KSTypeReference,
        val typeName: TypeName
    ) {
        fun buildTuple(index: Int) = TupleComponent(index, name, typeReference, typeName, isDynamic)
    }
}

internal data class Properties(
    val functionProviders: Collection<FunctionProviderProperty>,
    val tupleComponents: List<TupleComponent>,
)

data class TupleComponent(
    val index: Int,
    val name: String,
    val typeReference: KSTypeReference,
    val typeName: TypeName,
    val fixed: Boolean,
)

