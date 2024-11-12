package com.anaplan.engineering.kazuki.toolkit.ISO8601

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.ISO8601.NormalisedTime_Module.mk_NormalisedTime


@Module
interface Time : Comparable<Time> {
    val hour: Hour
    val minute: Minute
    val second: Second
    val millisecond: Millisecond

    private val timeComparator
        get() = compareBy<Time> { it.hour }.thenBy { it.minute }.thenBy { it.second }.thenBy { it.millisecond }

    override fun compareTo(other: Time) = timeComparator.compare(this, other)

    @FunctionProvider(TimeFunctions::class)
    val functions: TimeFunctions

    class TimeFunctions(private val time: Time) {
        val toDurationSinceFirstTime: () -> Duration = function<Duration>(
            command = {
                Duration.fromHours(time.hour.toLong()).functions.addDuration(
                    Duration.fromMinutes(time.minute.toLong()).functions.addDuration(
                        Duration.fromSeconds(time.second.toLong()).functions.addDuration(
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

    val normalisedTime: Time get() = functions.normaliseTimeInZone().time

    override fun compareTo(other: TimeInZone) = normalisedTime.compareTo(other.normalisedTime)

    @FunctionProvider(TimeInZoneFunctions::class)
    val functions: TimeInZoneFunctions

    class TimeInZoneFunctions(private val timeInZone: TimeInZone) {

        val toNormalisedDurationSinceFirstTime: () -> Duration = function(
            command = { timeInZone.functions.normaliseTimeInZone().time.functions.toDurationSinceFirstTime() },
            post = { result -> result.functions.toTimeAfterFirstTime() == timeInZone.normalisedTime }
        )

        private val normaliseTimeInZonePlus: (Duration, Duration) -> NormalisedTime = function(
            command = { utcTimeDuration, offsetDuration ->
                if (offsetDuration <= utcTimeDuration) mk_NormalisedTime(
                    utcTimeDuration.functions.subtractDuration(offsetDuration).functions.toTimeAfterFirstTime(),
                    OffsetDirection.None
                ) else mk_NormalisedTime(
                    utcTimeDuration.functions.addDuration(OneDayDuration).functions.subtractDuration(offsetDuration).functions.toTimeAfterFirstTime(),
                    OffsetDirection.Plus
                )
            }
        )
        private val normaliseTimeInZoneMinus: (Duration, Duration) -> NormalisedTime = function(
            command = { utcTimeDuration, offsetDuration ->
                val adjusted = utcTimeDuration.functions.addDuration(offsetDuration)
                if (adjusted < OneDayDuration) mk_NormalisedTime(
                    adjusted.functions.toTimeAfterFirstTime(),
                    OffsetDirection.None
                ) else mk_NormalisedTime(
                    adjusted.functions.subtractDuration(OneDayDuration).functions.toTimeAfterFirstTime(),
                    OffsetDirection.Minus
                )
            }
        )

        val normaliseTimeInZone: () -> NormalisedTime = function<NormalisedTime>(
            command = {
                val utcTimeDuration = timeInZone.time.functions.toDurationSinceFirstTime()
                val offsetDuration = timeInZone.offset.offsetDuration
                val directionOfOffset = timeInZone.offset.offsetDirection
                when (directionOfOffset) {
                    OffsetDirection.Plus -> normaliseTimeInZonePlus(utcTimeDuration, offsetDuration)
                    OffsetDirection.Minus -> normaliseTimeInZoneMinus(utcTimeDuration, offsetDuration)
                    OffsetDirection.None -> mk_NormalisedTime(timeInZone.time, OffsetDirection.None)
                }
            }
        )
        val format: () -> String = function<String>(
            command = {
                timeInZone.time.functions.format() +
                        if (timeInZone.offset.offsetDuration != NoDuration) timeInZone.offset.functions.format() else "Z"
            }
        )
    }
}

@Module
interface NormalisedTime {
    val time: Time
    val plusOrMinusADay: PlusOrMinus
}

@Module
interface Offset {
    val offsetDuration: Duration
    val offsetDirection: OffsetDirection

    @Invariant
    fun offsetMoreThanDay() = offsetDuration < OneDayDuration

    @Invariant
    fun offsetGranularityTooFine() = offsetDuration.functions.modMinutes() == NoDuration

    @FunctionProvider(OffsetFunctions::class)
    val functions: OffsetFunctions

    class OffsetFunctions(private val offset: Offset) {

        val format: () -> String = function<String>(
            command = {
                val hourMinute = offset.offsetDuration.functions.toTimeAfterFirstTime()
                val sign = when (offset.offsetDirection) {
                    OffsetDirection.Plus -> "+"; OffsetDirection.Minus -> "-"; OffsetDirection.None -> ""
                }
                String.format("%s%02d:%02d", sign, hourMinute.hour, hourMinute.minute)
            }
        )
    }
}

@PrimitiveInvariant(name = "Hour", base = nat::class)
fun hourNotInRange(hour: nat) = hour < HoursPerDay

@PrimitiveInvariant(name = "Minute", base = nat::class)
fun minuteNotInRange(minute: nat) = minute < MinutesPerHour

@PrimitiveInvariant(name = "Second", base = nat::class)
fun secondNotInRange(second: nat) = second < SecondsPerMinute

@PrimitiveInvariant(name = "Millisecond", base = nat::class)
fun millisecondNotInRange(millisecond: nat) = millisecond < MillisPerSecond

val minTime: (Set1<Time>) -> Time = function(
    command = { times -> times.min() },
    post = { times, result -> result in times && forall(times) { result <= it } }
)
val maxTime: (Set1<Time>) -> Time = function(
    command = { times -> times.max() },
    post = { times, result -> result in times && forall(times) { result >= it } }
)
