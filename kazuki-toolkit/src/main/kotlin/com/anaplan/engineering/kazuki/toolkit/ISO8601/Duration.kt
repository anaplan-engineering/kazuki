package com.anaplan.engineering.kazuki.toolkit.ISO8601

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.ISO8601.DTG_Module.mk_DTG
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Date_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Duration_Module.mk_Duration
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Time_Module.mk_Time
import kotlin.math.abs

@Module
interface Duration : Comparable<Duration> {

    @ComparableProperty
    val duration_ms: Long

    override fun compareTo(other: Duration) = duration_ms.compareTo(other.duration_ms)

    companion object {

        val fromMillis: (Long) -> Duration = function(
            command = { millisecond: Long -> mk_Duration(millisecond) },
//        post = { millisecond, result -> result.functions.toMillis() == millisecond }
        )

        val fromSeconds: (Long) -> Duration = function(
            command = { second: Long -> fromMillis(second * MILLIS_PER_SECOND) },
//        post = { second, result -> result.functions.toSeconds() == second }
        )

        val fromMinutes: (Long) -> Duration = function(
            command = { minutes: Long -> fromSeconds(minutes * SECONDS_PER_MINUTE) },
//        post = { minutes, result -> result.functions.toMinutes() == minutes }
        )

        val fromHours: (Long) -> Duration = function(
            command = { hour: Long -> fromMinutes(hour * MINUTES_PER_HOUR) },
//        post = { hour, result -> result.functions.toHours() == hour }
        )

        val fromDays: (Long) -> Duration = function(
            command = { day: Long -> fromHours(day * HOURS_PER_DAY) },
//        post = { day, result -> result.functions.toDays() == day }
        )

        val fromMonth: (Year, Month) -> Duration = function(
            command = { year: Year, month: Month -> fromDays(daysInMonth(year, month).toLong()) }
        )

        val durationUpToMonth: (Year, Month) -> Duration = function(
            command = { year: Year, month: Month -> sumDuration(seq(1 until month) { fromMonth(year, it) }) }
        )

        val fromYear: (Year) -> Duration = function(
            command = { year: Year -> fromDays(daysInYear(year).toLong()) }
        )

        val durationUpToYear: (Year) -> Duration = function(
            command = { year: Year -> sumDuration(seq(FirstYear until year) { fromYear(it) }) }
        )
    }

    @FunctionProvider(DurationFunctions::class)
    val functions: DurationFunctions

    class DurationFunctions(private val duration: Duration) {
        val toMillis: () -> Long = function(
            command = { duration.duration_ms },
            post = { result -> fromMillis(result) == duration }
        )

        val toSeconds: () -> Long = function(
            command = { duration.functions.toMillis() / MILLIS_PER_SECOND },
            post = { result -> fromSeconds(result) <= duration && duration < fromSeconds(result + 1) }
        )

        val toMinutes: () -> Long = function(
            command = { duration.functions.toSeconds() / SECONDS_PER_MINUTE },
            post = { result -> fromMinutes(result) <= duration && duration < fromMinutes(result + 1) }
        )

        val toHours: () -> Long = function(
            command = { duration.functions.toMinutes() / MINUTES_PER_HOUR },
            post = { result -> fromHours(result) <= duration && duration < fromHours(result + 1) }
        )

        val toDays: () -> Long = function(
            command = { duration.functions.toHours() / HOURS_PER_DAY },
            post = { result -> fromDays(result) <= duration && duration < fromDays(result + 1) }
        )

        val toMonth: (Year) -> nat1 = function(
            command = { year: Year ->
                (set(1..MONTHS_PER_YEAR, filter = { durationUpToMonth(year, it) <= duration }) { it }).max() - 1
            },
            pre = { year -> duration < fromYear(year) }
        )

        val toYear: (Year) -> nat by lazy {
            function(
                command = { year: Year ->
                    if (duration < fromYear(year)) {
                        0
                    } else {
                        1 + durationDiff(duration, fromYear(year)).functions.toYear(year + 1)
                    }
                },
//                    This post condition is slow and toYear() is recursive, so it repeats many times
//                    post = { year, result ->
//                        durUpToYear(year + result).functions.subtract(durUpToYear(year)) <= d &&
//                                durUpToYear(year + result + 1).functions.subtract(durUpToYear(year)) > d
//                    },
                measure = { year -> LastYear - year },
            )
        }

        val toDTG: () -> DTG = function(
            command = {
                val days = fromDays(duration.functions.toDays())
                mk_DTG(days.functions.toDate(), durationDiff(duration, days).functions.toTime())
            },
            post = { result -> result.functions.toDuration() == duration }
        )

