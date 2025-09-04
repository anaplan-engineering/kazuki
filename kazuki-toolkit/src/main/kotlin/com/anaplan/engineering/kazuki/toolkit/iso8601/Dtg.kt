package com.anaplan.engineering.kazuki.toolkit.iso8601

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.iso8601.Date_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.iso8601.Dtg_Module.mk_Dtg
import com.anaplan.engineering.kazuki.toolkit.iso8601.Duration_Module.mk_Duration
import com.anaplan.engineering.kazuki.toolkit.iso8601.Interval_Module.mk_Interval
import com.anaplan.engineering.kazuki.toolkit.iso8601.Time_Module.mk_Time
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Module
interface Dtg : PrettyPrintable {
    val date: Date
    val time: Time

    override fun pretty() = properties.formatted

    @FunctionProvider(DtgFunctions::class)
    val functions: DtgFunctions

    @FunctionProvider(DtgProperties::class)
    val properties: DtgProperties

}

class DtgProperties(private val dtg: Dtg) {

    val durationSinceFirstDtg by lazy {
        dtg.date.properties.durationSinceFirstDate.functions.add(dtg.time.properties.durationSinceFirstTime)
    }

    val instant by lazy { mk_Interval(dtg, dtg.functions.addDuration(OneMillisecondDuration)) }

    val formatted by lazy { dtg.date.properties.formatted + "T" + dtg.time.properties.formatted }
}

class DtgFunctions(private val dtg: Dtg) {

    private val localDateTime by lazy { dtg.toLocalDateTime() }

    val isEarlierThan = function(
        command = { other: Dtg -> localDateTime < other.toLocalDateTime() },
        post = { other, result ->
            result iff (dtg.date.functions.isEarlierThan(other.date) || (
                    dtg.date == other.date && dtg.time.functions.isEarlierThan(other.time)))
        }
    )

    val addDuration = function(
        command = { duration: Duration ->
            dtg.toLocalDateTime().plusNanos(duration.milliseconds.toLong() * 1_000_000).toDtg()
        },
        post = { duration, result -> DtgUtilities.durationBetween(dtg, result) == duration }
    )

    val subtractDuration: (Duration) -> Dtg = function(
        command = { duration ->
            dtg.toLocalDateTime().minusNanos(duration.milliseconds.toLong() * 1_000_000).toDtg()
        },
        pre = { duration -> duration <= dtg.properties.durationSinceFirstDtg },
        post = { duration, result -> DtgUtilities.durationBetween(result, dtg) == duration }
    )

    val withinDurationOfDtg = function(
        command = { duration: Duration, other: Dtg ->
            dtg.functions.inRange(other.functions.subtractDuration(duration), other.functions.addDuration(duration))
        },
        post = { duration, other, result ->
            result iff dtg.functions.inRange(
                other.functions.subtractDuration(duration),
                other.functions.addDuration(duration)
            )
        }
    )

    val inInterval = function(
        command = { interval: Interval -> dtg.functions.inRange(interval.begins, interval.ends) }
    )

    val inRange = function(
        command = { start: Dtg, end: Dtg ->
            dtg == start || (start.functions.isEarlierThan(dtg) && dtg.functions.isEarlierThan(end))
        }
    )

    val finestGranularity = function(
        command = { granularity: Duration -> dtg.properties.durationSinceFirstDtg.milliseconds % granularity.milliseconds == 0uL },
        pre = { granularity -> granularity != NoDuration }
    )

    val addYears = function(command = { n: nat -> mk_Dtg(dtg.date.functions.addYears(n), dtg.time) })

    val subtractYears = function(command = { n: nat -> mk_Dtg(dtg.date.functions.subtractYears(n), dtg.time) })

    val addMonths = function(command = { n: nat -> mk_Dtg(dtg.date.functions.addMonths(n), dtg.time) })

    val subtractMonths = function(command = { n: nat -> mk_Dtg(dtg.date.functions.subtractMonths(n), dtg.time) })

    val addDays = function(command = { n: nat -> mk_Dtg(dtg.date.functions.addDays(n), dtg.time) })

    val subtractDays = function(command = { n: nat -> mk_Dtg(dtg.date.functions.subtractDays(n), dtg.time) })

}


object DtgUtilities {

    fun Dtg.isEarlierThanOrEqual(other: Dtg) =
        this == other || this.functions.isEarlierThan(other)

    val durationBetween = function(
        command = { dtg1: Dtg, dtg2: Dtg ->
            mk_Duration(ChronoUnit.MILLIS.between(dtg1.toLocalDateTime(), dtg2.toLocalDateTime()).toNat())
        },
        pre = { dtg1, dtg2 -> dtg1.isEarlierThanOrEqual(dtg2) },
    )

    val earliest = function(
        command = { dtgs: Set1<Dtg> -> dtgs.minOf { it.toLocalDateTime() }.toDtg() },
        post = { dtgs, result -> result in dtgs && forall(dtgs / result) { result.functions.isEarlierThan(it) } }
    )

    val latest = function(
        command = { dtgs: Set1<Dtg> -> dtgs.maxOf { it.toLocalDateTime() }.toDtg() },
        post = { dtgs, result -> result in dtgs && forall(dtgs / result) { it.functions.isEarlierThan(result) } }
    )

    val monthsBetween = function(
        command = { earlierDtg: Dtg, laterDtg: Dtg ->
            ChronoUnit.MONTHS.between(earlierDtg.toLocalDateTime(), laterDtg.toLocalDateTime()).toNat()
        },
        pre = { earlierDtg, laterDtg -> earlierDtg.isEarlierThanOrEqual(laterDtg) },
        post = { earlierDtg, laterDtg, result ->
            val upperBound = laterDtg.functions.subtractMonths(result)
            val inUpperBoundMonth =
                upperBound.date.year == earlierDtg.date.year && upperBound.date.month == earlierDtg.date.month

            (earlierDtg.isEarlierThanOrEqual(upperBound)) and {
                inUpperBoundMonth or {
                    val lowerBound = laterDtg.functions.subtractMonths(result + 1u)
                    val inLowerBoundMonth =
                        lowerBound.date.year == earlierDtg.date.year && lowerBound.date.month == earlierDtg.date.month

                    (lowerBound.functions.isEarlierThan(earlierDtg)) && inLowerBoundMonth
                }
            }
        }
    )

    val yearsBetween = function(
        command = { earlierDtg: Dtg, laterDtg: Dtg ->
            ChronoUnit.YEARS.between(earlierDtg.toLocalDateTime(), laterDtg.toLocalDateTime()).toNat()
        },
        pre = { earlierDtg, laterDtg -> earlierDtg.isEarlierThanOrEqual(laterDtg) },
        post = { earlierDtg, laterDtg, result -> result == monthsBetween(earlierDtg, laterDtg) / MonthsPerYear }
    )
}

private fun LocalDateTime.toDtg() =
    mk_Dtg(
        mk_Date(this.year.toNat(), this.monthValue.toNat1(), this.dayOfMonth.toNat1()),
        mk_Time(this.hour.toNat(), this.minute.toNat(), this.second.toNat(), (this.nano / 1_000_000).toNat())
    )

private fun Dtg.toLocalDateTime() = LocalDateTime.of(
    this.date.year.toInt(),
    this.date.month.toInt(),
    this.date.day.toInt(),
    this.time.hour.toInt(),
    this.time.minute.toInt(),
    this.time.second.toInt(),
    this.time.millisecond.toInt() * 1_000_000
)