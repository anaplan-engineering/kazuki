package com.anaplan.engineering.kazuki.core

@Module
interface Record {
    val a: Int

    @FunctionProvider(RecordFunctions::class)
    val functions: RecordFunctions
}

open class RecordFunctions(private val r: Record) {
    open val mutateA = function<Int>(command = { r.a * 2 })
}

@Module
interface RecordExtension : Record {
    val b: String
}

@Module
interface RecordExtensionAlternate : Record {
    val b: Int

    @FunctionProvider(RecordExtensionAlternateFunctions::class)
    override val functions: RecordExtensionAlternateFunctions
}

open class RecordExtensionAlternateFunctions(private val r: RecordExtensionAlternate) : RecordFunctions(r) {
    override val mutateA = function<Int>(command = { r.a * r.b })
}

@Module
interface RecordInvOnlyExtension : Record {
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

