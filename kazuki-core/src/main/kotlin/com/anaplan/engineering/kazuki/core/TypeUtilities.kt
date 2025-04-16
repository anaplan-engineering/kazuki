package com.anaplan.engineering.kazuki.core

import kotlin.RequiresOptIn.Level
import kotlin.reflect.KClass

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "These functions are experimental and will change soon ae move to add to_ and distinguish from as_"
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS,
)
annotation class ExperimentalIsAs


@ExperimentalIsAs
inline fun <reified T: Any> is_(a: Any?) =
    if (a == null) {
        false
    } else {
        val moduleName = "${T::class.qualifiedName}_Module"
        val moduleClass = try {
            Class.forName(moduleName).kotlin
        } catch (_: ClassNotFoundException) {
            null
        }
        val fn = moduleClass?.members?.find { it.name == "is_${T::class.simpleName}" }
        if (fn == null) {
            a is T
        } else {
            fn.call(moduleClass.objectInstance, a) == true
        }
    }

@ExperimentalIsAs
inline fun <reified T: Any> as_(a: Any): T =
    if (a is T) {
        a
    } else {
        // TODO -- use a wrapper to simulate and introduce distinction between as_ and to_
        val moduleClass = getModuleClass<T>()
        val fn = moduleClass?.members?.find { it.name == "as_${T::class.simpleName}" }
        val cast = if (fn == null) {
            a
        } else {
            fn.call(moduleClass.objectInstance, a)
        }
        pre { cast is T }
        cast as T
    }

inline fun <reified T> getModuleClass(): KClass<out Any>? {
    val moduleName = "${T::class.qualifiedName}_Module"
    return try {
        Class.forName(moduleName).kotlin
    } catch (_: ClassNotFoundException) {
        null
    }
}