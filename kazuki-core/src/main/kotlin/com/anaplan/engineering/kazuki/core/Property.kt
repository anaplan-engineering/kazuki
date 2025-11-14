package com.anaplan.engineering.kazuki.core

import kotlin.reflect.KProperty

interface Property<T> {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T
}

internal class PropertyDelegate<T>(
    pre: () -> Boolean,
    init: () -> T,
    post: (T) -> Boolean,
    private val logAs: String?,
): Property<T> {
    val propertyId: String

    init {
        val frame = StackWalker.getInstance(setOf(StackWalker.Option.SHOW_HIDDEN_FRAMES), 6).walk {
            it.limit(4).reduce { _, r -> r }.get()
        }
        propertyId = "${frame.className}(${frame.fileName}:${frame.lineNumber})"
    }

    private val value by lazy {
        if (!pre()) {
            val name = logAs ?: propertyId
            throw PreconditionFailure("In $name")
        }
        val result = init()
        if (!post(result)) {
            val name = logAs ?: propertyId
            val msg = "In $name=${result.prettyOrDefault()}"
            throw PostconditionFailure(msg)
        }
        result
    }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value

}

fun <T> property(
    pre: () -> Boolean = { true },
    post: (T) -> Boolean = { true },
    logAs: String? = null,
    init: () -> T
): Property<T> = PropertyDelegate(pre, init, post, logAs)

