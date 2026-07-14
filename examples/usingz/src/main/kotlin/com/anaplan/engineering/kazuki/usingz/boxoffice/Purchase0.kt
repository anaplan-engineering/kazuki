package com.anaplan.engineering.kazuki.usingz.boxoffice

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.usingz.boxoffice.BoxOffice_Module.transform

interface Purchase0 : SchematicFunction<BoxOffice> {

    @Input
    val s: Seat

    @Input
    val c: Customer

    @Delta
    val boxOffice: BoxOffice

    override fun command() =
        boxOffice.transform(sold = boxOffice.sold + mk_(s, c))

    override fun pre() = s in boxOffice.seating &&
            s !in boxOffice.sold.dom

    override fun post(result: BoxOffice) =
        result.sold == boxOffice.sold union mk_Set(mk_(s, c))
                && result.seating == boxOffice.seating

}