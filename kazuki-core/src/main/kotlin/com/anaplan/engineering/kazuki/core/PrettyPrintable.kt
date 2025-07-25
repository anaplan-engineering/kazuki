package com.anaplan.engineering.kazuki.core

import com.anaplan.engineering.kazuki.core.internal._prettyOrDefault

interface PrettyPrintable {
    fun pretty(): String = toString()
}

fun Any?.prettyOrDefault() = _prettyOrDefault(this)

