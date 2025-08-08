package com.anaplan.engineering.kazuki.toolkit.iso8601

import com.anaplan.engineering.kazuki.core.FunctionProvider
import com.anaplan.engineering.kazuki.core.Invariant
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.bool
import com.anaplan.engineering.kazuki.core.function
import com.anaplan.engineering.kazuki.core.iff
import com.anaplan.engineering.kazuki.toolkit.iso8601.DtgUtilities.durationBetween
import com.anaplan.engineering.kazuki.toolkit.iso8601.DtgUtilities.isEarlierThanOrEqual


@Module
interface Interval {
    val begins: Dtg
    val ends: Dtg

    @Invariant
    fun zeroSizeInterval() = begins != ends

    @Invariant
    fun beginAfterEnd() = begins.isEarlierThanOrEqual(ends)

    @FunctionProvider(IntervalFunctions::class)
    val functions: IntervalFunctions

    @FunctionProvider(IntervalProperties::class)
    val properties: IntervalProperties

}

open class IntervalProperties(private val interval: Interval) {
    val formatted by lazy {
        interval.begins.properties.formatted + "/" + interval.ends.properties.formatted
    }

    val duration by lazy { durationBetween(interval.begins, interval.ends) }
}

open class IntervalFunctions(private val interval: Interval) {

    val within: (Interval) -> bool = function(
        command = { containerInterval ->
            containerInterval.begins.isEarlierThanOrEqual(interval.begins) &&
                    interval.ends.isEarlierThanOrEqual(containerInterval.ends)
        },
        post = { containerInterval, result ->
            result iff (containerInterval.begins.isEarlierThanOrEqual(interval.begins)
                    && interval.ends.isEarlierThanOrEqual(containerInterval.ends))
        }
    )

    val overlap: (Interval) -> bool = function(
        command = { otherInterval ->
            otherInterval.begins.functions.isEarlierThan(interval.ends) &&
                    interval.begins.functions.isEarlierThan(otherInterval.ends)
        },
        post = { otherInterval, result ->
            result == (otherInterval.begins.functions.isEarlierThan(interval.ends) &&
                    interval.begins.functions.isEarlierThan(otherInterval.ends)) }
    )

    val finestGranularity: (Duration) -> bool = function(
        command = { granularity ->
            interval.begins.functions.finestGranularity(granularity) &&
                    interval.ends.functions.finestGranularity(granularity)
        },
        pre = { granularity -> granularity != NoDuration }
    )

    val contains: (Dtg) -> bool = function(
        command = { dtg -> dtg.functions.inInterval(interval) },
//          post = {dtg, result -> dtg.functions.inInterval(interval) == result}
//          This post condition uses a function whose post condition uses this function as a post condition.
//          If not commented, the two functions will recur until a stack overflow error occurs.
//          However, it is still a valid post condition so is left here for completeness.
    )
}


