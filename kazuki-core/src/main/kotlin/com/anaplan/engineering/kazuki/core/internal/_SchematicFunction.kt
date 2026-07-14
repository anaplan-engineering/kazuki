package com.anaplan.engineering.kazuki.core.internal

import com.anaplan.engineering.kazuki.core.*
import kotlin.random.Random


@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class _Input

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class _Output(val pos: Int)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class _Delta(val pos: Int)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class _Xi(val pos: Int)

abstract class _SchematicConjunction<O1, O2, O> : SchematicFunction<O> {

    protected abstract val l: SchematicFunction<O1>
    protected abstract val r: SchematicFunction<O2>

    protected abstract fun buildOutput(o1: O1, o2: O2): O

    private val lo: O1 by lazy { l.invoke() }
    private val ro: O2 by lazy { r.invoke() }

    override fun command() = buildOutput(lo, ro)

    override fun pre() = l.pre() && r.pre()

    override fun post(result: O) =
        l.post(lo) && r.post(ro) && result == buildOutput(lo, ro)

    override fun postCommand(result: O) =
        l.postCommand(lo) && r.postCommand(ro) && result == buildOutput(lo, ro)

}

abstract class _SchematicDisjunction<O1, O2, O> : SchematicFunction<O> {

    protected abstract val l: SchematicFunction<O1>
    protected abstract val r: SchematicFunction<O2>

    protected abstract fun buildOutput(o1: O1?, o2: O2?): O

    private val lBranch by lazy { l.pre() && (!r.pre() || Random.nextBoolean()) }

    private val lo: O1? by lazy { if (lBranch) l.invoke() else null }
    private val ro: O2? by lazy { if (lBranch) null else r.invoke() }

    override fun command() = buildOutput(lo, ro)

    override fun pre() = l.pre() || r.pre()

    override fun post(result: O) =
        ((lBranch && lo != null && l.post(lo!!)) || (!lBranch && ro != null && r.post(ro!!)))
                && result == buildOutput(lo, ro)

    override fun postCommand(result: O) =
        ((lBranch && lo != null && l.postCommand(lo!!)) || (!lBranch && ro != null && r.postCommand(ro!!)))
                && result == buildOutput(lo, ro)

}
