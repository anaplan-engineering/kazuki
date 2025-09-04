package com.anaplan.engineering.kazuki.toolkit.iso8601

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.iso8601.DateUtilities.daysInMonth
import com.anaplan.engineering.kazuki.toolkit.iso8601.DateUtilities.daysInYear
import com.anaplan.engineering.kazuki.toolkit.iso8601.Date_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.iso8601.DurationUtilities.fromYear
import com.anaplan.engineering.kazuki.toolkit.iso8601.Duration_Module.mk_Duration
import java.time.LocalDateTime
import java.time.LocalTime

@Module
interface Duration : Comparable<Duration> {

    val milliseconds: nat

    override fun compareTo(other: Duration) = milliseconds.compareTo(other.milliseconds)

    @FunctionProvider(DurationFunctions::class)
    val functions: DurationFunctions

    @FunctionProvider(DurationProperties::class)
    val properties: DurationProperties

}

class DurationProperties(private val duration: Duration) {

    val seconds by lazy { duration.milliseconds / MillisPerSecond }
    val minutes by lazy { seconds / SecondsPerMinute }
    val hours by lazy { minutes / MinutesPerHour }
    val days by lazy { hours / HoursPerDay }

    val formatted by lazy {
        val numDays = days
        val timeOfDay = modDays.properties.timeAfterFirstTime
        val date = formatItem(numDays, 'D')
        val time = formatItem(timeOfDay.hour, 'H') + formatItem(timeOfDay.minute, 'M') +
                if (timeOfDay.millisecond == 0uL) {
                    formatItem(timeOfDay.second, 'S')
                } else {
                    formatItemSec(timeOfDay.second, timeOfDay.millisecond)
                }
        if (date == "" && time == "") "PT0S" else "P$date${if (time == "") "" else "T$time"}"
    }

    private val formatItem: (nat, Char) -> String = function(
        command = { n, c -> if (n == 0uL) "" else String.format("%d%s", n.safeToInt(), c) }
    )

    private val formatItemSec: (nat, nat) -> String = function(
        command = { seconds, milliseconds -> String.format("%d.%03dS", seconds.safeToInt(), milliseconds.safeToInt()) }
    )

    val modSeconds by lazy { mk_Duration(duration.milliseconds % OneSecondDuration.milliseconds) }

    val modMinutes by lazy { mk_Duration(duration.milliseconds % OneMinuteDuration.milliseconds) }

    val modHours by lazy { mk_Duration(duration.milliseconds % OneHourDuration.milliseconds) }

    val modDays by lazy { mk_Duration(duration.milliseconds % OneDayDuration.milliseconds) }

    val timeAfterFirstTime by lazy { duration.functions.addToTime(FirstTime) }

    val dateAfterFirstDate by lazy { duration.functions.addToDate(FirstDate) }

    val dtgAfterFirstDtg by lazy { duration.functions.addToDtg(FirstDtg) }

    val yearsAfterFirstYear by lazy { duration.functions.yearCountFromYear(FirstYear) }
}

class DurationFunctions(private val duration: Duration) {

    private fun Year.startLocalDateTime() = LocalDateTime.of(toInt(), 1, 1, 0, 0, 0)

    val monthCountInYear = function(
        command = { year: Year -> (year.startLocalDateTime().plusDuration(duration).monthValue - 1).toNat() },
        pre = { year -> duration < fromYear(year) },
        post = { year, result ->
            val startOfYear = mk_Date(year, 1u, 1u)
            val startOfResultMonth = startOfYear.functions.addMonths(result)
            val startOfFollowingMonth = startOfResultMonth.functions.addMonths(1u)
            duration >= DateUtilities.durationBetween(startOfYear, startOfResultMonth)
                    && duration < DateUtilities.durationBetween(startOfYear, startOfFollowingMonth)
        }
    )

    val yearCountFromYear = function(
        command = { year: Year -> year.startLocalDateTime().plusDuration(duration).year.toNat() as Year - year },
        post = { year, result ->
            val startOfGivenYear = mk_Date(year, 1u, 1u)
            duration >= DateUtilities.durationBetween(startOfGivenYear, mk_Date(year + result, 1u, 1u))
                    && duration < DateUtilities.durationBetween(startOfGivenYear, mk_Date(year + result + 1u, 1u, 1u))
        }
    )

    val addToDtg = function(
        command = { dtg: Dtg -> dtg.functions.addDuration(duration) },
        pre = { dtg ->
            val maxDuration = LastDtg.properties.durationSinceFirstDtg
            val totalDuration = dtg.properties.durationSinceFirstDtg.functions.add(duration)
            totalDuration <= maxDuration
        }
    )

