package com.anaplan.engineering.kazuki.toolkit.examples

import com.anaplan.engineering.kazuki.core.function
import com.anaplan.engineering.kazuki.core.mk_
import com.anaplan.engineering.kazuki.toolkit.Bag_Module.mk_Bag
import org.junit.Test
import kotlin.test.assertEquals

class TestBag {

    val b1 = mk_Bag(mk_("x", 1UL), mk_("y", 2UL))
    val b2 = mk_Bag(mk_("x", 4UL), mk_("z", 1UL))
    val b3 = mk_Bag(mk_("x", 5UL), mk_("y", 2UL), mk_("z", 1UL))

    @Test
    fun test_isEmpty() {
        assert(!b1.functions.isEmpty())

        assert(mk_Bag<String>().functions.isEmpty())
    }

    @Test
    fun test_bset() {
        assertEquals(b1.functions.bset(), setOf("x", "y"))
        assertEquals(b2.functions.bset(), setOf("x", "z"))
        assertEquals(b3.functions.bset(), setOf("x", "y", "z"))
    }

    @Test
    fun test_inbag() {
        // in
        assert(b1.functions.inbag("x"))
        assert(b1.functions.inbag("y"))
        assert(b3.functions.inbag("z"))

        // out
        assert(!b1.functions.inbag("z"))
    }

    @Test
    fun test_amount() {
        // counted
        assertEquals(b1.functions.amount("x"), 1UL)
        assertEquals(b2.functions.amount("x"), 4UL)
        assertEquals(b3.functions.amount("x"), 5UL)

        // out of scope
        assertEquals(b1.functions.amount("z"), 0UL)
    }

    @Test
    fun test_subbageq() {
        // itself
        assert(b1.functions.subbageq(b1))

        // others
        assert(b1.functions.subbageq(b3))
        assert(b2.functions.subbageq(b3))

        // not
        assert(!b2.functions.subbageq(b1))
        assert(!b3.functions.subbageq(b1))
    }

    @Test
    fun test_scale() {
        // scale
        assertEquals(b1.functions.scale(10UL),
            mk_Bag(mk_("x", 10UL), mk_("y", 20UL)))

        // scale 0
        assert(b1.functions.scale(0UL).isEmpty())
    }

    @Test
    fun test_bunion() {
        assertEquals(b1.functions.bunion(b2), b3)
    }

    @Test
    fun test_bdiff() {
        assertEquals(b3.functions.bdiff(b2), b1)
    }

    @Test
    fun test_count() {
        val c = b1.functions.count()

        // in
        assertEquals(c("x"), 1UL)
        assertEquals(c("y"), 2UL)

        // out
        assertEquals(c("z"), 0UL)
    }

    @Test
    fun test_image() {
        // bag mapping; simplify?
        val codePointAt0 = function<String, Int>(
            command = { s -> s.codePointAt(0) }
        )
        val image = b1.functions.image<Int>()(codePointAt0)

        assertEquals(
            mk_Bag(mk_("x".codePointAt(0), 1UL),
                mk_("y".codePointAt(0), 2UL)),
            image
        )
    }

    @Test
    fun test_sum() {
        // non-empty sum
        assertEquals(b1.functions.bsum(), 3UL)
        assertEquals(b2.functions.bsum(), 5UL)
        assertEquals(b3.functions.bsum(), 8UL)

        // empty sum
        assertEquals(mk_Bag<String>().functions.bsum(), 0UL)
    }

    // TODO Specification tests for bag are mostly repetitions; remove?
    @Test
    fun test_isEmpty_post() {
        assert(b1.functions.isEmpty.post(false))
        assert(mk_Bag<String>().functions.isEmpty.post(true))
    }

    @Test
    fun test_bset_post() {
        assert(b1.functions.bset.post(setOf("x", "y")))
        assert(b2.functions.bset.post(setOf("x", "z")))
        assert(b3.functions.bset.post(setOf("x", "y", "z")))
    }

    @Test
    fun test_inbag_post() {
        // in
        assert(b1.functions.inbag.post("x", true))
        assert(b1.functions.inbag.post("y", true))
        assert(b3.functions.inbag.post("z", true))

        // out
        assert(b1.functions.inbag.post("z", false))
    }

    @Test
    fun test_amount_post() {
        // counted
        assert(b1.functions.amount.post("x", 1UL))
        assert(b2.functions.amount.post("x", 4UL))
        assert(b3.functions.amount.post("x", 5UL))

        // out of scope
        assert(b1.functions.amount.post("z", 0UL))
    }

    @Test
    fun test_subbageq_post() {
        // itself
        assert(b1.functions.subbageq.post(b1, true))

        // others
        assert(b1.functions.subbageq.post(b3, true))
        assert(b2.functions.subbageq.post(b3, true))

        // not
        assert(b2.functions.subbageq.post(b1, false))
        assert(b3.functions.subbageq.post(b1, false))
    }

    @Test
    fun test_scale_post() {
        // scale
        assert(b1.functions.scale.post(
            10UL,
            mk_Bag(mk_("x", 10UL), mk_("y", 20UL))
        ))

        // scale 0
        assert(b1.functions.scale.post(0UL, mk_Bag()))
    }

    @Test
    fun test_bunion_post() {
        assert(b1.functions.bunion.post(b2, b3))
    }

    @Test
    fun test_bdiff_post() {
        assert(b3.functions.bdiff.post(b2, b1))
    }

    @Test
    fun test_count_post() {
        val c = b1.functions.count()

        assert(b1.functions.count.post(c))
    }

    @Test
    fun test_sum_post() {
        // non-empty sum
        assert(b1.functions.bsum.post(3UL))
        assert(b2.functions.bsum.post(5UL))
        assert(b3.functions.bsum.post(8UL))

        // empty sum
        assert(mk_Bag<String>().functions.bsum.post(0UL))
    }
}