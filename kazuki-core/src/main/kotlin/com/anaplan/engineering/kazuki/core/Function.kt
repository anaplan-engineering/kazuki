package com.anaplan.engineering.kazuki.core

// TODO - measure
fun <I, O> function(
    command: Function1<I, O>,
    pre: Function1<I, Boolean> = { true },
    post: Function2<I, O, Boolean> = { _, _ -> true }
) = VFunction1(command, pre, post)

fun <I1, I2, O> function(
    command: Function2<I1, I2, O>,
    pre: Function2<I1, I2, Boolean> = { _, _ -> true },
    post: Function3<I1, I2, O, Boolean> = { _, _, _ -> true }
) = VFunction2(command, pre, post)

fun <I1, I2, I3, O> function(
    command: Function3<I1, I2, I3, O>,
    pre: Function3<I1, I2, I3, Boolean> = { _, _, _ -> true },
    post: Function4<I1, I2, I3, O, Boolean> = { _, _, _, _ -> true }
) = VFunction3(command, pre, post)

class VFunction1<I, O>(
    private val command: Function1<I, O>,
    private val pre: Function1<I, Boolean>,
    private val post: Function2<I, O, Boolean>
) : Function1<I, O> {
    override fun invoke(i1: I): O {
        val validParams = validatePrimitive(i1)
        if (!validParams) {
            throw InvariantFailure()
        }
        val admit = pre(i1)
        if (!admit) {
            throw PreconditionFailure()
        }
        val result = command(i1)
        val validResult = validatePrimitive(result)
        if (!validResult) {
            throw InvariantFailure()
        }
        val returnable = post(i1, result)
        if (!returnable) {
            throw PostconditionFailure()
        }
        return result
    }
}

class VFunction2<I1, I2, O>(
    private val command: Function2<I1, I2, O>,
    private val pre: Function2<I1, I2, Boolean>,
    private val post: Function3<I1, I2, O, Boolean>
) : Function2<I1, I2, O> {
    override fun invoke(i1: I1, i2: I2): O {
        val validParams = validatePrimitive(i2) && validatePrimitive(i2)
        if (!validParams) {
            throw InvariantFailure()
        }
        val admit = pre(i1, i2)
        if (!admit) {
            throw PreconditionFailure()
        }
        val result = command(i1, i2)
        val validResult = validatePrimitive(result)
        if (!validResult) {
            throw InvariantFailure()
        }
        val returnable = post(i1, i2, result)
        if (!returnable) {
            throw PostconditionFailure()
        }
        return result
    }
}

class VFunction3<I1, I2, I3, O>(
    private val command: Function3<I1, I2, I3, O>,
    private val pre: Function3<I1, I2, I3, Boolean>,
    private val post: Function4<I1, I2, I3, O, Boolean>
) : Function3<I1, I2, I3, O> {
    override fun invoke(i1: I1, i2: I2, i3: I3): O {
        val validParams = validatePrimitive(i2) && validatePrimitive(i2) && validatePrimitive(i3)
        if (!validParams) {
            throw InvariantFailure()
        }
        val admit = pre(i1, i2, i3)
        if (!admit) {
            throw PreconditionFailure()
        }
        val result = command(i1, i2, i3)
        val validResult = validatePrimitive(result)
        if (!validResult) {
            throw InvariantFailure()
        }
        val returnable = post(i1, i2, i3, result)
        if (!returnable) {
            throw PostconditionFailure()
        }
        return result
    }
}

// TODO -- how do we validate primitive i/o
private fun <T> validatePrimitive(t: T) = true