    val addToDate = function(
        command = { date: Date -> date.toLocalDate().atStartOfDay().plusDuration(duration).toLocalDate().toDate() },
        pre = { date ->
            val totalDuration = date.properties.durationSinceFirstDate.functions.add(duration)
            val maxDuration = LastDate.properties.durationSinceFirstDate.functions.add(OneDayDuration)
            totalDuration < maxDuration
        }
    )

    val addToTime = function(
        command = { time: Time -> time.toLocalTime().plusDuration(duration).toTime() },
        pre = { time ->
            val totalDuration = time.properties.durationSinceFirstTime.functions.add(duration)
            totalDuration < OneDayDuration
        },
    )

    val add = function(
        command = { other: Duration -> mk_Duration(duration.milliseconds + other.milliseconds) },
        post = { other, result -> result.milliseconds == duration.milliseconds + other.milliseconds }
    )

    val subtract = function(
        command = { other: Duration -> mk_Duration(duration.milliseconds - other.milliseconds) },
        pre = { other -> duration >= other },
        post = { other, result -> result.milliseconds == duration.milliseconds - other.milliseconds }
    )

    val multiply = function(
        command = { n: nat -> mk_Duration(duration.milliseconds * n) },
        post = { n, result -> result.milliseconds == duration.milliseconds * n }
    )

    val divide = function(
        command = { n: nat1 -> mk_Duration(duration.milliseconds / n) },
        post = { n, result -> result.milliseconds == duration.milliseconds / n }
    )

}

object DurationUtilities {

    val min = function(
        command = { durations: Set1<Duration> -> durations.min() },
        post = { durations, result -> result in durations && forall(durations) { result <= it } }
    )

    val max = function(
        command = { durations: Set1<Duration> -> durations.max() },
        post = { durations, result -> result in durations && forall(durations) { result >= it } }
    )

    val sum = function(
        command = { durations: Sequence<Duration> -> mk_Duration(durations.sumOf { it.milliseconds }) },
        post = { durations, result -> result.milliseconds == durations.sumOf { it.milliseconds } }
    )

    val durationBetween = function(
        command = { d1: Duration, d2: Duration ->
            val diff = if (d1 > d2) {
                d1.milliseconds - d2.milliseconds
            } else {
                d2.milliseconds - d1.milliseconds
            }
            mk_Duration(diff)
        },
        post = { d1, d2, result -> if (d1 > d2) d2.functions.add(result) == d1 else d1.functions.add(result) == d2 }
    )

    val fromMillis = function(
        command = { millis: nat -> mk_Duration(millis) },
        post = { millis, result -> result.milliseconds == millis },
    )

    val fromSeconds = function(
        command = { seconds: nat -> fromMillis(seconds * MillisPerSecond) },
        post = { seconds, result -> result.properties.seconds == seconds && result.properties.modSeconds == NoDuration },
    )

    val fromMinutes = function(
        command = { minutes: nat -> fromSeconds(minutes * SecondsPerMinute) },
        post = { minutes, result -> result.properties.minutes == minutes && result.properties.modMinutes == NoDuration },
    )

    val fromHours = function(
        command = { hours: nat -> fromMinutes(hours * MinutesPerHour) },
        post = { hours, result -> result.properties.hours == hours && result.properties.modHours == NoDuration },
    )

    val fromDays = function(
        command = { days: nat -> fromHours(days * HoursPerDay) },
        post = { days, result -> result.properties.days == days && result.properties.modDays == NoDuration },
    )

    val fromMonth = function(
        command = { year: Year, month: Month -> fromDays(daysInMonth(year, month)) },
        post = { year, month, result ->
            result.properties.days == daysInMonth(year, month) && result.properties.modDays == NoDuration
        },
    )

    val fromYear = function(
        command = { year: Year -> fromDays(daysInYear(year)) },
        post = { year, result ->
            result.properties.days == daysInYear(year) && result.properties.modDays == NoDuration
        },
    )


    val durationInYearUpToStartOfMonth: (Year, Month) -> Duration = function(
        command = { year, month -> sum(seq(1uL until month) { fromMonth(year, it) }) }
    )

    val durationFromFirstYearUpToStartOfYear: (Year) -> Duration = function(
        command = { year -> sum(seq(FirstYear until year) { fromYear(it) }) }
    )
}

internal fun LocalDateTime.plusDuration(d: Duration) = plusNanos((d.milliseconds * NanosPerMilli).toLong())
internal fun LocalTime.plusDuration(d: Duration) = plusNanos((d.milliseconds * NanosPerMilli).toLong())
