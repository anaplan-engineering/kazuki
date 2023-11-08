package com.anaplan.engineering.kazuki.core

// TODO - measure
fun <I, O> function(
    command: Function1<I, O>,
    pre: Function1<I, Boolean> = { true },
    post: Function1<O, Boolean> = { true }
) = VFunction1(command, pre, post)

fun <I1, I2, O> function(
    command: Function2<I1, I2, O>,
    pre: Function2<I1, I2, Boolean> = { _, _ -> true },
    post: Function1<O, Boolean> = { true }
) = VFunction2(command, pre, post)

class VFunction1<I, O>(
    private val command: Function1<I, O>,
    private val pre: Function1<I, Boolean>,
    private val post: Function1<O, Boolean>
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
        val returnable = post(result)
        if (!returnable) {
            throw PostconditionFailure()
        }
        return result
    }
}

class VFunction2<I1, I2, O>(
    private val command: Function2<I1, I2, O>,
    private val pre: Function2<I1, I2, Boolean>,
    private val post: Function1<O, Boolean>
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
        val returnable = post(result)
        if (!returnable) {
            throw PostconditionFailure()
        }
        return result
    }
}

// TODO -- how do we validate primitive i/o
private fun <T> validatePrimitive(t: T) = true
