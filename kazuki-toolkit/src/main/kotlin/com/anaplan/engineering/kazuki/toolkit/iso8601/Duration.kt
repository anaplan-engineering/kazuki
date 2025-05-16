package com.anaplan.engineering.kazuki.toolkit.iso8601

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.iso8601.DateUtilities.daysInMonth
import com.anaplan.engineering.kazuki.toolkit.iso8601.DateUtilities.daysInYear
import com.anaplan.engineering.kazuki.toolkit.iso8601.Date_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.iso8601.Dtg_Module.mk_Dtg
import com.anaplan.engineering.kazuki.toolkit.iso8601.Duration.Companion.durationFromFirstYearUpToStartOfYear
import com.anaplan.engineering.kazuki.toolkit.iso8601.Duration.Companion.durationInYearUpToStartOfMonth
import com.anaplan.engineering.kazuki.toolkit.iso8601.Duration.Companion.fromDays
import com.anaplan.engineering.kazuki.toolkit.iso8601.Duration.Companion.fromYear
import com.anaplan.engineering.kazuki.toolkit.iso8601.DurationUtiltites.sumDuration
import com.anaplan.engineering.kazuki.toolkit.iso8601.Duration_Module.mk_Duration
import com.anaplan.engineering.kazuki.toolkit.iso8601.Time_Module.mk_Time
import kotlin.math.abs

@Module
interface Duration : Comparable<Duration> {

    val milliseconds: nat1

    @Invariant
    fun millisNonNegative() = milliseconds >= 0uL

    override fun compareTo(other: Duration) = milliseconds.compareTo(other.milliseconds)

    companion object {

        val fromMillis: (nat1) -> Duration = function(
            command = { milliseconds -> mk_Duration(milliseconds) },
//        post = { millisecond, result -> result.functions.toMillis() == millisecond }
//        This post condition uses a function whose post condition uses this function as a post condition.
//        If not commented, the two functions will recur until a stack overflow error occurs.
//        However, it is still a valid post condition so is left here for completeness.

        )

        val fromSeconds: (nat1) -> Duration = function(
            command = { seconds -> fromMillis(seconds * MillisPerSecond) },
//        post = { second, result -> result.functions.toSeconds() == second }
//        This post condition uses a function whose post condition uses this function as a post condition.
//        If not commented, the two functions will recur until a stack overflow error occurs.
//        However, it is still a valid post condition so is left here for completeness.

        )

        val fromMinutes: (nat1) -> Duration = function(
            command = { minutes -> fromSeconds(minutes * SecondsPerMinute) },
//        post = { minutes, result -> result.functions.toMinutes() == minutes }
//        This post condition uses a function whose post condition uses this function as a post condition.
//        If not commented, the two functions will recur until a stack overflow error occurs.
//        However, it is still a valid post condition so is left here for completeness.
        )

        val fromHours: (nat1) -> Duration = function(
            command = { hours -> fromMinutes(hours * MinutesPerHour) },
//        post = { hour, result -> result.functions.toHours() == hour }
//        This post condition uses a function whose post condition uses this function as a post condition.
//        If not commented, the two functions will recur until a stack overflow error occurs.
//        However, it is still a valid post condition so is left here for completeness.
        )

        val fromDays: (nat1) -> Duration = function(
            command = { days -> fromHours(days * HoursPerDay) },
//        post = { day, result -> result.functions.toDays() == day }
//        This post condition uses a function whose post condition uses this function as a post condition.
//        If not commented, the two functions will recur until a stack overflow error occurs.
//        However, it is still a valid post condition so is left here for completeness.
        )

        val fromMonth: (Year, Month) -> Duration = function(
            command = { year, month -> fromDays(daysInMonth(year, month)) }
        )

        val durationInYearUpToStartOfMonth: (Year, Month) -> Duration = function(
            command = { year, month -> sumDuration(seq(1uL until month) { fromMonth(year, it) }) }
        )

        val fromYear: (Year) -> Duration = function(
            command = { year -> fromDays(daysInYear(year)) }
        )

        val durationFromFirstYearUpToStartOfYear: (Year) -> Duration = function(
            command = { year -> sumDuration(seq(FirstYear until year) { fromYear(it) }) }
        )
    }

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
        val timeOfDay = duration.functions.modDays().functions.toTimeAfterFirstTime()
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
}

