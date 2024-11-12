package com.anaplan.engineering.kazuki.toolkit.ISO8601

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Date_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Dtg_Module.mk_Dtg
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Duration_Module.mk_Duration
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Time_Module.mk_Time
import kotlin.math.abs

@Module
interface Duration : Comparable<Duration> {

    val milliseconds: Long

    @Invariant
    fun millisNonNegative() = milliseconds >= 0

    override fun compareTo(other: Duration) = milliseconds.compareTo(other.milliseconds)

    companion object {

        val fromMillis: (Long) -> Duration = function(
            command = { milliseconds -> mk_Duration(milliseconds) },
//        post = { millisecond, result -> result.functions.toMillis() == millisecond }
        )

        val fromSeconds: (Long) -> Duration = function(
            command = { seconds -> fromMillis(seconds * MillisPerSecond) },
//        post = { second, result -> result.functions.toSeconds() == second }
        )

        val fromMinutes: (Long) -> Duration = function(
            command = { minutes -> fromSeconds(minutes * SecondsPerMinute) },
//        post = { minutes, result -> result.functions.toMinutes() == minutes }
        )

        val fromHours: (Long) -> Duration = function(
            command = { hours -> fromMinutes(hours * MinutesPerHour) },
//        post = { hour, result -> result.functions.toHours() == hour }
        )

        val fromDays: (Long) -> Duration = function(
            command = { days -> fromHours(days * HoursPerDay) },
//        post = { day, result -> result.functions.toDays() == day }
        )

        val fromMonth: (Year, Month) -> Duration = function(
            command = { year, month -> fromDays(daysInMonth(year, month).toLong()) }
        )

        val durationInYearUpToStartOfMonth: (Year, Month) -> Duration = function(
            command = { year, month -> sumDuration(seq(1 until month) { fromMonth(year, it) }) }
        )

        val fromYear: (Year) -> Duration = function(
            command = { year -> fromDays(daysInYear(year).toLong()) }
        )

        val durationFromFirstYearUpToStartOfYear: (Year) -> Duration = function(
            command = { year -> sumDuration(seq(FirstYear until year) { fromYear(it) }) }
        )
    }

    @FunctionProvider(DurationFunctions::class)
    val functions: DurationFunctions

    class DurationFunctions(private val duration: Duration) {
        val toMillis: () -> Long = function(
            command = { duration.milliseconds },
            post = { result -> fromMillis(result) == duration }
        )

        val toSeconds: () -> Long = function(
            command = { toMillis() / MillisPerSecond },
            post = { result -> fromSeconds(result) <= duration && duration < fromSeconds(result + 1) }
        )

        val toMinutes: () -> Long = function(
            command = { toSeconds() / SecondsPerMinute },
            post = { result -> fromMinutes(result) <= duration && duration < fromMinutes(result + 1) }
        )

        val toHours: () -> Long = function(
            command = { toMinutes() / MinutesPerHour },
            post = { result -> fromHours(result) <= duration && duration < fromHours(result + 1) }
        )

        val toDays: () -> Long = function(
            command = { toHours() / HoursPerDay },
            post = { result -> fromDays(result) <= duration && duration < fromDays(result + 1) }
        )

        val toMonthsInGivenYear: (Year) -> nat1 = function(
            command = { year ->
                (set(
                    1..MonthsPerYear,
                    filter = { durationInYearUpToStartOfMonth(year, it) <= duration }) { it }).max() - 1
            },
            pre = { year -> duration < fromYear(year) }
        )

        val toYearsAfterFirstYear: () -> nat = function<nat>(
            command = { toYearsAfterGivenYear(FirstYear) }
        )

        val toYearsAfterGivenYear: (Year) -> nat by lazy {
            function(
                command = { givenYear ->
                    if (duration < fromYear(givenYear)) {
                        0
                    } else {
                        1 + duration.functions.subtractDuration(fromYear(givenYear)).functions.toYearsAfterGivenYear(
                            givenYear + 1
                        )
                    }
                },
                measure = { year -> LastYear - year },
            )
        }

        val toDtgAfterFirstDtg: () -> Dtg = function(
            command = { toDtgAfterGivenDtg(FirstDtg) },
            pre = { duration <= LastDtg.functions.toDurationSinceFirstDtg() },
            post = { result -> result.functions.toDurationSinceFirstDtg() == duration }
        )

        val toDtgAfterGivenDtg: (Dtg) -> Dtg = function(
            command = { givenDtg ->
                val totalDuration = givenDtg.functions.toDurationSinceFirstDtg().functions.addDuration(duration)
                val daysDuration = fromDays(totalDuration.functions.toDays())
                val timeDuration = totalDuration.functions.modDays()
                mk_Dtg(daysDuration.functions.toDateAfterFirstDate(), timeDuration.functions.toTimeAfterFirstTime())
            },
            pre = { givenDtg ->
                val maxDuration = LastDtg.functions.toDurationSinceFirstDtg()
                val totalDuration = givenDtg.functions.toDurationSinceFirstDtg().functions.addDuration(duration)
                totalDuration <= maxDuration
            }
        )

