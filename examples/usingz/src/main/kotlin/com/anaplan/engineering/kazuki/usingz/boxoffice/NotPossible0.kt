package com.anaplan.engineering.kazuki.usingz.boxoffice

import com.anaplan.engineering.kazuki.core.*

interface NotPossible0 : SchematicFunction<BoxOffice> {

    @Input
    val s: Seat

    @Input
    val c: Customer

    @Xi
    val boxOffice: BoxOffice

    override fun command() = boxOffice

    override fun pre() = mk_(s, c) !in boxOffice.sold

}