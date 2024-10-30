package com.anaplan.engineering.kazuki.toolkit.ISO8601

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Dtg_Module.mk_Dtg
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Interval_Module.mk_Interval

@Module
interface Dtg : Comparable<Dtg> {
    val date: Date
    val time: Time

    private val dtgComparator get() = compareBy<Dtg> { it.date }.thenBy { it.time }
    override fun compareTo(other: Dtg) = dtgComparator.compare(this, other)

    @FunctionProvider(DtgFunctions::class)
    val functions: DtgFunctions

    class DtgFunctions(private val dtg: Dtg) {

        val addDuration: (Duration) -> Dtg = function(
            command = { duration -> dtg.functions.toDuration().functions.addDuration(duration).functions.toDtg() },
            post = { duration, result -> result.functions.subtractDuration(duration) == dtg }
        )

        val subtractDuration: (Duration) -> Dtg = function(
            command = { duration -> dtg.functions.toDuration().functions.subtractDuration(duration).functions.toDtg() },
            pre = { duration -> duration <= dtg.functions.toDuration() },
//        post = { duration, result -> result.functions.addDuration(duration) == dtg }
        )

        val toDuration: () -> Duration = function<Duration>(
            command = { dtg.date.functions.toDuration().functions.addDuration(dtg.time.functions.toDuration()) },
        )

        val within: (Duration, Dtg) -> bool = function(
            command = { duration, targetDtg ->
                if (duration.milliseconds == 0L) {
                    dtg == targetDtg
                } else {
                    dtg.functions.inInterval(
                        mk_Interval(
                            targetDtg.functions.subtractDuration(duration),
                            targetDtg.functions.addDuration(duration)
                        )
                    )
                }
            },
            post = { duration, targetDtg, result ->
                if (duration == NoDuration) {
                    (dtg == targetDtg) == result
                } else {
                    (targetDtg.functions.subtractDuration(duration) <= dtg && dtg < targetDtg.functions.addDuration(
                        duration
                    )) == result
                }
            }
        )

        val inInterval: (Interval) -> bool = function(
            command = { interval -> dtg.functions.inRange(interval.begins, interval.ends) }
        )

        val inRange: (Dtg, Dtg) -> bool = function(
            command = { min, max -> min <= dtg && dtg < max }
        )

        val finestGranularity: (Duration) -> bool = function(
            command = { duration -> dtg.functions.toDuration().milliseconds % duration.milliseconds == 0L },
            pre = { duration -> duration != NoDuration }
        )

        val instant: () -> Interval = function(
            command = { mk_Interval(dtg, dtg.functions.addDuration(OneMillisecondDuration)) },
            post = { result -> result.functions.contains(dtg) }
        )

        val format: () -> String = function<String>(
            command = { dtg.date.functions.format() + "T" + dtg.time.functions.format() }
        )

        val addMonths: (nat) -> Dtg = function(
            command = { n ->
                mk_Dtg(dtg.date.functions.addMonths(n), dtg.time)
            },
        )

        val subtractMonths: (nat) -> Dtg = function(
            command = { n ->
                mk_Dtg(dtg.date.functions.subtractMonths(n), dtg.time)
            },
        )

        val addDays: (nat) -> Dtg = function(
            command = { n -> mk_Dtg(dtg.date.functions.addDays(n), dtg.time) },
            post = { n, result -> result.functions.subtractDays(n) == dtg }
        )
        val subtractDays: (nat) -> Dtg = function(
            command = { n -> mk_Dtg(dtg.date.functions.subtractDays(n), dtg.time) },
            pre = { n -> dtg.functions.toDuration() >= Duration.fromDays(n.toLong()) },
//            post = { n, result -> result.functions.addDays(n) == dtg }

        )

    }
}

@Module
interface DtgInZone : Comparable<DtgInZone> {
    val date: Date
    val time: TimeInZone

    @Invariant
    fun dtgBeforeFirstDate() =
        !(date == FirstDate && time.functions.normaliseTimeInZone().plusOrMinusADay == PlusOrMinus.Plus)

    @Invariant
    fun dtgAfterLastDate() =
        !(date == LastDate && time.functions.normaliseTimeInZone().plusOrMinusADay == PlusOrMinus.Minus)

    override fun compareTo(other: DtgInZone) = functions.normalise().compareTo(other.functions.normalise())


    @FunctionProvider(DtgInZoneFunctions::class)
    val functions: DtgInZoneFunctions