        val toDate: () -> Date = function(
            command = {
                val year = duration.functions.toYear(FirstYear)
                val yearDur = durationDiff(duration, durationUpToYear(year))
                val month = yearDur.functions.toMonth(year) + 1
                val day = (durationDiff(yearDur, durationUpToMonth(year, month)).functions.toDays() + 1).toInt()
                mk_Date(year, month, day)
            },
            post = { result ->
                result.functions.toDuration() <= duration &&
                        duration < result.functions.toDuration().functions.add(ONE_DAY)
            }
        )

        val toTime: () -> Time = function(
            command = {
                val hour = duration.functions.toHours().toInt()
                val minute = durationDiff(duration, fromHours(hour.toLong())).functions.toMinutes().toInt()
                val hmd = fromHours(hour.toLong()).functions.add(fromMinutes(minute.toLong()))
                val second = durationDiff(duration, hmd).functions.toSeconds().toInt()
                val millisecond =
                    durationDiff(duration, hmd.functions.add(fromSeconds(second.toLong()))).functions.toMillis().toInt()
                mk_Time(hour, minute, second, millisecond)
            },
            post = { result -> result.functions.toDuration() == duration }
        )

        val add: (Duration) -> Duration = function(
            command = { plusDuration: Duration -> mk_Duration(duration.duration_ms + plusDuration.duration_ms) },
            post = { plusDuration, result ->
//            durDiff(result, d) == duration && durDiff(result, duration) == d &&
                result.functions.subtract(plusDuration) == duration && result.functions.subtract(duration) == plusDuration
            }
        )

        val subtract: (Duration) -> Duration = function(
            command = { subtractDuration: Duration -> mk_Duration(duration.duration_ms - subtractDuration.duration_ms) },
            pre = { subtractDuration -> duration >= subtractDuration },
//        post = { subtractDuration, result -> result.functions.add(dur) == d }
        )

        val multiply: (nat) -> Duration = function(
            command = { n: nat -> mk_Duration(duration.duration_ms * n) },
            post = { n, result -> result.functions.divide(n) == duration }
        )

        val divide: (nat) -> Duration = function(
            command = { n: nat -> mk_Duration(duration.duration_ms / n) },
//        post = { n, result -> result.functions.multiply(n) <= duration && duration < result.functions.multiply(n+1)}
        )

        val modMinutes: () -> Duration = function(
            command = { mk_Duration(duration.duration_ms % ONE_MINUTE.duration_ms) },
            post = { result -> result < ONE_MINUTE }
        )
        val modHours: () -> Duration = function(
            command = { mk_Duration(duration.duration_ms % ONE_HOUR.duration_ms) },
            post = { result -> result < ONE_HOUR }
        )
        val modDays: () -> Duration = function(
            command = { mk_Duration(duration.duration_ms % ONE_DAY.duration_ms) },
            post = { result -> result < ONE_DAY }
        )

        private val formatItem: (nat, Char) -> String = function(
            command = { n: nat, c: Char -> if (n == 0) "" else String.format("%d%s", n, c) }
        )

        private val formatItemSec: (nat, nat) -> String = function(
            command = { seconds: nat, milliseconds: nat -> String.format("%d.%03dS", seconds, milliseconds) }
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
    command = { durationSet1: Set1<Duration> -> mk_Duration((set(durationSet1) { it.duration_ms }).min()) },
    post = { durationSet1, result -> result in durationSet1 && forall(durationSet1) { result <= it } }
)
val maxDuration: (Set1<Duration>) -> Duration = function(
    command = { durationSet1: Set1<Duration> -> mk_Duration((set(durationSet1) { it.duration_ms }).max()) },
    post = { durationSet1, result -> result in durationSet1 && forall(durationSet1) { result >= it } }
)
val sumDuration: (Sequence<Duration>) -> Duration = function(
    command = { durationSequence: Sequence<Duration> -> mk_Duration((seq(durationSequence) { it.duration_ms }).sum()) }
)

val durationDiff: (Duration, Duration) -> Duration = function(
    command = { dur1: Duration, dur2: Duration -> mk_Duration(abs(dur1.duration_ms - dur2.duration_ms)) },
    post = { dur1, dur2, result ->
        ((dur1 <= dur2) implies (dur1.functions.add(result) == dur2)) &&
                ((dur2 <= dur1) implies (dur2.functions.add(result) == dur1))
    }
)
