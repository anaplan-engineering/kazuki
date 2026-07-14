package com.anaplan.engineering.kazuki.usingz.boxoffice

import com.anaplan.engineering.kazuki.core.Input
import com.anaplan.engineering.kazuki.core.SchematicFunction
import com.anaplan.engineering.kazuki.core.Xi

interface NotAvailable0 : SchematicFunction<BoxOffice> {

    @Input
    val s: Seat

    @Input
    val c: Customer

    @Xi
    val boxOffice: BoxOffice

    override fun command() = boxOffice

    override fun pre() = s !in boxOffice.seating || s in boxOffice.sold.dom


}