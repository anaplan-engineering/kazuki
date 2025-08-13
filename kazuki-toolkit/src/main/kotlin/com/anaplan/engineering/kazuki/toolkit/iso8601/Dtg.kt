package com.anaplan.engineering.kazuki.toolkit.iso8601

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.iso8601.Dtg_Module.mk_Dtg
import com.anaplan.engineering.kazuki.toolkit.iso8601.DurationUtilities.durationDiff
import com.anaplan.engineering.kazuki.toolkit.iso8601.Interval_Module.mk_Interval

@Module
interface Dtg : Comparable<Dtg>, PrettyPrintable {
    val date: Date
    val time: Time

    private val dtgComparator get() = compareBy<Dtg> { it.date }.thenBy { it.time }
    override fun compareTo(other: Dtg) = dtgComparator.compare(this, other)


    override fun pretty() = properties.formatted

    @FunctionProvider(DtgFunctions::class)
    val functions: DtgFunctions

    @FunctionProvider(DtgProperties::class)
    val properties: DtgProperties

}

class DtgProperties(private val dtg: Dtg) {

    val durationSinceFirstDtg by lazy { dtg.date.properties.durationSinceFirstDate.functions.addDuration(dtg.time.properties.durationSinceFirstTime) }

    val instant by lazy { mk_Interval(dtg, dtg.functions.addDuration(OneMillisecondDuration)) }

    val formatted by lazy { dtg.date.properties.formatted + "T" + dtg.time.properties.formatted }
}

class DtgFunctions(private val dtg: Dtg) {

    val addDuration: (Duration) -> Dtg = function(
        command = { duration -> dtg.properties.durationSinceFirstDtg.functions.addDuration(duration).properties.dtgAfterFirstDtg },
        post = { duration, result -> result.functions.subtractDuration(duration) == dtg }
    )

    val subtractDuration: (Duration) -> Dtg = function(
        command = { duration -> dtg.properties.durationSinceFirstDtg.functions.subtractDuration(duration).properties.dtgAfterFirstDtg },
        pre = { duration -> duration <= dtg.properties.durationSinceFirstDtg },
//          post = { duration, result -> result.functions.addDuration(duration) == dtg }
//          This post condition uses a function whose post condition uses this function as a post condition.
//          If not commented, the two functions will recur until a stack overflow error occurs.
//          However, it is still a valid post condition so is left here for completeness.
    )


