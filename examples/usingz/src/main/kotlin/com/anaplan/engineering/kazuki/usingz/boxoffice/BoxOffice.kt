package com.anaplan.engineering.kazuki.usingz.boxoffice

import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.card
import com.anaplan.engineering.kazuki.core.property

@Module
interface BoxOffice : System<Seat, Customer> {

    val properties: BoxOfficeProperties

    val functions: BoxOfficeFunctions
}

class BoxOfficeFunctions(boxOffice: BoxOffice) {

    val purchase = Purchase(boxOffice)

    val returnSeat = Return(boxOffice)

}

class BoxOfficeProperties(boxOffice: BoxOffice) {

    val seatsFree by property { boxOffice.seating.card }
}