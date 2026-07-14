package com.anaplan.engineering.kazuki.usingz.boxoffice

import com.anaplan.engineering.kazuki.core.mk_
import com.anaplan.engineering.kazuki.core.mk_InjectiveMapping
import com.anaplan.engineering.kazuki.core.set
import kotlin.test.Test
import kotlin.test.assertEquals

class TestBoxOffice {

    @Test
    fun purchase() {
        val seats = set(0uL..50uL) { Seat_Module.mk_Seat(it) }
        val bo1 = BoxOffice_Module.mk_BoxOffice(seats, mk_InjectiveMapping())
        val jim = Customer_Module.mk_Customer("Jim".toName())
        val seat23 = Seat_Module.mk_Seat(23uL)
        val (bo2, r2) = bo1.functions.purchase(seat23, jim)
        assertEquals(seats, bo2.seating)
        assertEquals(mk_InjectiveMapping(mk_(seat23, jim)), bo2.sold)
        assertEquals(Response.okay, r2)
        val (bo3, r3) = bo2.functions.purchase(seat23, jim)
        assertEquals(seats, bo3.seating)
        assertEquals(mk_InjectiveMapping(mk_(seat23, jim)), bo3.sold)
        assertEquals(Response.sorry, r3)
    }

    @Test
    fun returnSeat() {
        val seats = set(0uL..50uL) { Seat_Module.mk_Seat(it) }
        val bo1 = BoxOffice_Module.mk_BoxOffice(seats, mk_InjectiveMapping())
        val jim = Customer_Module.mk_Customer("Jim".toName())
        val seat23 = Seat_Module.mk_Seat(23uL)
        val (bo2, r2) = bo1.functions.purchase(seat23, jim)
        assertEquals(seats, bo2.seating)
        assertEquals(mk_InjectiveMapping(mk_(seat23, jim)), bo2.sold)
        assertEquals(Response.okay, r2)
        val (bo3, r3) = bo2.functions.returnSeat(seat23, jim)
        assertEquals(seats, bo3.seating)
        assertEquals(mk_InjectiveMapping(), bo3.sold)
        assertEquals(Response.okay, r3)
        val (bo4, r4) = bo3.functions.returnSeat(seat23, jim)
        assertEquals(seats, bo4.seating)
        assertEquals(mk_InjectiveMapping(), bo4.sold)
        assertEquals(Response.sorry, r4)
    }
}