package com.anaplan.engineering.kazuki.core.internal

import com.anaplan.engineering.kazuki.core.Tuple


internal data class _CacheKey(
    val function: Function<*>,
    val args: Tuple
)

internal object _EvaluationCache {

    // TODO - use sys property to init cache size
    // TODO - cache stats logging
    internal val cache by lazy {
        _FunctionCache(10_000)
    }

}

// TODO -- not thread safe
internal class _FunctionCache(private val maxSize: Int) :
    LinkedHashMap<_CacheKey, Any?>(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, true) {
    private var dropCount = 0

    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<_CacheKey, Any?>?): Boolean {
        val remove = size > maxSize
        if (remove) {
            ++dropCount
        }
        return remove
    }

    override fun clear() {
        super.clear()
        dropCount = 0
    }

    companion object {
        // cannot access directly from HashMap
        private val DEFAULT_INITIAL_CAPACITY = 1 shl 4 // aka 16

        // cannot access directly from HashMap
        private const val DEFAULT_LOAD_FACTOR = 0.75f
    }
}