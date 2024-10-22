package com.anaplan.engineering.kazuki.toolkit.ISO8601

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.ISO8601.DTG_Module.mk_DTG
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Date_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Duration_Module.mk_Duration
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Interval_Module.mk_Interval
import kotlin.math.max
import kotlin.math.min


@Module
interface DTG : Comparable<DTG> {
    val date: Date
    val time: Time

    @ComparableProperty
    val duration_ms: Long get() = functions.toDuration().duration_ms

    override fun compareTo(other: DTG) = duration_ms.compareTo(other.duration_ms)

    @FunctionProvider(DTGFunctions::class)
    val functions: DTGFunctions


    class DTGFunctions(private val dtg: DTG) {

        val add: (Duration) -> DTG = function(
            command = { duration: Duration -> dtg.functions.toDuration().functions.add(duration).functions.toDTG() },
            post = { duration, result -> result.functions.subtract(duration) == dtg }
        )

        val subtract: (Duration) -> DTG = function(
            command = { duration -> durationDiff(dtg.functions.toDuration(), duration).functions.toDTG() },
            pre = { duration -> duration <= dtg.functions.toDuration() },
//        post = { duration, result -> result.functions.add(dur) == dtg }
        )

        val toDuration: () -> Duration = function<Duration>(
            command = { dtg.date.functions.toDuration().functions.add(dtg.time.functions.toDuration()) },
        )

        val within = function(
            command = { duration: Duration, target: DTG ->
                if (duration.duration_ms == 0L) {
                    dtg == target
                } else {
                    dtg.functions.inInterval(
                        mk_Interval(target.functions.subtract(duration), target.functions.add(duration))
                    )
                }
            },
            post = { duration, target, result -> (duration == NO_DURATION) implies ((dtg == target) == result) }
        )

        val inInterval: (Interval) -> bool = function(
            command = { interval: Interval -> dtg.functions.inRange(interval.begins, interval.ends) }
        )

        val inRange: (DTG, DTG) -> bool = function(
            command = { min: DTG, max: DTG -> min <= dtg && dtg < max }
        )

        val finestGranularity: (Duration) -> bool = function(
            command = { duration: Duration -> dtg.functions.toDuration().duration_ms % duration.duration_ms == 0L },
            pre = { duration -> duration != NO_DURATION }
        )

        val instant: () -> Interval = function(
            command = { mk_Interval(dtg, dtg.functions.add(ONE_MILLISECOND)) },
            post = { result -> dtg.functions.inInterval(result) }
        )

        val format: () -> String = function<String>(
            command = { dtg.date.functions.format() + "T" + dtg.time.functions.format() }
        )

        val addMonths: (nat) -> DTG = function(
            command = { n: nat ->
                val nextMonth = ((dtg.date.month + n - 1) % MONTHS_PER_YEAR) + 1
                val nextYear = dtg.date.year + (dtg.date.month + n - 1) / MONTHS_PER_YEAR
                if (dtg.date.day > daysInMonth(nextYear, nextMonth)) {
                    mk_DTG(mk_Date(nextYear, nextMonth, daysInMonth(nextYear, nextMonth)), dtg.time)
                } else {
                    mk_DTG(mk_Date(nextYear, nextMonth, dtg.date.day), dtg.time)
                }
            },
        )

        val subtractMonths: (nat) -> DTG = function(
            command = { n: nat ->
                val nextMonth = (dtg.date.month - n - 1).mod(MONTHS_PER_YEAR) + 1
                val nextYear = dtg.date.year + (dtg.date.month - n - 12) / MONTHS_PER_YEAR
                if (dtg.date.day > daysInMonth(nextYear, nextMonth)) {
                    mk_DTG(mk_Date(nextYear, nextMonth, daysInMonth(nextYear, nextMonth)), dtg.time)
                } else {
                    mk_DTG(mk_Date(nextYear, nextMonth, dtg.date.day), dtg.time)
                }
            }
        )


    }
}

@Module
interface DtgInZone : Comparable<DtgInZone> {
    val date: Date
    val time: TimeInZone

    @Invariant
    fun dtgBeforeFirstDate() = !(date == FirstDate && time.functions.normalise()._2 == PlusOrMinus.Plus)

    @Invariant
    fun dtgAfterLastDate() = !(date == LastDate && time.functions.normalise()._2 == PlusOrMinus.Minus)

    @ComparableProperty
    val duration_ms: Long get() = functions.normalise().functions.toDuration().duration_ms

