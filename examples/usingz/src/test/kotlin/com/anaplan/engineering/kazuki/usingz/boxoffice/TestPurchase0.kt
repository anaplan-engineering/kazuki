package com.anaplan.engineering.kazuki.usingz.boxoffice

import com.anaplan.engineering.kazuki.core.mk_
import com.anaplan.engineering.kazuki.core.mk_InjectiveMapping
import com.anaplan.engineering.kazuki.core.set
import com.anaplan.engineering.kazuki.usingz.boxoffice.BoxOffice_Module.mk_BoxOffice
import com.anaplan.engineering.kazuki.usingz.boxoffice.Customer_Module.mk_Customer
import kotlin.test.Test
import kotlin.test.assertEquals

class TestPurchase0 {

    @Test
    fun purchase0() {
        val seats = set(0uL..50uL) { Seat_Module.mk_Seat(it) }
        val old = mk_BoxOffice(seats, mk_InjectiveMapping())
        val jim = mk_Customer("Jim".toName())
        val seat23 = Seat_Module.mk_Seat(23uL)
        val new = Purchase0(seat23, jim, old)
        assertEquals(seats, new.seating)
        assertEquals(mk_InjectiveMapping(mk_(seat23, jim)), new.sold)
    }
}