package com.anaplan.engineering.kazuki.usingz.boxoffice

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.internal.*

/**
 * THIS FILE CONTAINS A MOCK UP OF THE CODE THAT KAZUKI C/SHOULD GENERATE FOR SCHEMATIC FUNCTIONS
 * -- IT IS CURRENTLY HANDWRITTEN AND INCOMPLETE
 */

// TODO -- prob can't make things private as will need to refer to schema cross file

private class Purchase0_SchFun(
    @_Input
    override val s: Seat,
    @_Input
    override val c: Customer,
    @_Delta(pos = 1)
    override val boxOffice: BoxOffice
) : Purchase0 {
    override fun invoke(): BoxOffice =
        VFunction0(
            command = ::command,
            pre = ::pre,
            post = ::post,
            postCommand = ::postCommand,
            measure = { 0uL },
            cache = false,
            logAs = null,
        ).invoke()
}

private class Return0_SchFun(
    @_Input
    override val s: Seat,
    @_Input
    override val c: Customer,
    @_Delta(pos = 1)
    override val boxOffice: BoxOffice
) : Return0 {
    override fun invoke(): BoxOffice =
        VFunction0(
            command = ::command,
            pre = ::pre,
            post = ::post,
            postCommand = ::postCommand,
            measure = { 0uL },
            cache = false,
            logAs = null,
        ).invoke()
}

private class NotAvailable0_SchFun(
    @_Input
    override val s: Seat,
    @_Input
    override val c: Customer,
    @_Xi(pos = 1)
    override val boxOffice: BoxOffice
) : NotAvailable0 {

    // Autogenerate for Xi
    override fun post(result: BoxOffice): Boolean =
        super.post(result) && boxOffice == result

    override fun invoke(): BoxOffice =
        VFunction0(
            command = ::command,
            pre = ::pre,
            post = ::post,
            postCommand = ::postCommand,
            measure = { 0uL },
            cache = false,
            logAs = null,
        ).invoke()
}

private class NotPossible0_SchFun(
    @_Input
    override val s: Seat,
    @_Input
    override val c: Customer,
    @_Xi(pos = 1)
    override val boxOffice: BoxOffice
) : NotPossible0 {

    // Autogenerate for Xi
    override fun post(result: BoxOffice): Boolean =
        super.post(result) && boxOffice == result

    override fun invoke(): BoxOffice =
        VFunction0(
            command = ::command,
            pre = ::pre,
            post = ::post,
            postCommand = ::postCommand,
            measure = { 0uL },
            cache = false,
            logAs = null,
        ).invoke()
}

class Success_SchFun : Success {

    @_Output(pos = 1)
    override val response: Response
        get() = unreachable()

    override fun invoke(): Response =
        VFunction0(
            command = ::command,
            pre = ::pre,
            post = ::post,
            postCommand = ::postCommand,
            measure = { 0uL },
            cache = false,
            logAs = null,
        ).invoke()
}

private class Failure_SchFun : Failure {

    @_Output(pos = 1)
    override val response: Response
        get() = unreachable()

    override fun invoke(): Response =
        VFunction0(
            command = ::command,
            pre = ::pre,
            post = ::post,
            postCommand = ::postCommand,
            measure = { 0uL },
            cache = false,
            logAs = null,
        ).invoke()
}

private class Purchase1_SchFun(
    @_Input
    val s: Seat,
    @_Input
    val c: Customer,
    @_Delta(pos = 1)
    val boxOffice: BoxOffice
) : _SchematicConjunction<BoxOffice, Response, Tuple2<BoxOffice, Response>>() {

    @_Output(pos = 2)
    val response: Response
        get() = unreachable()


    override val l = Purchase0_SchFun(s, c, boxOffice)
    override val r = Success_SchFun()

    override fun buildOutput(o1: BoxOffice, o2: Response) = mk_(o1, o2)

    override fun invoke(): Tuple2<BoxOffice, Response> =
        VFunction0(
            command = ::command,
            pre = ::pre,
            post = ::post,
            postCommand = ::postCommand,
            measure = { 0uL },
            cache = false,
            logAs = null,
        ).invoke()

}

private class Return1_SchFun(
    @_Input
    val s: Seat,
    @_Input
    val c: Customer,
    @_Delta(pos = 1)
    val boxOffice: BoxOffice
) : _SchematicConjunction<BoxOffice, Response, Tuple2<BoxOffice, Response>>() {

    @_Output(pos = 2)
    val response: Response
        get() = unreachable()


    override val l = Return0_SchFun(s, c, boxOffice)
    override val r = Success_SchFun()

    override fun buildOutput(o1: BoxOffice, o2: Response) = mk_(o1, o2)

    override fun invoke(): Tuple2<BoxOffice, Response> =
        VFunction0(
            command = ::command,
            pre = ::pre,
            post = ::post,
            postCommand = ::postCommand,
            measure = { 0uL },
            cache = false,
            logAs = null,
        ).invoke()

}