    class DtgInZoneFunctions(private val dtgInZone: DtgInZone) {

        val normalise: () -> Dtg = function<Dtg>(
            command = {
                val normalisedTime = dtgInZone.time.functions.normaliseTimeInZone()
                val baseDtg = mk_Dtg(dtgInZone.date, normalisedTime.time)
                when (normalisedTime.plusOrMinusADay) {
                    PlusOrMinus.Plus -> baseDtg.functions.subtractDuration(OneDayDuration)
                    PlusOrMinus.Minus -> baseDtg.functions.addDuration(OneDayDuration)
                    PlusOrMinus.None -> baseDtg
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
    val begins: Dtg
    val ends: Dtg

    @Invariant
    fun zeroSizeInterval() = begins != ends

    @Invariant
    fun beginAfterEnd() = begins <= ends

    @FunctionProvider(IntervalFunctions::class)
    val functions: IntervalFunctions

    class IntervalFunctions(private val interval: Interval) {
        val within: (Interval) -> bool = function(
            command = { containerInterval ->
                containerInterval.begins <= interval.begins && interval.ends <= containerInterval.ends
            },
            post = { containerInterval, result ->
                result == (containerInterval.begins <= interval.begins
                        && interval.ends <= containerInterval.ends)
            }
        )
        val overlap: (Interval) -> bool = function(
            command = { otherInterval -> otherInterval.begins < interval.ends && interval.begins < otherInterval.ends },
            post = { otherInterval, result -> result == (otherInterval.begins < interval.ends && interval.begins < otherInterval.ends) }
        )
        val intervalDuration: () -> Duration = function(
            command = { dtgDiff(interval.begins, interval.ends) },
            post = { result -> interval.begins.functions.addDuration(result) == interval.ends }
        )
        val finestGranularity: (Duration) -> bool = function(
            command = { duration ->
                interval.begins.functions.finestGranularity(duration) &&
                        interval.ends.functions.finestGranularity(duration)
            }
        )

        val contains: (Dtg) -> bool = function(
            command = { dtg -> dtg.functions.inInterval(interval) },
//            post = {dtg, result -> dtg.functions.inInterval(interval) == result}
        )

        val format: () -> String = function<String>(
            command = { interval.begins.functions.format() + "/" + interval.ends.functions.format() }
        )
    }
}

val dtgDiff: (Dtg, Dtg) -> Duration = function(
    command = { dtg1, dtg2 -> durationDiff(dtg1.functions.toDuration(), dtg2.functions.toDuration()) }
)

val minDtg: (Set1<Dtg>) -> Dtg = function(
    command = { dtgs -> dtgs.min() },
    post = { dtgs, result -> result in dtgs && forall(dtgs) { result <= it } }
)
val maxDtg: (Set1<Dtg>) -> Dtg = function(
    command = { dtgs -> dtgs.max() },
    post = { dtgs, result -> result in dtgs && forall(dtgs) { result >= it } }
)

val monthsBetweenDtgs: (Dtg, Dtg) -> nat = function(
    command = { earlierDtg, laterDtg ->
        MonthsPerYear * yearsBetweenDtgs(earlierDtg, laterDtg) +
                (if (laterDtg.date.month < earlierDtg.date.month) 12 else 0) +
                if (laterDtg.date.day >= earlierDtg.date.day) {
                    laterDtg.date.month - earlierDtg.date.month
                } else {
                    laterDtg.date.month - earlierDtg.date.month - 1
                }
    },
    pre = { earlierDtg, laterDtg -> earlierDtg <= laterDtg }
)

val yearsBetweenDtgs: (Dtg, Dtg) -> nat = function(
    command = { earlierDtg, laterDtg ->

        val millisInYearUpToEarlierDtg =
            earlierDtg.functions.toDuration().milliseconds - Duration.durationUpToYear(earlierDtg.date.year).milliseconds
        val millisInYearUpToLaterDtg =
            laterDtg.functions.toDuration().milliseconds - Duration.durationUpToYear(laterDtg.date.year).milliseconds

        if (millisInYearUpToEarlierDtg <= millisInYearUpToLaterDtg) {
            laterDtg.date.year - earlierDtg.date.year
        } else {
            laterDtg.date.year - earlierDtg.date.year - 1
        }
    },
    pre = { earlierDtg, laterDtg -> earlierDtg <= laterDtg }
)
