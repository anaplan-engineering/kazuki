package com.anaplan.engineering.kazuki.core

interface PrettyPrintable {
    fun pretty(): String = toString()
}

fun Any?.prettyOrDefault() =
    if (this is PrettyPrintable) {
        this.pretty()
    } else {
        this.toString()
    }