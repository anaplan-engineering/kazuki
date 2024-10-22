package com.anaplan.engineering.kazuki.toolkit.ISO8601

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Offset_Module.mk_Offset

@Module
interface Time : Comparable<Time> {
    val hour: Hour
    val minute: Minute
    val second: Second
    val millisecond: Millisecond

    @ComparableProperty
    val duration_ms: Long get() = functions.toDuration().duration_ms

    override fun compareTo(other: Time) = duration_ms.compareTo(other.duration_ms)

    @FunctionProvider(TimeFunctions::class)
    val functions: TimeFunctions

    class TimeFunctions(private val time: Time) {
        val toDuration: () -> Duration = function<Duration>(
            command = {
                Duration.fromHours(time.hour.toLong()).functions.add(
                    Duration.fromMinutes(time.minute.toLong()).functions.add(
                        Duration.fromSeconds(time.second.toLong()).functions.add(
                            Duration.fromMillis(time.millisecond.toLong())
                        )
                    )
                )
            },
//        post = { result -> result.functions.toTime() == time }
        )

        val format: () -> String = function<String>(
            command = {
                val milliseconds = if (time.millisecond == 0) "" else String.format(".%03d", time.millisecond)
                String.format("%02d:%02d:%02d%s", time.hour, time.minute, time.second, milliseconds)
            }
        )
    }
}

@Module
interface TimeInZone : Comparable<TimeInZone> {
    val time: Time
    val offset: Offset

    val normalisedTime: Time get() = functions.normalise()._1

    @ComparableProperty
    val duration_ms: Long get() = normalisedTime.functions.toDuration().duration_ms

    override fun compareTo(other: TimeInZone) = duration_ms.compareTo(other.duration_ms)

    @FunctionProvider(TimeInZoneFunctions::class)
    val functions: TimeInZoneFunctions

    class TimeInZoneFunctions(private val timeInZone: TimeInZone) {

        val toDuration: () -> Duration = function(
            command = { timeInZone.functions.normalise()._1.functions.toDuration() },
            post = { result -> result.functions.toTime() == timeInZone.normalisedTime }
        )
        val normalise: () -> Tuple2<Time, PlusOrMinus> = function<Tuple2<Time, PlusOrMinus>>(
            command = {
                val utcTimeDuration = timeInZone.time.functions.toDuration()
                val offset = timeInZone.offset.delta
                when (timeInZone.offset) {
                    mk_Offset(offset, PlusOrMinus.Plus) ->
                        if (offset <= utcTimeDuration) {
                            mk_(utcTimeDuration.functions.subtract(offset).functions.toTime(), PlusOrMinus.None)
                        } else {
                            mk_(
                                utcTimeDuration.functions.add(ONE_DAY).functions.subtract(offset).functions.toTime(),
                                PlusOrMinus.Plus
                            )
                        }

                    mk_Offset(offset, PlusOrMinus.Minus) -> {
                        val adjusted = utcTimeDuration.functions.add(offset)
                        if (adjusted < ONE_DAY) {
                            mk_(adjusted.functions.toTime(), PlusOrMinus.None)
                        } else {
                            mk_(adjusted.functions.subtract(ONE_DAY).functions.toTime(), PlusOrMinus.Minus)
                        }
                    }

                    else -> mk_(timeInZone.time, PlusOrMinus.None)
                }
            }
        )
        val format: () -> String = function<String>(
            command = {
                timeInZone.time.functions.format() +
                        if (timeInZone.offset.delta == NO_DURATION) {
                            "Z"
                        } else {
                            timeInZone.offset.functions.format()
                        }
            }
        )
    }
}

@Module
interface Offset {
    val delta: Duration
    val pm: PlusOrMinus

    @Invariant
    fun offsetMoreThanDay() = delta < ONE_DAY

    @Invariant
    fun offsetZero() = delta.functions.modMinutes() == NO_DURATION

    @ComparableProperty
    val comp: Long
        get() = when (pm) {
            PlusOrMinus.Plus -> delta.duration_ms
            PlusOrMinus.Minus -> -delta.duration_ms
            PlusOrMinus.None -> delta.duration_ms
        }

    fun compareTo(other: Offset) = comp.compareTo(other.comp)

    @FunctionProvider(OffsetFunctions::class)
    val functions: OffsetFunctions

    class OffsetFunctions(private val offset: Offset) {

        val format: () -> String = function<String>(
            command = {
                val hourMinute = offset.delta.functions.toTime()
                val sign = when (offset.pm) {
                    PlusOrMinus.Plus -> "+"
                    PlusOrMinus.Minus -> "-"
                    else -> ""
                }
                String.format("%s%02d:%02d", sign, hourMinute.hour, hourMinute.minute)
            }
        )
    }
}

@PrimitiveInvariant(name = "Hour", base = nat::class)
fun hourNotInRange(hour: nat) = hour < HOURS_PER_DAY

@PrimitiveInvariant(name = "Minute", base = nat::class)
fun minuteNotInRange(minute: nat) = minute < MINUTES_PER_HOUR

@PrimitiveInvariant(name = "Second", base = nat::class)
fun secondNotInRange(second: nat) = second < SECONDS_PER_MINUTE

@PrimitiveInvariant(name = "Millisecond", base = nat::class)
fun millisecondNotInRange(millisecond: nat) = millisecond < MILLIS_PER_SECOND

val minTime: (Set1<Time>) -> Time = function(
    command = { times: Set1<Time> -> (set(times) { it }).min() },
    post = { times, result -> result in times && forall(times) { result <= it } }
)
val maxTime: (Set1<Time>) -> Time = function(
    command = { times: Set1<Time> -> (set(times) { it }).max() },
    post = { times, result -> result in times && forall(times) { result >= it } }
)
