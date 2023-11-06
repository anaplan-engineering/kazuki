package com.anaplan.engineering.kazuki.toolkit.examples

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.OrderedSet
import com.anaplan.engineering.kazuki.toolkit.examples.Stack_Module.mk_Stack

@Module
interface Stack<T> : OrderedSet<T> {

    @FunctionProvider(Functions::class)
    val functions: Functions<T>

    class Functions<T>(private val stack: Stack<T>) {

        val push = function(
            command = { t: T -> mk_Stack(listOf(t) + stack) },
            pre = { t: T -> t !in stack },
            post = { t: T, result: Stack<T> ->
                result.isNotEmpty() && result.first() == t && result.drop(1) == stack
            }
        )

        val pop = function(
            command = { mk_(stack.first(), stack.drop(1)) },
            pre = { stack.len > 0 },
            post = { result: Tuple2<T, Stack<T>> -> result._2.functions.push(result._1) == stack }
        )

    }
}