private class NotAvailable1_SchFun(
    @_Input
    val s: Seat,
    @_Input
    val c: Customer,
    @_Delta(pos = 1)
    val boxOffice: BoxOffice
) : _SchematicConjunction<BoxOffice, Response, Tuple2<BoxOffice, Response>>() {

    @_Output(pos = 2)
    val response: Response
        get() = unreachable()


    override val l = NotAvailable0_SchFun(s, c, boxOffice)
    override val r = Failure_SchFun()

    override fun buildOutput(o1: BoxOffice, o2: Response) = mk_(o1, o2)

    override fun invoke(): Tuple2<BoxOffice, Response> =
        VFunction0(
            command = ::command,
            pre = ::pre,
            post = ::post,
            postCommand = ::postCommand,
            measure = { 0uL },
            cache = false,
            logAs = null,
        ).invoke()

}

private class NotPossible1_SchFun(
    @_Input
    val s: Seat,
    @_Input
    val c: Customer,
    @_Delta(pos = 1)
    val boxOffice: BoxOffice
) : _SchematicConjunction<BoxOffice, Response, Tuple2<BoxOffice, Response>>() {

    @_Output(pos = 2)
    val response: Response
        get() = unreachable()


    override val l = NotPossible0_SchFun(s, c, boxOffice)
    override val r = Failure_SchFun()

    override fun buildOutput(o1: BoxOffice, o2: Response) = mk_(o1, o2)

    override fun invoke(): Tuple2<BoxOffice, Response> =
        VFunction0(
            command = ::command,
            pre = ::pre,
            post = ::post,
            postCommand = ::postCommand,
            measure = { 0uL },
            cache = false,
            logAs = null,
        ).invoke()

}

private class Purchase_SchFun(
    @_Input
    val s: Seat,
    @_Input
    val c: Customer,
    @_Delta(pos = 1)
    val boxOffice: BoxOffice
) : _SchematicDisjunction<Tuple2<BoxOffice, Response>, Tuple2<BoxOffice, Response>, Tuple2<BoxOffice, Response>>() {

    @_Output(pos = 2)
    val response: Response
        get() = unreachable()

    override val l = Purchase1_SchFun(s, c, boxOffice)
    override val r = NotAvailable1_SchFun(s, c, boxOffice)

    override fun buildOutput(
        o1: Tuple2<BoxOffice, Response>?,
        o2: Tuple2<BoxOffice, Response>?
    ) = o1 ?: o2!!

    override fun invoke(): Tuple2<BoxOffice, Response> =
        VFunction0(
            command = ::command,
            pre = ::pre,
            post = ::post,
            postCommand = ::postCommand,
            measure = { 0uL },
            cache = false,
            logAs = null,
        ).invoke()

}
private class Return_SchFun(
    @_Input
    val s: Seat,
    @_Input
    val c: Customer,
    @_Delta(pos = 1)
    val boxOffice: BoxOffice
) : _SchematicDisjunction<Tuple2<BoxOffice, Response>, Tuple2<BoxOffice, Response>, Tuple2<BoxOffice, Response>>() {

    @_Output(pos = 2)
    val response: Response
        get() = unreachable()

    override val l = Return1_SchFun(s, c, boxOffice)
    override val r = NotPossible1_SchFun(s, c, boxOffice)

    override fun buildOutput(
        o1: Tuple2<BoxOffice, Response>?,
        o2: Tuple2<BoxOffice, Response>?
    ) = o1 ?: o2!!

    override fun invoke(): Tuple2<BoxOffice, Response> =
        VFunction0(
            command = ::command,
            pre = ::pre,
            post = ::post,
            postCommand = ::postCommand,
            measure = { 0uL },
            cache = false,
            logAs = null,
        ).invoke()

}

private fun _Purchase0(s: Seat, c: Customer, boxOffice: BoxOffice) = Purchase0_SchFun(s, c, boxOffice)
private fun _Purchase1(s: Seat, c: Customer, boxOffice: BoxOffice) = Purchase1_SchFun(s, c, boxOffice)
private fun _Purchase(s: Seat, c: Customer, boxOffice: BoxOffice) = Purchase_SchFun(s, c, boxOffice)
private fun _Return(s: Seat, c: Customer, boxOffice: BoxOffice) = Return_SchFun(s, c, boxOffice)


fun Purchase1.pre(s: Seat, c: Customer, boxOffice: BoxOffice) = _Purchase1(s, c, boxOffice).pre()
fun Purchase.pre(s: Seat, c: Customer, boxOffice: BoxOffice) = _Purchase(s, c, boxOffice).pre()

fun Purchase0(s: Seat, c: Customer, boxOffice: BoxOffice) = _Purchase0(s, c, boxOffice).invoke()
fun Purchase1(s: Seat, c: Customer, boxOffice: BoxOffice) = _Purchase1(s, c, boxOffice).invoke()
fun Purchase(s: Seat, c: Customer, boxOffice: BoxOffice) = _Purchase(s, c, boxOffice).invoke()
fun Return(s: Seat, c: Customer, boxOffice: BoxOffice) = _Return(s, c, boxOffice).invoke()

// Auto create for xi/delta vals
fun Purchase(boxOffice: BoxOffice) = function(
    command = { s: Seat, c: Customer -> Purchase(s, c, boxOffice) }
)
fun Return(boxOffice: BoxOffice) = function(
    command = { s: Seat, c: Customer -> Return(s, c, boxOffice) }
)