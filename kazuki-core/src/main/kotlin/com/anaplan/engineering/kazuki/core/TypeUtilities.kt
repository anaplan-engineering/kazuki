package com.anaplan.engineering.kazuki.core

import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses

inline fun <reified T : Any> is_(a: Any?) =
    if (a == null) {
        false
    } else if (Collection::class in T::class.allSuperclasses && a !is Collection<*>) {
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

inline fun <reified T : Any> as_(a: Any): T =
    if (a is T) {
        a
    } else {
        pre { is_<T>(a) }
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

inline fun <reified T : Any> to_(a: Any): T {
    pre { is_<T>(a) }
    val moduleClass = getModuleClass<T>()
    val fn = moduleClass?.members?.find { it.name == "to_${T::class.simpleName}" }
    val cast = if (fn == null) {
        a
    } else {
        fn.call(moduleClass.objectInstance, a)
    }
    pre { cast is T }
    return cast as T
}

inline fun <reified T> getModuleClass(): KClass<out Any>? {
    val moduleName = "${T::class.qualifiedName}_Module"
    return try {
        Class.forName(moduleName).kotlin
    } catch (_: ClassNotFoundException) {
        null
    }
}