        val toDateAfterFirstDate: () -> Date = function(
            command = { toDateAfterGivenDate(FirstDate) },
            pre = {
                val maxDuration = LastDate.functions.toDurationSinceFirstDate().functions.addDuration(OneDayDuration)
                duration < maxDuration
            },
            post = { result ->
                result.functions.toDurationSinceFirstDate() <= duration &&
                        duration < result.functions.toDurationSinceFirstDate().functions.addDuration(OneDayDuration)
            }
        )

        val toDateAfterGivenDate: (Date) -> Date = function(
            command = { givenDate ->
                val totalDuration = givenDate.functions.toDurationSinceFirstDate().functions.addDuration(duration)
                val year = totalDuration.functions.toYearsAfterGivenYear(FirstYear)
                val totalDurationModYear =
                    totalDuration.functions.subtractDuration(durationFromFirstYearUpToStartOfYear(year))
                val month = totalDurationModYear.functions.toMonthsInGivenYear(year) + 1
                val day = (totalDurationModYear.functions.subtractDuration(
                    durationInYearUpToStartOfMonth(year, month)
                ).functions.toDays() + 1).toInt()
                mk_Date(year, month, day)
            },
            pre = { givenDate ->
                val totalDuration = givenDate.functions.toDurationSinceFirstDate().functions.addDuration(duration)
                val maxDuration = LastDate.functions.toDurationSinceFirstDate().functions.addDuration(OneDayDuration)
                totalDuration < maxDuration
            }
        )

        val toTimeAfterFirstTime: () -> Time = function(
            command = {
                toTimeAfterGivenTime(FirstTime)
            },
            pre = { duration < OneDayDuration },
            post = { result -> result.functions.toDurationSinceFirstTime() == duration }
        )

        val toTimeAfterGivenTime: (Time) -> Time = function(
            command = { givenTime ->
                val totalDuration = givenTime.functions.toDurationSinceFirstTime().functions.addDuration(duration)
                val hour = totalDuration.functions.toHours().toInt()
                val minute = totalDuration.functions.modHours().functions.toMinutes().toInt()
                val second = totalDuration.functions.modMinutes().functions.toSeconds().toInt()
                val millisecond = totalDuration.functions.modSeconds().functions.toMillis().toInt()
                mk_Time(hour, minute, second, millisecond)
            },
            pre = { givenTime ->
                val totalDuration = givenTime.functions.toDurationSinceFirstTime().functions.addDuration(duration)
                totalDuration < OneDayDuration
            },
        )

        val addDuration: (Duration) -> Duration = function(
            command = { plusDuration -> mk_Duration(duration.milliseconds + plusDuration.milliseconds) },
            post = { plusDuration, result ->
//            durationDiff(result, d) == duration && durationDiff(result, duration) == d &&
                result.functions.subtractDuration(plusDuration) == duration
                        && result.functions.subtractDuration(duration) == plusDuration
            }
        )

        val subtractDuration: (Duration) -> Duration = function(
            command = { minusDuration -> mk_Duration(duration.milliseconds - minusDuration.milliseconds) },
            pre = { minusDuration -> duration >= minusDuration },
//        post = { subtractDuration, result -> result.functions.addDuration(duration) == d }
        )

        val multiply: (nat) -> Duration = function(
            command = { n -> mk_Duration(duration.milliseconds * n) },
            post = { n, result -> (n != 0) implies { result.functions.divide(n) == duration } }
        )

        val divide: (nat) -> Duration = function(
            command = { n -> mk_Duration(duration.milliseconds / n) },
            pre = { n -> n != 0 }
//        post = { n, result -> result.functions.multiply(n) <= duration && duration < result.functions.multiply(n+1)}
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

        private val formatItem: (nat, Char) -> String = function(
            command = { n, c -> if (n == 0) "" else String.format("%d%s", n, c) }
        )

        private val formatItemSec: (nat, nat) -> String = function(
            command = { seconds, milliseconds -> String.format("%d.%03dS", seconds, milliseconds) }
        )

        val format: () -> String = function<String>(
            command = {
                val numDays = duration.functions.toDays().toInt()
                val timeOfDay = duration.functions.modDays().functions.toTimeAfterFirstTime()
                val date = formatItem(numDays, 'D')
                val time = formatItem(timeOfDay.hour, 'H') + formatItem(timeOfDay.minute, 'M') +
                        if (timeOfDay.millisecond == 0) {
                            formatItem(timeOfDay.second, 'S')
                        } else {
                            formatItemSec(timeOfDay.second, timeOfDay.millisecond)
                        }
                if (date == "" && time == "") "PT0S" else "P$date${if (time == "") "" else "T$time"}"
            }
        )
    }
}

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
    command = { duration1, duration2 -> mk_Duration(abs(duration1.milliseconds - duration2.milliseconds)) },
    post = { duration1, duration2, difference ->
        val smallerDuration = minDuration(mk_Set1(duration1, duration2))
        val largerDuration = maxDuration(mk_Set1(duration1, duration2))
        smallerDuration.functions.addDuration(difference) == largerDuration
    }
)