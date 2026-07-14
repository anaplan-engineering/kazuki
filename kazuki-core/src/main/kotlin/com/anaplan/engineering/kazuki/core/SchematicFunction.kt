package com.anaplan.engineering.kazuki.core

import kotlin.reflect.KClass

/**
 * Schematic function is a function with a schema that binds names to input and outputs
 */
interface SchematicFunction<O>: Function0<O> {
    fun command(): O
    fun pre(): Boolean = true
    fun post(result: O): Boolean = true
    fun postCommand(result: O): Boolean = true
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Conjoin(vararg val classes: KClass<*>)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Disjoin(vararg val classes: KClass<*>)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Compose(vararg val classes: KClass<*>)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Input

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Output

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Delta

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Xi