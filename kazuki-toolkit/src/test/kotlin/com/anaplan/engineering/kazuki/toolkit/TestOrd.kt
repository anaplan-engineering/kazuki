package com.anaplan.engineering.kazuki.toolkit

import com.anaplan.engineering.kazuki.toolkit.OrderedSet_Module.mk_OrderedSet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration

class TestOrd {

    @Test
    fun natural_min() {
        assertEquals(2, Ord.natural<Int>().min(2, 2))
        assertEquals("a", Ord.natural<String>().min("b", "a"))
        assertEquals(Duration.ZERO, Ord.natural<Duration>().min(Duration.ZERO, Duration.INFINITE))
    }

    @Test
    fun natural_min_post() {
        assertEquals(false, Ord.natural<Int>().min.post(2, 2, 3))
        assertEquals(false, Ord.natural<String>().min.post("b", "a", "b"))
        assertEquals(false, Ord.natural<Duration>().min.post(Duration.ZERO, Duration.INFINITE, Duration.INFINITE))
    }

    @Test
    fun natural_max() {
        assertEquals(2, Ord.natural<Int>().max(2, 2))
        assertEquals("b", Ord.natural<String>().max("b", "a"))
        assertEquals(Duration.INFINITE, Ord.natural<Duration>().max(Duration.ZERO, Duration.INFINITE))
    }

    @Test
    fun natural_max_post() {
        assertEquals(false, Ord.natural<Int>().max.post(2, 2, 3))
        assertEquals(false, Ord.natural<String>().max.post("b", "a", "a"))
        assertEquals(false, Ord.natural<Duration>().max.post(Duration.ZERO, Duration.INFINITE, Duration.ZERO))
    }

    @Test
    fun natural_compare() {
        assertEquals(Ord.EQ, Ord.natural<Int>().compare(2, 2))
        assertEquals(Ord.GT, Ord.natural<String>().compare("b", "a"))
        assertEquals(Ord.LT, Ord.natural<Duration>().compare(Duration.ZERO, Duration.INFINITE))
    }

    @Test
    fun natural_compare_post() {
        assertEquals(false, Ord.natural<Int>().compare.post(2, 2, Ord.LT))
        assertEquals(false, Ord.natural<String>().compare.post("b", "a", Ord.EQ))
        assertEquals(false, Ord.natural<Duration>().compare.post(Duration.ZERO, Duration.INFINITE, Ord.GT))
    }

    @Test
    fun bySeq_min() {
        val order = mk_OrderedSet(4, 1, 5)
        assertEquals(4, Ord.bySeq(order).min(1, 4))
        assertEquals(1, Ord.bySeq(order).min(1, 1))
        assertEquals(1, Ord.bySeq(order).min(1, 5))
    }

    @Test
    fun bySeq_min_pre() {
        val order = mk_OrderedSet(4, 1, 5)
        assertEquals(false, Ord.bySeq(order).min.pre(2, 4))
        assertEquals(false, Ord.bySeq(order).min.pre(1, 6))
    }

    @Test
    fun bySeq_min_post() {
        val order = mk_OrderedSet(4, 1, 5)
        assertEquals(false, Ord.bySeq(order).min.post(1, 4, 1))
        assertEquals(false, Ord.bySeq(order).min.post(1, 1, 2))
        assertEquals(false, Ord.bySeq(order).min.post(1, 5, 5))
    }
    
    @Test
    fun bySeq_compare() {
        val order = mk_OrderedSet(4, 1, 5)
        assertEquals(Ord.GT, Ord.bySeq(order).compare(1, 4))
        assertEquals(Ord.EQ, Ord.bySeq(order).compare(1, 1))
        assertEquals(Ord.LT, Ord.bySeq(order).compare(1, 5))
    }

    @Test
    fun bySeq_compare_pre() {
        val order = mk_OrderedSet(4, 1, 5)
        assertEquals(false, Ord.bySeq(order).compare.pre(2, 4))
        assertEquals(false, Ord.bySeq(order).compare.pre(1, 6))
    }

    @Test
    fun bySeq_compare_post() {
        val order = mk_OrderedSet(4, 1, 5)
        assertEquals(false, Ord.bySeq(order).compare.post(1, 4, Ord.EQ))
        assertEquals(false, Ord.bySeq(order).compare.post(1, 1, Ord.LT))
        assertEquals(false, Ord.bySeq(order).compare.post(1, 5, Ord.GT))
    }
}