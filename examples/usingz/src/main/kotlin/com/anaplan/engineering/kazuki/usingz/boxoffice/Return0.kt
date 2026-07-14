package com.anaplan.engineering.kazuki.usingz.boxoffice

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.usingz.boxoffice.BoxOffice_Module.transform

interface Return0 : SchematicFunction<BoxOffice> {

    @Input
    val s: Seat

    @Input
    val c: Customer

    @Delta
    val boxOffice: BoxOffice

    override fun command() = boxOffice.transform(sold = boxOffice.sold - mk_(s, c))

    override fun pre() = mk_(s, c) in boxOffice.sold

    override fun post(result: BoxOffice) =
        result.sold == boxOffice.sold - mk_Set(mk_(s, c))
                && result.seating == boxOffice.seating

}