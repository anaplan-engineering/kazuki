package com.anaplan.engineering.kazuki.core

annotation class GHGHS

@GHGHS
@Module
interface Record {
    val a: Int
}

@Module
interface RecordExtension: Record {
    val b: String
}


class RE(private val r: Record, override val b: String): RecordExtension, Record by r