class DurationFunctions(private val duration: Duration) {


    val toMonthsInGivenYear: (Year) -> nat1 = function(
        command = { year ->
            (set(
                1uL..MonthsPerYear,
                filter = { durationInYearUpToStartOfMonth(year, it) <= duration }) { it }).max() - 1uL
        },
        pre = { year -> duration < fromYear(year) }
    )

    val toYearsAfterFirstYear: () -> nat = function<nat>(
        command = { toYearsAfterGivenYear(FirstYear) }
    )

    val toYearsAfterGivenYear: (Year) -> nat =
        function(
            command = { givenYear ->
                tailrec fun safeRecursion(acc: nat, duration: Duration, givenYear: Year): nat =
                    if (duration < fromYear(givenYear)) {
                        acc
                    } else {
                        safeRecursion(
                            acc + 1uL,
                            duration.functions.subtractDuration(fromYear(givenYear)),
                            givenYear + 1uL
                        )
                    }

                safeRecursion(0uL, duration, givenYear)
            },
        )

    val toDtgAfterFirstDtg: () -> Dtg = function(
        command = { duration.functions.toDtgAfterGivenDtg(FirstDtg) },
        pre = { duration <= LastDtg.properties.durationSinceFirstDtg },
        post = { result -> result.properties.durationSinceFirstDtg == duration }
    )

    val toDtgAfterGivenDtg: (Dtg) -> Dtg = function(
        command = { givenDtg ->
            val totalDuration = givenDtg.properties.durationSinceFirstDtg.functions.addDuration(duration)
            val daysDuration = fromDays(totalDuration.properties.days)
            val timeDuration = totalDuration.functions.modDays()
            mk_Dtg(daysDuration.functions.toDateAfterFirstDate(), timeDuration.functions.toTimeAfterFirstTime())
        },
        pre = { givenDtg ->
            val maxDuration = LastDtg.properties.durationSinceFirstDtg
            val totalDuration = givenDtg.properties.durationSinceFirstDtg.functions.addDuration(duration)
            totalDuration <= maxDuration
        }
    )

    val toDateAfterFirstDate: () -> Date = function(
        command = { toDateAfterGivenDate(FirstDate) },
        pre = {
            val maxDuration = LastDate.properties.durationSinceFirstDate.functions.addDuration(OneDayDuration)
            duration < maxDuration
        },
        post = { result ->
            result.properties.durationSinceFirstDate <= duration &&
                    duration < result.properties.durationSinceFirstDate.functions.addDuration(OneDayDuration)
        }
    )

    val toDateAfterGivenDate: (Date) -> Date = function(
        command = { givenDate ->
            val totalDuration = givenDate.properties.durationSinceFirstDate.functions.addDuration(duration)
            val year = totalDuration.functions.toYearsAfterGivenYear(FirstYear)
            val totalDurationModYear =
                totalDuration.functions.subtractDuration(durationFromFirstYearUpToStartOfYear(year))
            val month = totalDurationModYear.functions.toMonthsInGivenYear(year) + 1uL
            val day = (totalDurationModYear.functions.subtractDuration(
                durationInYearUpToStartOfMonth(year, month)
            ).properties.days + 1uL)
            mk_Date(year, month, day)
        },
        pre = { givenDate ->
            val totalDuration = givenDate.properties.durationSinceFirstDate.functions.addDuration(duration)
            val maxDuration = LastDate.properties.durationSinceFirstDate.functions.addDuration(OneDayDuration)
            totalDuration < maxDuration
        }
    )

    val toTimeAfterFirstTime: () -> Time = function(
        command = { toTimeAfterGivenTime(FirstTime) },
        pre = { duration < OneDayDuration },
        post = { result -> result.properties.durationSinceFirstTime == duration }
    )

