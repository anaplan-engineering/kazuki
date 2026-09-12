package com.anaplan.engineering.kazuki.adt

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.adt.StackADT_Module.transform
import com.anaplan.engineering.kazuki.adt.Stack_Module.mk_Stack

/**
 * A Kazuki specification inspired by https://github.com/leouk/VDM_Toolkit/blob/main/vdmlib/src/main/resources/Stack.vdmsl,
 * which was originally developed by Leo Freitas.
 *
 * The motivation is to create a SPARK/Ada-styled abstract data type, as opposed to a generically-typed module type from the VDM.
 * It is stricter than the VDM in the sense of not allowing certain scenarios (e.g. popping empty stack, etc.)
 */
@Module(makeable = false)
@ImplementedBy(StackADT::class)
@ConstructedBy(style = ConstructorStyle.VARARG_ELEMENTS, adapter = "as_Seq")
interface Stack<out T> {
    @FunctionProvider(StackFunctions::class)
    val functions: StackFunctions<T, Stack<T>>

    @FunctionProvider(StackProperties::class)
    val properties: StackProperties<T>
}

interface StackFunctions<out T, out S: Stack<T>> {
    val empty: () -> S
    val pop: () -> S
    val top: () -> T
    val push: (@UnsafeVariance T) -> S
}

interface StackProperties<out T> {
    val isEmpty: bool
    val size: nat
    val elems: Set<T>
}

@Module
internal interface StackADT<T> : Stack<T> {
    val value: Sequence<T>

    @FunctionProvider(StackADTFunctions::class)
    override val functions: StackADTFunctions<T>

    @FunctionProvider(StackADTProperties::class)
    override val properties: StackADTProperties<T>
}

/**
 * With the ADT, we strive to minimise/avoid leaking of `stack.value` outside of `Stack`.
 * That is, we strive to keep access to `stack.value` to function `command` only.
 */
internal class StackADTFunctions<T>(private val stack: StackADT<T>) : StackFunctions<T, Stack<T>> {

    override val empty = function(
        command = { -> mk_Stack(mk_Seq<T>()) },
        post = { result -> result.properties.isEmpty }
    )

    override val pop = function(
        command = { -> stack.transform(value = stack.value.take(stack.properties.size - 1u)) },
        pre = { -> !stack.properties.isEmpty },
        post = { result ->
            (stack.properties.size == result.properties.size + 1uL) and {
                //ideally, we would want? result.value subseq result.value
                result.properties.elems subset stack.properties.elems
            }
        }
    )

    override val push = function(
        command = { item: T -> stack.transform(value = stack.value + item) },
        post = { item, result ->
            (stack.properties.size + 1uL == result.properties.size) and {
                item in result.properties.elems
            }
        }
    )

    override val top = function(
        command = { -> stack.value.last() },
        pre = { -> !stack.properties.isEmpty },
    )
}

internal class StackADTProperties<T>(stack: StackADT<T>) : StackProperties<T> {
    override val isEmpty by property { stack.properties.size == 0uL }
    override val size by property { stack.value.len }
    override val elems by property { stack.value.elems }
}

// Public mk_Stack(vararg elements: T) is generated on Stack_Module by @ConstructedBy.
// Canonical mk_Stack(value: Sequence<T>) stays internal (default publicMk = false, hideCanonical = true).
// To expose the canonical constructor instead, use @ImplementedBy(..., publicMk = true) and omit @ConstructedBy.