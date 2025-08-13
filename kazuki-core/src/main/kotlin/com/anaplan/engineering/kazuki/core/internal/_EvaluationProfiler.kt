package com.anaplan.engineering.kazuki.core.internal

import com.anaplan.engineering.kazuki.core.Kazuki
import java.util.Stack
import kotlin.math.max

// TODO - assumes single thread -- does it need to be thread safe?
object _EvaluationProfiler {

    private data class Invocation(
        val id: String,
        val instance: Function<*>,
        val invocation: Int,
        var start: Long,
        var evaluationTime: Long
    )

    private val invocationStack by lazy { Stack<Invocation>() }

    private data class ProfileResult(
        val id: String,
        var instanceCount: Int,
        var invocationCount: Int,
        var evaluationTime: Long,
    )

    private val results by lazy { HashMap<String, ProfileResult>() }

    internal fun createInstance(id: String) {
        if (Kazuki.Profile) {
            val result = results[id]
            if (result == null) {
                results[id] = ProfileResult(id, 1, 0, 0L)
            } else {
                result.instanceCount++
            }
        }
    }

    internal fun startInvocation(id: String, instance: Function<*>, invocationId: Int) {
        if (Kazuki.Profile) {
            val invocationPoint = System.nanoTime()
            if (invocationStack.isNotEmpty()) {
                invocationStack.peek().apply {
                    evaluationTime += invocationPoint - start
                    start = -1L
                }
            }
            invocationStack.push(Invocation(id, instance, invocationId, invocationPoint, 0L))
        }
    }

    internal fun endInvocation(id: String, instance: Function<*>, invocationId: Int) {
        if (Kazuki.Profile) {
            val invocationPoint = System.nanoTime()
            val invocation = invocationStack.pop()
            require(invocation.id == id && invocation.instance == instance && invocation.invocation == invocationId)
            results[id]!!.apply {
                invocationCount++
                evaluationTime = evaluationTime +
                        invocation.evaluationTime +
                        (invocationPoint - invocation.start)
            }
            if (invocationStack.isNotEmpty()) {
                invocationStack.peek().apply {
                    start = invocationPoint
                }
            }
        }
    }

    fun log() {
        Kazuki.Log.info("Evaluation profile results:")
        val slowest = results.values.sortedByDescending { it.evaluationTime }.take(20)
        val maxId = max(10, slowest.maxOfOrNull { it.id.length } ?: 10)
        val maxInstance = max(5, slowest.maxOfOrNull { it.instanceCount.toString().length } ?: 5)
        val maxInv = max(5, slowest.maxOfOrNull { it.invocationCount.toString().length } ?: 5)
        Kazuki.Log.info(String.format("%-${maxId}s|%${maxInstance}s|%${maxInv}s|%s", "Function", "Inst", "Inv", "Duration"))
        slowest.forEach {
            val logLine =
                String.format("%-${maxId}s|%${maxInstance}d|%${maxInv}d|%s", it.id, it.instanceCount, it.invocationCount, it.evaluationTime.toString())
            Kazuki.Log.info(logLine)
        }
    }

}