package com.anaplan.engineering.kazuki.core

interface Tuple: PrettyPrintable

// TODO -- token type?
object Tuple0: Tuple

fun mk_() = Tuple0