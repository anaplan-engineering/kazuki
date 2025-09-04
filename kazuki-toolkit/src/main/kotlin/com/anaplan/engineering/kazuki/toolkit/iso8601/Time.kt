package com.anaplan.engineering.kazuki.toolkit.iso8601

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.iso8601.Duration_Module.mk_Duration
import com.anaplan.engineering.kazuki.toolkit.iso8601.NormalisedTime_Module.mk_NormalisedTime
import com.anaplan.engineering.kazuki.toolkit.iso8601.Time_Module.mk_Time
import java.time.LocalTime
import java.time.temporal.ChronoUnit


@Module
interface Time: PrettyPrintable {
    val hour: Hour
    val minute: Minute
    val second: Second
    val millisecond: Millisecond

    override fun pretty() = properties.formatted

    @FunctionProvider(TimeFunctions::class)
    val functions: TimeFunctions

    @FunctionProvider(TimeProperties::class)
    val properties: TimeProperties
}

class TimeProperties(private val time: Time) {
    val durationSinceFirstTime by lazy {
        mk_Duration(ChronoUnit.MILLIS.between(FirstTime.toLocalTime(), time.toLocalTime()).toNat())
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

    private val localTime by lazy { time.toLocalTime() }
    
    val isEarlierThan = function(
        command = { other: Time -> localTime < other.toLocalTime() },
        post = { other, result ->
            result iff (time.hour < other.hour || (time.hour == other.hour && (time.minute < other.minute || (time.minute == other.minute && (time.second < other.second || (time.second == other.second && time.millisecond < other.millisecond))))))
        }
    )
}

@Module
interface TimeInZone {
    val time: Time
    val offset: Offset

    // TODO -- surely this doesn't account for day offset?
//    override fun compareTo(other: TimeInZone) =
//        properties.normalisedTime.time.compareTo(other.properties.normalisedTime.time)

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
            if (offsetDuration <= utcTimeDuration) {
                mk_NormalisedTime(
                    utcTimeDuration.functions.subtract(offsetDuration).properties.timeAfterFirstTime,
                    OffsetDirection.None
                )
            } else {
                mk_NormalisedTime(
                    utcTimeDuration.functions.add(OneDayDuration).functions.subtract(offsetDuration).properties.timeAfterFirstTime,
                    OffsetDirection.Plus
                )
            }
        }
    )
    private val normaliseTimeInZoneMinus: (Duration, Duration) -> NormalisedTime = function(
        command = { utcTimeDuration, offsetDuration ->
            val adjusted = utcTimeDuration.functions.add(offsetDuration)
            if (adjusted < OneDayDuration) {
                mk_NormalisedTime(
                    adjusted.properties.timeAfterFirstTime,
                    OffsetDirection.None
                )
            } else {
                mk_NormalisedTime(
                    adjusted.functions.subtract(OneDayDuration).properties.timeAfterFirstTime,
                    OffsetDirection.Minus
                )
            }
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

class TimeInZoneFunctions(private val timeInZone: TimeInZone) {
    

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
    fun offsetGranularityTooFine() = offsetDuration.properties.modMinutes == NoDuration

    @FunctionProvider(OffsetProperties::class)
    val properties: OffsetProperties

}

class OffsetProperties(private val offset: Offset) {

    val formatted by lazy {
        val hourMinute = offset.offsetDuration.properties.timeAfterFirstTime
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

    val earliest: (Set1<Time>) -> Time = function(
        command = { times -> times.minOf { it.toLocalTime() }.toTime() },
        post = { times, result -> result in times && forall(times - result) { result.functions.isEarlierThan(it) } }
    )

    val latest: (Set1<Time>) -> Time = function(
        command = { times -> times.maxOf { it.toLocalTime() }.toTime() },
        post = { times, result -> result in times && forall(times - result) { it.functions.isEarlierThan(result) } }
    )
}



internal fun LocalTime.toTime() = mk_Time(this.hour.toNat(), this.minute.toNat(), this.second.toNat(), (this.nano / 1_000_000).toNat())

internal fun Time.toLocalTime() = LocalTime.of(this.hour.toInt(), this.minute.toInt(), this.second.toInt(), this.millisecond.toInt() * 1_000_000)