package com.anaplan.engineering.kazuki.toolkit

import com.anaplan.engineering.kazuki.core.FunctionProvider
import com.anaplan.engineering.kazuki.core.Mapping
import com.anaplan.engineering.kazuki.core.Sequence
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.VFunction1
import com.anaplan.engineering.kazuki.core.as_Set
import com.anaplan.engineering.kazuki.core.domSubtract
import com.anaplan.engineering.kazuki.core.filter
import com.anaplan.engineering.kazuki.core.forall
import com.anaplan.engineering.kazuki.core.function
import com.anaplan.engineering.kazuki.core.iff
import com.anaplan.engineering.kazuki.core.inter
import com.anaplan.engineering.kazuki.core.iota
import com.anaplan.engineering.kazuki.core.mk_
import com.anaplan.engineering.kazuki.core.nat
import com.anaplan.engineering.kazuki.core.nat1
import com.anaplan.engineering.kazuki.core.property
import com.anaplan.engineering.kazuki.core.set
import com.anaplan.engineering.kazuki.core.subset
import com.anaplan.engineering.kazuki.toolkit.Bag_Module.as_Bag
import com.anaplan.engineering.kazuki.toolkit.Bag_Module.mk_Bag
import kotlin.math.max

// Based on VDM_Toolkit Bag.vdmsl (which is based on Z/Eves bags)
@Module
interface Bag<T> : Mapping<T, nat1> {

    //val empty = mk_Mapping<T, nat1>()

    @FunctionProvider(BagProperties::class)
    val properties: BagProperties<T>

    @FunctionProvider(BagFunctions::class)
    val functions: BagFunctions<T>
}

class BagProperties<T>(private val bag: Bag<T>) {
    val empty by property { mk_Bag<T>() }
}

class BagFunctions<T>(private val bag: Bag<T>) {

    val isEmpty = function(
        command = { -> bag.dom.isEmpty() },
    )

    val bset = function(
        command = { -> as_Set(bag.dom) },
        post = { result: Set<T> -> result == bag.dom }
    )

    val inbag = function(
        command = { t: T -> t in bag.functions.bset() },
        post = { t: T, result: Boolean -> result iff (t in bag.functions.bset()) }
    )

    val amount = function(
        command = { t: T -> if (bag.functions.inbag(t)) bag[t] else 0uL },
        post = { t: T, r: nat -> bag.functions.inbag(t) iff (t in bag.functions.bset() && r == bag[t])}
    )

    val count = function(
        command = { ->
            val inner = function(
                command = { t: T -> bag.functions.amount(t) }
            )
            inner
        }
    )

    val subbageq = function(
        command = { other: Bag<T> ->
            forall(bag.dom) { t ->
                other.functions.inbag(t) &&
                        (bag.functions.amount(t) <= other.functions.amount(t)) }
        },
        post = { other, result ->
            result iff
                    ((bag.dom subset other.dom) &&
                            forall(bag.dom)
                            { t -> bag.functions.inbag(t) <= other.functions.inbag(t) })
        }
    )

    val scale = function(
        command = { n: nat ->
            (if (n == 0UL) bag.properties.empty else
            as_Bag(set(bag.dom) { x -> mk_(x, n * bag[x]) }))
        },
        post = { n: nat, r: Bag<T> ->
            (r.dom == if (n == 0UL) emptySet() else bag.dom) &&
                    forall(bag.dom) { x -> r.functions.amount(x) == n * bag.functions.amount(x)}
        }
    )

    val bunion = function(
        command = { other: Bag<T> ->
            // unique to each + sum of common to both
            as_Bag(bag.domSubtract(other.dom) +
                    other.domSubtract(bag.dom) +
                    set(bag.dom inter  other.dom) { x -> mk_(x, bag[x] + other[x]) })
        },
        post = { other: Bag<T>, r: Bag<T> ->
            r.dom == bag.dom + other.dom &&
                    forall(bag.dom inter r.dom) { r.functions.amount(it) == bag.functions.amount(it) + other.functions.amount(it) } &&
                    forall(bag.dom - r.dom) { r.functions.amount(it) == bag.functions.amount(it) } &&
                    forall(other.dom - r.dom) { r.functions.amount(it) == other.functions.amount(it) }
        }
    )

    val bdiff = function(
        command = { other: Bag<T> ->
            as_Bag(bag.domSubtract(other.dom) +
                    set(bag.dom inter other.dom, filter = { x -> bag[x] > other[x] })
                    { x -> mk_(x, bag[x] - other[x]) })
        },
        post = { other: Bag<T>, r: Bag<T> ->
            forall(r.dom) { r.functions.amount(it) == max(0UL, bag.functions.amount(it) - other.functions.amount(it)) }
        }
    )

    val items = function(
        command = { s: Sequence<T> ->
            as_Bag(set(s.inds) { i -> mk_(s[i], s.filter { it == s[i] }.len) })
        },
        post = { s: Sequence<T>, r: Bag<T> ->
            s.elems == r.dom
        }
    )

//    val  image = function(command = { fn: ((T) -> nat) -> set(bag.dom) { x -> mk_(fn(x), bag[x]) } } )
    // Not sure here whether to have a lambda or VFunction input; can't be `val image` either way because of <U>
    fun <U> image() = function(
        command = { fn: VFunction1<T, U> ->
            as_Bag(set(bag.dom) { x -> mk_(fn(x), bag[x]) })
        },
        post = { fn, r ->
            r.dom == set(bag.dom) { x -> fn(x) } &&
                    forall(r.dom) { y ->
                        r.functions.amount(y) ==
                                bag.functions.amount(iota(bag.dom) { x -> fn(x) == y })
                    }
        }
    )

    val bsum = function(
        command = { -> bag.dom.map { bag[it] }.sum() }
    )
}