    val toTimeAfterGivenTime: (Time) -> Time = function(
        command = { givenTime ->
            val totalDuration = givenTime.properties.durationSinceFirstTime.functions.addDuration(duration)
            val hour = totalDuration.properties.hours
            val minute = totalDuration.functions.modHours().properties.minutes
            val second = totalDuration.functions.modMinutes().properties.seconds
            val millisecond = totalDuration.functions.modSeconds().milliseconds
            mk_Time(hour, minute, second, millisecond)
        },
        pre = { givenTime ->
            val totalDuration = givenTime.properties.durationSinceFirstTime.functions.addDuration(duration)
            totalDuration < OneDayDuration
        },
    )

    val addDuration: (Duration) -> Duration = function(
        command = { plusDuration -> mk_Duration(duration.milliseconds + plusDuration.milliseconds) },
        post = { plusDuration, result ->
            result.functions.subtractDuration(plusDuration) == duration
                    && result.functions.subtractDuration(duration) == plusDuration
        }
    )

    val subtractDuration: (Duration) -> Duration = function(
        command = { minusDuration -> mk_Duration(duration.milliseconds - minusDuration.milliseconds) },
        pre = { minusDuration -> duration >= minusDuration },
//          post = { subtractDuration, result -> result.functions.addDuration(duration) == d }
//          This post condition uses a function whose post condition uses this function as a post condition.
//          If not commented, the two functions will recur until a stack overflow error occurs.
//          However, it is still a valid post condition so is left here for completeness.

    )

    val multiply: (nat) -> Duration = function(
        command = { n -> mk_Duration(duration.milliseconds * n) },
        post = { n, result -> (n != 0uL) implies { result.functions.divide(n) == duration } }
    )

    val divide: (nat) -> Duration = function(
        command = { n -> mk_Duration(duration.milliseconds / n) },
        pre = { n -> n != 0uL }
//        post = { n, result -> result.functions.multiply(n) <= duration && duration < result.functions.multiply(n+1)}
//        This post condition uses a function whose post condition uses this function as a post condition.
//        If not commented, the two functions will recur until a stack overflow error occurs.
//        However, it is still a valid post condition so is left here for completeness.
    )

    val modSeconds: () -> Duration = function(
        command = { mk_Duration(duration.milliseconds % OneSecondDuration.milliseconds) },
        post = { result -> result < OneSecondDuration }
    )

    val modMinutes: () -> Duration = function(
        command = { mk_Duration(duration.milliseconds % OneMinuteDuration.milliseconds) },
        post = { result -> result < OneMinuteDuration }
    )
    val modHours: () -> Duration = function(
        command = { mk_Duration(duration.milliseconds % OneHourDuration.milliseconds) },
        post = { result -> result < OneHourDuration }
    )
    val modDays: () -> Duration = function(
        command = { mk_Duration(duration.milliseconds % OneDayDuration.milliseconds) },
        post = { result -> result < OneDayDuration }
    )

}

object DurationUtiltites {

    val minDuration: (Set1<Duration>) -> Duration = function(
        command = { durations -> durations.min() },
        post = { durations, result -> result in durations && forall(durations) { result <= it } }
    )
    val maxDuration: (Set1<Duration>) -> Duration = function(
        command = { durations -> durations.max() },
        post = { durations, result -> result in durations && forall(durations) { result >= it } }
    )
    val sumDuration: (Sequence<Duration>) -> Duration = function(
        command = { durationSequence -> mk_Duration((seq(durationSequence) { it.milliseconds }).sum()) },
        post = { durationSequence, result -> forall(durationSequence) { result >= it } }
    )
    val durationDiff: (Duration, Duration) -> Duration = function(
        command = { duration1, duration2 ->
            val diff = if (duration1 > duration2) {
                duration1.milliseconds - duration2.milliseconds
            } else {
                duration2.milliseconds - duration1.milliseconds
            }
            mk_Duration(diff)
        },
        post = { duration1, duration2, difference ->
            val smallerDuration = minDuration(mk_Set1(duration1, duration2))
            val largerDuration = maxDuration(mk_Set1(duration1, duration2))
            smallerDuration.functions.addDuration(difference) == largerDuration
        }
    )
}