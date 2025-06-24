package com.anaplan.engineering.kazuki.toolkit.iso8601

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.iso8601.NormalisedTime_Module.mk_NormalisedTime


@Module
interface Time : Comparable<Time>, PrettyPrintable {
    val hour: Hour
    val minute: Minute
    val second: Second
    val millisecond: Millisecond

    private val timeComparator
        get() = compareBy<Time> { it.hour }.thenBy { it.minute }.thenBy { it.second }.thenBy { it.millisecond }

    override fun compareTo(other: Time) = timeComparator.compare(this, other)

    override fun pretty() = properties.formatted

    @FunctionProvider(TimeFunctions::class)
    val functions: TimeFunctions

    @FunctionProvider(TimeProperties::class)
    val properties: TimeProperties
}

class TimeProperties(private val time: Time) {
    val durationSinceFirstTime by lazy {
        Duration.fromHours(time.hour).functions.addDuration(
            Duration.fromMinutes(time.minute).functions.addDuration(
                Duration.fromSeconds(time.second).functions.addDuration(
                    Duration.fromMillis(time.millisecond)
                )
            )
        )
    }

    val formatted by lazy {
        val milliseconds = if (time.millisecond == 0uL) "" else String.format(".%03d", time.millisecond.safeToInt())
        String.format(
            "%02d:%02d:%02d%s",
            time.hour.safeToInt(),
            time.minute.safeToInt(),
            time.second.safeToInt(),
            milliseconds
        )
    }
}

class TimeFunctions(private val time: Time) {

}

@Module
interface TimeInZone : Comparable<TimeInZone> {
    val time: Time
    val offset: Offset

    // TODO -- surely this doesn't account for day offset?
    override fun compareTo(other: TimeInZone) =
        properties.normalisedTime.time.compareTo(other.properties.normalisedTime.time)

    @FunctionProvider(TimeInZoneFunctions::class)
    val functions: TimeInZoneFunctions

    @FunctionProvider(TimeInZoneProperties::class)
    val properties: TimeInZoneProperties

}

class TimeInZoneProperties(private val timeInZone: TimeInZone) {

    val formatted by lazy {
        timeInZone.time.properties.formatted +
                if (timeInZone.offset.offsetDuration != NoDuration) timeInZone.offset.properties.formatted else "Z"
    }

    val normalisedDurationSinceFirstTime by lazy { normalisedTime.time.properties.durationSinceFirstTime }

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

    val normalisedTime by lazy {
        val utcTimeDuration = timeInZone.time.properties.durationSinceFirstTime
        val offsetDuration = timeInZone.offset.offsetDuration
        val directionOfOffset = timeInZone.offset.offsetDirection
        when (directionOfOffset) {
            OffsetDirection.Plus -> normaliseTimeInZonePlus(utcTimeDuration, offsetDuration)
            OffsetDirection.Minus -> normaliseTimeInZoneMinus(utcTimeDuration, offsetDuration)
            OffsetDirection.None -> mk_NormalisedTime(timeInZone.time, OffsetDirection.None)
        }
    }
}

class TimeInZoneFunctions(private val timeInZone: TimeInZone)

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

    @FunctionProvider(OffsetProperties::class)
    val properties: OffsetProperties

}

class OffsetProperties(private val offset: Offset) {

    val formatted by lazy {
        val hourMinute = offset.offsetDuration.functions.toTimeAfterFirstTime()
        val sign = when (offset.offsetDirection) {
            OffsetDirection.Plus -> "+"; OffsetDirection.Minus -> "-"; OffsetDirection.None -> ""
        }
        String.format("%s%02d:%02d", sign, hourMinute.hour.safeToInt(), hourMinute.minute.safeToInt())
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

object TimeUtilities {
    val minTime: (Set1<Time>) -> Time = function(
        command = { times -> times.min() },
        post = { times, result -> result in times && forall(times) { result <= it } }
    )
    val maxTime: (Set1<Time>) -> Time = function(
        command = { times -> times.max() },
        post = { times, result -> result in times && forall(times) { result >= it } }
    )
}
