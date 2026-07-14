package com.anaplan.engineering.kazuki.usingz.boxoffice

import com.anaplan.engineering.kazuki.core.Output
import com.anaplan.engineering.kazuki.core.SchematicFunction

interface Success : SchematicFunction<Response> {

    @Output
    val response: Response

    override fun command() = Response.okay

    override fun post(result: Response) = result == Response.okay
}