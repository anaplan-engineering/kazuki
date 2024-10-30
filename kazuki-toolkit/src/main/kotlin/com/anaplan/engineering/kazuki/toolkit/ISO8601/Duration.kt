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

        val durationUpToMonth: (Year, Month) -> Duration = function(
            command = { year, month -> sumDuration(seq(1 until month) { fromMonth(year, it) }) }
        )

        val fromYear: (Year) -> Duration = function(
            command = { year -> fromDays(daysInYear(year).toLong()) }
        )

        val durationUpToYear: (Year) -> Duration = function(
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

        val toMonthInYear: (Year) -> nat1 = function(
            command = { year ->
                (set(1..MonthsPerYear, filter = { durationUpToMonth(year, it) <= duration }) { it }).max() - 1
            },
            pre = { year -> duration < fromYear(year) }
        )

        val toYear: (Year) -> nat by lazy {
            function(
                command = { year ->
                    if (duration < fromYear(year)) {
                        0
                    } else {
                        1 + duration.functions.subtractDuration(fromYear(year)).functions.toYear(year + 1)
                    }
                },
//                    This post condition is slow and toYear() is recursive, so it repeats many times
//                    post = { year, result ->
//                        durationUpToYear(year + result).functions.subtract(durationUpToYear(year)) <= d &&
//                                durationUpToYear(year + result + 1).functions.subtract(durationUpToYear(year)) > d
//                    },
                measure = { year -> LastYear - year },
            )
        }

        val toDtg: () -> Dtg = function(
            command = {
                val days = fromDays(duration.functions.toDays())
                mk_Dtg(days.functions.toDate(), duration.functions.modDays().functions.toTime())
            },
            post = { result -> result.functions.toDuration() == duration }
        )

        val toDate: () -> Date = function(
            command = {
                val year = duration.functions.toYear(FirstYear)
                val durationModYear = duration.functions.subtractDuration(durationUpToYear(year))
                val month = durationModYear.functions.toMonthInYear(year) + 1
                val day = (durationModYear.functions.subtractDuration(
                    durationUpToMonth(
                        year,
                        month
                    )
                ).functions.toDays() + 1).toInt()
                mk_Date(year, month, day)
            },
            post = { result ->
                result.functions.toDuration() <= duration &&
                        duration < result.functions.toDuration().functions.addDuration(OneDayDuration)
            }
        )

        val toTime: () -> Time = function(
            command = {
                val hour = duration.functions.toHours().toInt()
                val minute = duration.functions.modHours().functions.toMinutes().toInt()
                val second = duration.functions.modMinutes().functions.toSeconds().toInt()
                val millisecond = duration.functions.modSeconds().functions.toMillis().toInt()
                mk_Time(hour, minute, second, millisecond)
            },
            post = { result -> result.functions.toDuration() == duration }
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
            command = { subtractDuration -> mk_Duration(duration.milliseconds - subtractDuration.milliseconds) },
            pre = { subtractDuration -> duration >= subtractDuration },
//        post = { subtractDuration, result -> result.functions.addDuration(duration) == d }
        )

        val multiply: (nat) -> Duration = function(
            command = { n -> mk_Duration(duration.milliseconds * n) },
            post = { n, result -> result.functions.divide(n) == duration }
        )

        val divide: (nat) -> Duration = function(
            command = { n -> mk_Duration(duration.milliseconds / n) },
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
                val timeOfDay = duration.functions.modDays().functions.toTime()
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
    command = { durationSequence -> mk_Duration((seq(durationSequence) { it.milliseconds }).sum()) }
)

val durationDiff: (Duration, Duration) -> Duration = function(
    command = { duration1, duration2 -> mk_Duration(abs(duration1.milliseconds - duration2.milliseconds)) },
    post = { duration1, duration2, result ->
        ((duration1 <= duration2) implies (duration1.functions.addDuration(result) == duration2)) &&
                ((duration2 <= duration1) implies (duration2.functions.addDuration(result) == duration1))
    }
)