    val withinDurationOfDtg: (Duration, Dtg) -> bool = function(
        command = { duration, targetDtg ->
            if (duration.milliseconds == 0uL) {
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
        command = { granularity -> dtg.properties.durationSinceFirstDtg.milliseconds % granularity.milliseconds == 0uL },
        pre = { granularity -> granularity != NoDuration }
    )

    val addYears: (nat) -> Dtg = function(
        command = { n -> mk_Dtg(dtg.date.functions.addYears(n), dtg.time) },
    )

    val subtractYears: (nat) -> Dtg = function(
        command = { n -> mk_Dtg(dtg.date.functions.subtractYears(n), dtg.time) },
    )

    val addMonths: (nat) -> Dtg = function(
        command = { n -> mk_Dtg(dtg.date.functions.addMonths(n), dtg.time) },
    )

    val subtractMonths: (nat) -> Dtg = function(
        command = { n -> mk_Dtg(dtg.date.functions.subtractMonths(n), dtg.time) },
    )

    val addDays: (nat) -> Dtg = function(
        command = { n -> mk_Dtg(dtg.date.functions.addDays(n), dtg.time) },
        post = { n, result -> result.functions.subtractDays(n) == dtg }
    )
    val subtractDays: (nat) -> Dtg = function(
        command = { n -> mk_Dtg(dtg.date.functions.subtractDays(n), dtg.time) },
        pre = { n -> dtg.properties.durationSinceFirstDtg.properties.days >= n },
//          post = { n, result -> result.functions.addDays(n) == dtg }
//          This post condition uses a function whose post condition uses this function as a post condition.
//          If not commented, the two functions will recur until a stack overflow error occurs.
//          However, it is still a valid post condition so is left here for completeness.

    )

}

@Module
interface DtgInZone : Comparable<DtgInZone> {
    val date: Date
    val time: TimeInZone

    @Invariant
    fun dtgBeforeFirstDate() =
        !(date == FirstDate && time.properties.normalisedTime.plusOrMinusADay == PlusOrMinus.Plus)

    @Invariant
    fun dtgAfterLastDate() =
        !(date == LastDate && time.properties.normalisedTime.plusOrMinusADay == PlusOrMinus.Minus)

    override fun compareTo(other: DtgInZone) = properties.normalised.compareTo(other.properties.normalised)

    @FunctionProvider(DtgInZoneProperties::class)
    val properties: DtgInZoneProperties

}

class DtgInZoneProperties(private val dtgInZone: DtgInZone) {

    val normalised by lazy {
            val normalisedTime = dtgInZone.time.properties.normalisedTime
            val baseDtg = mk_Dtg(dtgInZone.date, normalisedTime.time)
            when (normalisedTime.plusOrMinusADay) {
                PlusOrMinus.Plus -> baseDtg.functions.subtractDuration(OneDayDuration)
                PlusOrMinus.Minus -> baseDtg.functions.addDuration(OneDayDuration)
                PlusOrMinus.None -> baseDtg
            }
        }

    val formatted by lazy { dtgInZone.date.properties.formatted + "T" + dtgInZone.time.properties.formatted }
}

object DtgUtilities {
    val dtgDiff: (Dtg, Dtg) -> Duration = function(
        command = { dtg1, dtg2 ->
            durationDiff(
                dtg1.properties.durationSinceFirstDtg,
                dtg2.properties.durationSinceFirstDtg
            )
        }
    )

    val minDtg: (Set1<Dtg>) -> Dtg = function(
        command = { dtgs -> dtgs.min() },
        post = { dtgs, result -> result in dtgs && forall(dtgs) { result <= it } }
    )
    val maxDtg: (Set1<Dtg>) -> Dtg = function(
        command = { dtgs -> dtgs.max() },
        post = { dtgs, result -> result in dtgs && forall(dtgs) { result >= it } }
    )

    val monthsBetweenDtgs = function(
        command = { earlierDtg: Dtg, laterDtg: Dtg ->
            val earlierDate = earlierDtg.date
            val laterDate = laterDtg.date

            val baseYears = laterDate.year - earlierDate.year
            val baseMonths = ((baseYears * MonthsPerYear) + laterDate.month) - earlierDate.month

            // The above calculation is off by one if laterDate is earlier on in its month than earlierDate
            // (for example, 1 March is only one month after 2 January)
            val isPartialMonth =
                laterDate.day < earlierDate.day || (laterDate.day == earlierDate.day && laterDtg.time < earlierDtg.time)

            if (isPartialMonth) {
                baseMonths - 1u
            } else {
                baseMonths
            }
        },
        pre = { earlierDtg, laterDtg -> earlierDtg <= laterDtg },
        post = { earlierDtg, laterDtg, result ->
            val upperBound = laterDtg.functions.subtractMonths(result)
            val inUpperBoundMonth =
                upperBound.date.year == earlierDtg.date.year && upperBound.date.month == earlierDtg.date.month

            (earlierDtg <= upperBound) and {
                inUpperBoundMonth or {
                    val lowerBound = laterDtg.functions.subtractMonths(result + 1u)
                    val inLowerBoundMonth =
                        lowerBound.date.year == earlierDtg.date.year && lowerBound.date.month == earlierDtg.date.month

                    (lowerBound < earlierDtg) && inLowerBoundMonth
                }
            }
        }
    )

    val yearsBetweenDtgs = function(
        command = { earlierDtg: Dtg, laterDtg: Dtg ->
            val earlierDate = earlierDtg.date
            val laterDate = laterDtg.date

            val baseYears = laterDate.year - earlierDate.year

            // The above calculation is off by one if laterDate is earlier on in its year than earlierDate
            // (for example, 1 January 2009 is only one year after 2 January 2008)
            val isPartialYear =
                laterDate.month < earlierDate.month || (laterDate.month == earlierDate.month &&
                        (laterDate.day < earlierDate.day || (laterDate.day == earlierDate.day && laterDtg.time < earlierDtg.time)))

            if (isPartialYear) {
                baseYears - 1u
            } else {
                baseYears
            }
        },
        pre = { earlierDtg, laterDtg -> earlierDtg <= laterDtg },
        post = { earlierDtg, laterDtg, result ->
            result == monthsBetweenDtgs(earlierDtg, laterDtg) / MonthsPerYear
        }
    )
}