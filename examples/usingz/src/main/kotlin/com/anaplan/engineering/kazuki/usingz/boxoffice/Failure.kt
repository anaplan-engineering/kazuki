package com.anaplan.engineering.kazuki.usingz.boxoffice

import com.anaplan.engineering.kazuki.core.Output
import com.anaplan.engineering.kazuki.core.SchematicFunction

interface Failure : SchematicFunction<Response> {

    @Output
    val response: Response

    override fun command() = Response.sorry

    override fun post(result: Response) = result == Response.sorry
}