package com.anaplan.engineering.kazuki.core

@Module
interface Record {
    val a: Int
}

@Module
interface RecordExtension: Record {
    val b: String
}

@Module
interface RecordInvOnlyExtension: Record {
    @Invariant
    fun notZero() = a != 0
}

@Module
interface OtherRecord {
    val a: Int
}

@Module
interface GenericRecord<T> {
    val a: T
}

@Module
interface GenericRecordExtension<T> : GenericRecord<T> {
    val b: Set<T>
}

@Module
interface GenericRecordInvOnlyExtension : GenericRecord<Int> {
    @Invariant
    fun notZero() = a != 0
}

@Module
interface OtherGenericRecord<T> {
    val a: T
}

// TODO - incorrect ordering
//@Module
//interface RecordDblExtension: RecordExtension, Tuple3<Int, String, Double> {
//    val c: Double
//}