    override fun compareTo(other: DtgInZone) = duration_ms.compareTo(other.duration_ms)


    @FunctionProvider(DtgInZoneFunctions::class)
    val functions: DtgInZoneFunctions

    class DtgInZoneFunctions(private val dtgInZone: DtgInZone) {

        val normalise: () -> DTG = function<DTG>(
            command = {
                val normalisedTime = dtgInZone.time.functions.normalise()
                val baseDTG = mk_DTG(dtgInZone.date, normalisedTime._1)
                when (normalisedTime._2) {
                    PlusOrMinus.Plus -> baseDTG.functions.subtract(ONE_DAY)
                    PlusOrMinus.Minus -> baseDTG.functions.add(ONE_DAY)
                    PlusOrMinus.None -> baseDTG
                }
            }
        )

        val format: () -> String = function<String>(
            command = { dtgInZone.date.functions.format() + "T" + dtgInZone.time.functions.format() }
        )
    }
}

@Module
interface Interval {
    val begins: DTG
    val ends: DTG

    @Invariant
    fun zeroSizeInterval() = begins != ends

    @Invariant
    fun beginAfterEnd() = begins <= ends

    @FunctionProvider(IntervalFunctions::class)
    val functions: IntervalFunctions

    class IntervalFunctions(private val interval: Interval) {
        val within: (Interval) -> bool = function(
            command = { containerInterval: Interval ->
                containerInterval.begins <= interval.begins &&
                        interval.ends <= containerInterval.ends
            },
            post = { containerInterval, result ->
                result == (containerInterval.begins <= interval.begins
                        && interval.ends <= containerInterval.ends)
            }
        )
        val overlap: (Interval) -> bool = function(
            command = { other: Interval ->
                other.begins < interval.ends &&
                        interval.begins < other.ends
            },
            post = { other, result ->
                result == exists(
                    max(interval.begins.duration_ms, other.begins.duration_ms)..
                            min(interval.ends.duration_ms, other.ends.duration_ms)
                ) {
                    mk_Duration(it).functions.toDTG().functions.inInterval(interval)
                            && mk_Duration(it).functions.toDTG().functions.inInterval(other)
                }
            } // todo replace post condition here
        )
        val toDuration: () -> Duration = function(
            command = { diff(interval.begins, interval.ends) },
            post = { result -> interval.begins.functions.add(result) == interval.ends }
        )
        val finestGranularityI: (Duration) -> bool = function(
            command = { duration: Duration ->
                interval.begins.functions.finestGranularity(duration) &&
                        interval.ends.functions.finestGranularity(duration)
            }
        )

        val format: () -> String = function<String>(
            command = { interval.begins.functions.format() + "/" + interval.ends.functions.format() }
        )
    }
}

val diff: (DTG, DTG) -> Duration = function(
    command = { dtg1: DTG, dtg2: DTG -> durationDiff(dtg1.functions.toDuration(), dtg2.functions.toDuration()) }
)

val minDTG: (Set1<DTG>) -> DTG = function(
    command = { dtgs: Set1<DTG> -> (set(dtgs) { it }).min() },
    post = { dtgs, result -> result in dtgs && forall(dtgs) { result <= it } }
)
val maxDTG: (Set1<DTG>) -> DTG = function(
    command = { dtgs: Set1<DTG> -> (set(dtgs) { it }).max() },
    post = { dtgs, result -> result in dtgs && forall(dtgs) { result >= it } }
)

val monthsBetween: (DTG, DTG) -> nat = function(
    command = { starts: DTG, ends: DTG ->
        MONTHS_PER_YEAR * yearsBetween(starts, ends) + (if (ends.date.month < starts.date.month) 12 else 0) +
                if (ends.date.day >= starts.date.day) {
                    ends.date.month - starts.date.month
                } else {
                    ends.date.month - starts.date.month - 1
                }
    },
    pre = { starts, ends -> starts <= ends }
)

val yearsBetween: (DTG, DTG) -> nat = function(
    command = { starts: DTG, ends: DTG ->
        if (Duration.durationUpToMonth(starts.date.year, starts.date.month).duration_ms
            + Duration.fromDays(starts.date.day.toLong()).duration_ms <=
            Duration.durationUpToMonth(ends.date.year, ends.date.month).duration_ms
            + Duration.fromDays(ends.date.day.toLong()).duration_ms
        ) {
            ends.date.year - starts.date.year
        } else {
            ends.date.year - starts.date.year - 1
        }
    },
    pre = { starts, ends -> starts <= ends }
)
