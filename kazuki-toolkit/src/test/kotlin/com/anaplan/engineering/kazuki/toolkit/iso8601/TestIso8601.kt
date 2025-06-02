package com.anaplan.engineering.kazuki.toolkit.iso8601

import com.anaplan.engineering.kazuki.core.InvariantFailure
import com.anaplan.engineering.kazuki.core.PreconditionFailure
import com.anaplan.engineering.kazuki.core.mk_Seq
import com.anaplan.engineering.kazuki.core.mk_Set1
import com.anaplan.engineering.kazuki.toolkit.iso8601.Date_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.iso8601.DtgInZone_Module.mk_DtgInZone
import com.anaplan.engineering.kazuki.toolkit.iso8601.Dtg_Module.mk_Dtg
import com.anaplan.engineering.kazuki.toolkit.iso8601.Duration_Module.mk_Duration
import com.anaplan.engineering.kazuki.toolkit.iso8601.Interval_Module.mk_Interval
import com.anaplan.engineering.kazuki.toolkit.iso8601.NormalisedTime_Module.mk_NormalisedTime
import com.anaplan.engineering.kazuki.toolkit.iso8601.Offset_Module.mk_Offset
import com.anaplan.engineering.kazuki.toolkit.iso8601.TimeInZone_Module.mk_TimeInZone
import com.anaplan.engineering.kazuki.toolkit.iso8601.Time_Module.mk_Time
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals
import kotlin.test.Test

class TestIso8601 {

    @Test
    fun constructDuration() {
        assertEquals(10uL, mk_Duration(10uL).milliseconds)
    }

    @Test
    fun t1() {
        DtgUtilities.dtgDiff(
            mk_Dtg(mk_Date(2018u, 4u, 1u), mk_Time(0u, 0u, 0u, 10u)),
            mk_Dtg(mk_Date(2018u, 4u, 1u), mk_Time(0u, 0u, 0u, 0u)),
        )
        DtgUtilities.dtgDiff(
            mk_Dtg(mk_Date(2018u, 4u, 1u), mk_Time(10u, 0u, 0u, 0u)),
            mk_Dtg(mk_Date(2018u, 4u, 1u), mk_Time(0u, 0u, 0u, 0u)),
        )
    }

    @Test
    fun timeInvariantTest() {
        assertFailsWith<InvariantFailure> { mk_Time(0u, 0u, 0u, 1000u) }
        assertFailsWith<InvariantFailure> { mk_Time(0u, 0u, 60u, 0u) }
        assertFailsWith<InvariantFailure> { mk_Time(0u, 60u, 0u, 0u) }
        assertFailsWith<InvariantFailure> { mk_Time(24u, 0u, 0u, 0u) }
    }

    @Test
    fun offsetInvariantTest() {
        assertFailsWith<InvariantFailure> { mk_Offset(OneDayDuration, PlusOrMinus.Plus) }
        assertFailsWith<InvariantFailure> { mk_Offset(OneSecondDuration, PlusOrMinus.Minus) }
    }

    @Test
    fun dateInvariantTest() {
//        assertFailsWith<InvariantFailure> { mk_Date(1,13,1) } // See TestPrimitiveInvariant.kt for more
// As Date's invariant is tested before Month's invariant, it throws an exception, because 13 is not in the domain of the mapping used in Date's invariant
        assertFailsWith<InvariantFailure> { mk_Date(1u, 2u, 29u) }
        assertFailsWith<InvariantFailure> { mk_Date(1u, 2u, 40u) }
    }

    @Test
    fun dtgInZoneInvariantTest() {
        assertFailsWith<InvariantFailure> {
            mk_DtgInZone(
                FirstDate,
                mk_TimeInZone(
                    FirstTime,
                    mk_Offset(Duration.fromHours(1u), PlusOrMinus.Plus)
                )
            )
        }
        assertFailsWith<InvariantFailure> {
            mk_DtgInZone(
                LastDate,
                mk_TimeInZone(
                    LastTime,
                    mk_Offset(Duration.fromHours(1u), PlusOrMinus.Minus)
                )
            )
        }
    }

    @Test
    fun intervalInvariantTest() {
        assertFailsWith<InvariantFailure> { mk_Interval(FirstDtg, FirstDtg) }
        assertFailsWith<InvariantFailure> { mk_Interval(LastDtg, FirstDtg) }
    }

    @Test
    fun isLeapTest() {
        assertEquals(true, DateUtilities.isLeap(1992u))
        assertEquals(false, DateUtilities.isLeap(1993u))
    }

    @Test
    fun daysInMonthTest() {
        assertEquals(29uL, DateUtilities.daysInMonth(1992u, 2u))
        assertEquals(28uL, DateUtilities.daysInMonth(1991u, 2u))
        assertEquals(30uL, DateUtilities.daysInMonth(1997u, 9u))
    }

    @Test
    fun daysInYearTest() {
        assertEquals(366uL, DateUtilities.daysInYear(1992u))
        assertEquals(365uL, DateUtilities.daysInYear(1u))
        assertEquals(365uL, DateUtilities.daysInYear(1991u))
    }

    @Test
    fun dtgInRangeDayTest() {
        assertEquals(
            true,
            mk_Date(1990u, 1u, 3u).properties.dtgAtStartOfDay.functions.inRange(
                mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay
            )
        )
        assertEquals(
            false,
            mk_Date(1990u, 1u, 7u).properties.dtgAtStartOfDay.functions.inRange(
                mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay
            )
        )
        assertEquals(
            true,
            mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay.functions.inRange(
                mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay
            )
        )
        assertEquals(
            false,
            mk_Date(1990u, 1u, 3u).properties.dtgAtStartOfDay.functions.inRange(
                mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 3u).properties.dtgAtStartOfDay
            )
        )
    }

    @Test
    fun dtgInRangeTimeTest() {
        assertEquals(
            true,
            mk_Dtg(FirstDate, mk_Time(2u, 30u, 0u, 0u)).functions.inRange(
                mk_Dtg(FirstDate, mk_Time(2u, 0u, 0u, 0u)),
                mk_Dtg(FirstDate, mk_Time(3u, 0u, 0u, 0u))
            )
        )
        assertEquals(
            false,
            mk_Dtg(FirstDate, mk_Time(3u, 30u, 0u, 0u)).functions.inRange(
                mk_Dtg(FirstDate, mk_Time(2u, 0u, 0u, 0u)),
                mk_Dtg(FirstDate, mk_Time(3u, 0u, 0u, 0u))
            )
        )
        assertEquals(
            true,
            mk_Dtg(FirstDate, mk_Time(2u, 0u, 0u, 0u)).functions.inRange(
                mk_Dtg(FirstDate, mk_Time(2u, 0u, 0u, 0u)),
                mk_Dtg(FirstDate, mk_Time(3u, 0u, 0u, 0u))
            )
        )
        assertEquals(
            false,
            mk_Dtg(FirstDate, mk_Time(3u, 0u, 0u, 0u)).functions.inRange(
                mk_Dtg(FirstDate, mk_Time(2u, 0u, 0u, 0u)),
                mk_Dtg(FirstDate, mk_Time(3u, 0u, 0u, 0u))
            )
        )
    }

    @Test
    fun intervalContainsDtgTest() {
        assertEquals(
            true,
            mk_Interval(
                mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay
            ).functions.contains(mk_Date(1990u, 1u, 3u).properties.dtgAtStartOfDay)
        )
        assertEquals(
            false,
            mk_Interval(
                mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay
            ).functions.contains(mk_Date(1990u, 1u, 7u).properties.dtgAtStartOfDay)
        )
        assertEquals(
            true,
            mk_Interval(
                mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay
            ).functions.contains(mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay)

        )
        assertEquals(
            false,
            mk_Interval(
                mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 3u).properties.dtgAtStartOfDay
            ).functions.contains(mk_Date(1990u, 1u, 3u).properties.dtgAtStartOfDay)
        )
        assertEquals(
            true,
            mk_Interval(
                mk_Dtg(FirstDate, mk_Time(2u, 0u, 0u, 0u)),
                mk_Dtg(FirstDate, mk_Time(3u, 0u, 0u, 0u))
            ).functions.contains(mk_Dtg(FirstDate, mk_Time(2u, 30u, 0u, 0u)))
        )
        assertEquals(
            false,
            mk_Interval(
                mk_Dtg(FirstDate, mk_Time(2u, 0u, 0u, 0u)),
                mk_Dtg(FirstDate, mk_Time(3u, 0u, 0u, 0u))
            ).functions.contains(mk_Dtg(FirstDate, mk_Time(3u, 30u, 0u, 0u)))
        )
        assertEquals(
            true,
            mk_Interval(
                mk_Dtg(FirstDate, mk_Time(2u, 0u, 0u, 0u)),
                mk_Dtg(FirstDate, mk_Time(3u, 0u, 0u, 0u))
            ).functions.contains(mk_Dtg(FirstDate, mk_Time(2u, 0u, 0u, 0u)))
        )
        assertEquals(
            false,
            mk_Interval(
                mk_Dtg(FirstDate, mk_Time(2u, 0u, 0u, 0u)),
                mk_Dtg(FirstDate, mk_Time(3u, 0u, 0u, 0u))
            ).functions.contains(mk_Dtg(FirstDate, mk_Time(3u, 0u, 0u, 0u)))
        )
    }

    @Test
    fun dtgWithinDurationOfDtgTest() {
        assertEquals(
            true,
            mk_Date(1989u, 1u, 3u).properties.dtgAtStartOfDay.functions.withinDurationOfDtg(
                Duration.fromDays(3u), mk_Date(1989u, 1u, 1u).properties.dtgAtStartOfDay
            )
        )
        assertEquals(
            true,
            mk_Date(1989u, 12u, 30u).properties.dtgAtStartOfDay.functions.withinDurationOfDtg(
                Duration.fromDays(3u), mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay
            )
        )
        assertEquals(
            true,
            mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay.functions.withinDurationOfDtg(
                Duration.fromDays(0u), mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay
            )
        )
        assertEquals(
            false,
            mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay.functions.withinDurationOfDtg(
                Duration.fromDays(3u), mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay
            )
        )
        assertEquals(
            false,
            mk_Date(1989u, 12u, 27u).properties.dtgAtStartOfDay.functions.withinDurationOfDtg(
                Duration.fromDays(3u), mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay
            )
        )
    }

    @Test
    fun dtgInIntervalTest() {
        assertEquals(
            true,
            mk_Date(1990u, 1u, 3u).properties.dtgAtStartOfDay.functions.inInterval(
                mk_Interval(
                    mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                    mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay
                )
            )
        )
        assertEquals(
            false,
            mk_Date(1990u, 1u, 7u).properties.dtgAtStartOfDay.functions.inInterval(
                mk_Interval(
                    mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                    mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay
                )
            )
        )
        assertEquals(
            true,
            mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay.functions.inInterval(
                mk_Interval(
                    mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                    mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay
                )
            )

        )
        assertEquals(
            false,
            mk_Date(1990u, 1u, 3u).properties.dtgAtStartOfDay.functions.inInterval(
                mk_Interval(
                    mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                    mk_Date(1990u, 1u, 3u).properties.dtgAtStartOfDay
                )
            )
        )
        assertEquals(
            true,
            mk_Dtg(FirstDate, mk_Time(2u, 30u, 0u, 0u)).functions.inInterval(
                mk_Interval(
                    mk_Dtg(FirstDate, mk_Time(2u, 0u, 0u, 0u)),
                    mk_Dtg(FirstDate, mk_Time(3u, 0u, 0u, 0u))
                )
            )
        )
        assertEquals(
            false,
            mk_Dtg(FirstDate, mk_Time(3u, 30u, 0u, 0u)).functions.inInterval(
                mk_Interval(
                    mk_Dtg(FirstDate, mk_Time(2u, 0u, 0u, 0u)),
                    mk_Dtg(FirstDate, mk_Time(3u, 0u, 0u, 0u))
                )
            )

        )
        assertEquals(
            true,
            mk_Dtg(FirstDate, mk_Time(2u, 0u, 0u, 0u)).functions.inInterval(
                mk_Interval(
                    mk_Dtg(FirstDate, mk_Time(2u, 0u, 0u, 0u)),
                    mk_Dtg(FirstDate, mk_Time(3u, 0u, 0u, 0u))
                )
            )

        )
        assertEquals(
            false,
            mk_Dtg(FirstDate, mk_Time(3u, 0u, 0u, 0u)).functions.inInterval(
                mk_Interval(
                    mk_Dtg(FirstDate, mk_Time(2u, 0u, 0u, 0u)),
                    mk_Dtg(FirstDate, mk_Time(3u, 0u, 0u, 0u))
                )
            )
        )
    }

    @Test
    fun intervalOverlapTest() {
        assertEquals(
            true,
            mk_Interval(
                mk_Date(1990u, 1u, 2u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 4u).properties.dtgAtStartOfDay
            ).functions.overlap(
                mk_Interval(
                    mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                    mk_Date(1990u, 1u, 3u).properties.dtgAtStartOfDay
                )
            )
        )
        assertEquals(
            true,
            mk_Interval(
                mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 3u).properties.dtgAtStartOfDay
            ).functions.overlap(
                mk_Interval(
                    mk_Date(1990u, 1u, 2u).properties.dtgAtStartOfDay,
                    mk_Date(1990u, 1u, 4u).properties.dtgAtStartOfDay
                )
            )
        )
        assertEquals(
            true,
            mk_Interval(
                mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay
            ).functions.overlap(
                mk_Interval(
                    mk_Date(1990u, 1u, 2u).properties.dtgAtStartOfDay,
                    mk_Date(1990u, 1u, 4u).properties.dtgAtStartOfDay
                )
            )
        )
        assertEquals(
            true,
            mk_Interval(
                mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay
            ).functions.overlap(
                mk_Interval(
                    mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                    mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay
                )
            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay
            ).functions.overlap(
                mk_Interval(
                    mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay,
                    mk_Date(1990u, 1u, 8u).properties.dtgAtStartOfDay
                )
            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay
            ).functions.overlap(
                mk_Interval(
                    mk_Date(1990u, 1u, 8u).properties.dtgAtStartOfDay,
                    mk_Date(1990u, 1u, 10u).properties.dtgAtStartOfDay
                )
            )
        )
    }

    @Test
    fun intervalWithinTest() {
        assertEquals(
            true,
            mk_Interval(
                mk_Date(1990u, 1u, 3u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay
            ).functions.within(
                mk_Interval(
                    mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                    mk_Date(1990u, 1u, 10u).properties.dtgAtStartOfDay
                )
            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_Date(1990u, 1u, 12u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 14u).properties.dtgAtStartOfDay
            ).functions.within(
                mk_Interval(
                    mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                    mk_Date(1990u, 1u, 10u).properties.dtgAtStartOfDay
                )
            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_Date(1990u, 1u, 3u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay
            ).functions.within(
                mk_Interval(
                    mk_Date(1990u, 1u, 5u).properties.dtgAtStartOfDay,
                    mk_Date(1990u, 1u, 10u).properties.dtgAtStartOfDay
                )
            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_Date(1990u, 1u, 8u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 12u).properties.dtgAtStartOfDay
            ).functions.within(
                mk_Interval(
                    mk_Date(1990u, 1u, 5u).properties.dtgAtStartOfDay,
                    mk_Date(1990u, 1u, 10u).properties.dtgAtStartOfDay
                )
            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_Date(1990u, 1u, 3u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay
            ).functions.within(
                mk_Interval(
                    mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                    mk_Date(1990u, 1u, 3u).properties.dtgAtStartOfDay
                )
            )
        )
        assertEquals(
            true,
            mk_Interval(
                mk_Date(1990u, 1u, 3u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay
            ).functions.within(
                mk_Interval(
                    mk_Date(1990u, 1u, 3u).properties.dtgAtStartOfDay,
                    mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay
                )
            )
        )
        assertEquals(
            true,
            mk_Interval(
                mk_Date(1990u, 1u, 3u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay
            ).functions.within(
                mk_Interval(
                    mk_Date(1990u, 1u, 2u).properties.dtgAtStartOfDay,
                    mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay
                )
            )
        )
        assertEquals(
            true,
            mk_Interval(
                mk_Date(1990u, 1u, 2u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 5u).properties.dtgAtStartOfDay
            ).functions.within(
                mk_Interval(
                    mk_Date(1990u, 1u, 2u).properties.dtgAtStartOfDay,
                    mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay
                )
            )
        )
    }

    @Test
    fun dtgAddDurationTest() {
        assertEquals(
            mk_Date(1990u, 1u, 5u).properties.dtgAtStartOfDay,
            mk_Date(1990u, 1u, 2u).properties.dtgAtStartOfDay.functions.addDuration(Duration.fromDays(3u))
        )
        assertEquals(
            mk_Date(1990u, 1u, 2u).properties.dtgAtStartOfDay,
            mk_Date(1990u, 1u, 2u).properties.dtgAtStartOfDay.functions.addDuration(Duration.fromDays(0u))
        )
        assertEquals(
            mk_Dtg(mk_Date(1990u, 1u, 2u), mk_Time(5u, 20u, 0u, 0u)),
            mk_Dtg(mk_Date(1990u, 1u, 2u), mk_Time(2u, 0u, 0u, 0u)).functions.addDuration(
                Duration.fromHours(3u).functions.addDuration(Duration.fromMinutes(20u))
            )
        )
        assertEquals(
            mk_Dtg(mk_Date(1990u, 1u, 5u), mk_Time(5u, 20u, 10u, 5u)),
            mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(2u, 0u, 0u, 0u)).functions.addDuration(
                Duration.fromDays(4u).functions.addDuration(
                    Duration.fromHours(3u).functions.addDuration(
                        Duration.fromMinutes(20u).functions.addDuration(
                            Duration.fromSeconds(10u).functions.addDuration(
                                Duration.fromMillis(5u)
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    fun dtgSubtractDurationTest() {
        assertFailsWith<PreconditionFailure> { Duration.fromSeconds(1u).functions.subtractDuration(Duration.fromSeconds(5u)) }
        assertEquals(
            mk_Date(1990u, 1u, 2u).properties.dtgAtStartOfDay,
            mk_Date(1990u, 1u, 5u).properties.dtgAtStartOfDay.functions.subtractDuration(Duration.fromDays(3u))
        )
        assertEquals(
            mk_Date(1990u, 1u, 5u).properties.dtgAtStartOfDay,
            mk_Date(1990u, 1u, 5u).properties.dtgAtStartOfDay.functions.subtractDuration(Duration.fromDays(0u))
        )
        assertEquals(
            mk_Dtg(mk_Date(1990u, 1u, 5u), mk_Time(3u, 20u, 0u, 0u)),
            mk_Dtg(
                mk_Date(1990u, 1u, 5u),
                mk_Time(6u, 40u, 0u, 0u)
            ).functions.subtractDuration(
                Duration.fromHours(3u).functions.addDuration(Duration.fromMinutes(20u))
            )
        )
        assertEquals(
            mk_Dtg(mk_Date(1990u, 1u, 5u), mk_Time(2u, 40u, 0u, 0u)),
            mk_Dtg(
                mk_Date(1990u, 1u, 5u),
                mk_Time(6u, 20u, 0u, 0u)
            ).functions.subtractDuration(
                Duration.fromHours(3u).functions.addDuration(Duration.fromMinutes(40u))
            )
        )
        assertEquals(
            mk_Dtg(mk_Date(1990u, 1u, 2u), mk_Time(2u, 20u, 20u, 5u)),
            mk_Dtg(
                mk_Date(1990u, 1u, 6u),
                mk_Time(5u, 40u, 30u, 10u)
            ).functions.subtractDuration(
                Duration.fromDays(4u).functions.addDuration(
                    Duration.fromHours(3u).functions.addDuration(
                        Duration.fromMinutes(20u).functions.addDuration(
                            Duration.fromSeconds(10u).functions.addDuration(
                                Duration.fromMillis(5u)
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    fun diffDtgTest() {
        assertEquals(
            Duration.fromDays(5u).functions.addDuration(
                Duration.fromHours(5u).functions.addDuration(
                    Duration.fromMinutes(5u).functions.addDuration(
                        Duration.fromSeconds(5u).functions.addDuration(
                            Duration.fromMillis(5u)
                        )
                    )
                )
            ),
            DtgUtilities.dtgDiff(
                mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 1u, 1u, 1u)),
                mk_Dtg(mk_Date(1990u, 1u, 6u), mk_Time(6u, 6u, 6u, 6u))
            )
        )
        assertEquals(
            Duration.fromDays(5u).functions.addDuration(
                Duration.fromHours(5u).functions.addDuration(
                    Duration.fromMinutes(5u).functions.addDuration(
                        Duration.fromSeconds(5u).functions.addDuration(
                            Duration.fromMillis(5u)
                        )
                    )
                )
            ),
            DtgUtilities.dtgDiff(
                mk_Dtg(mk_Date(1990u, 1u, 6u), mk_Time(6u, 6u, 6u, 6u)),
                mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 1u, 1u, 1u))
            )
        )
        assertEquals(
            Duration.fromDays(0u),
            DtgUtilities.dtgDiff(
                mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 1u, 1u, 1u)),
                mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 1u, 1u, 1u))
            )
        )
        assertEquals(
            Duration.fromDays(5u).functions.addDuration(
                Duration.fromHours(5u).functions.addDuration(
                    Duration.fromMinutes(5u).functions.addDuration(
                        Duration.fromSeconds(5u).functions.addDuration(
                            Duration.fromMillis(5u)
                        )
                    )
                )
            ),
            DtgUtilities.dtgDiff(
                mk_Dtg(mk_Date(1990u, 1u, 31u), mk_Time(1u, 1u, 1u, 1u)),
                mk_Dtg(mk_Date(1990u, 2u, 5u), mk_Time(6u, 6u, 6u, 6u))
            )
        )
        assertEquals(
            Duration.fromDays(5u).functions.addDuration(
                Duration.fromHours(5u).functions.addDuration(
                    Duration.fromMinutes(5u).functions.addDuration(
                        Duration.fromSeconds(5u).functions.addDuration(
                            Duration.fromMillis(5u)
                        )
                    )
                )
            ),
            DtgUtilities.dtgDiff(
                mk_Dtg(mk_Date(1990u, 2u, 5u), mk_Time(6u, 6u, 6u, 6u)),
                mk_Dtg(mk_Date(1990u, 1u, 31u), mk_Time(1u, 1u, 1u, 1u))
            )
        )
    }

    @Test
    fun durationAddTest() {
        assertEquals(Duration.fromDays(5u), Duration.fromDays(2u).functions.addDuration(Duration.fromDays(3u)))
        assertEquals(Duration.fromDays(2u), Duration.fromDays(2u).functions.addDuration(Duration.fromDays(0u)))
        assertEquals(Duration.fromDays(2u), Duration.fromDays(0u).functions.addDuration(Duration.fromDays(2u)))
        assertEquals(Duration.fromDays(3u), Duration.fromDays(2u).functions.addDuration(Duration.fromHours(24u)))
    }

    @Test
    fun durationSubtractTest() {
        assertEquals(
            Duration.fromDays(5u),
            Duration.fromDays(8u).functions.subtractDuration(Duration.fromDays(3u))
        )
        assertFailsWith<PreconditionFailure> { Duration.fromDays(2u).functions.subtractDuration(Duration.fromDays(3u)) }
        assertEquals(
            Duration.fromDays(8u),
            Duration.fromDays(8u).functions.subtractDuration(Duration.fromDays(0u))
        )
        assertEquals(
            Duration.fromDays(0u),
            Duration.fromDays(8u).functions.subtractDuration(Duration.fromDays(8u))
        )
    }

    @Test
    fun durationMultiplyTest() {
        assertEquals(Duration.fromDays(10u), Duration.fromDays(2u).functions.multiply(5u))
        assertEquals(Duration.fromDays(0u), Duration.fromDays(2u).functions.multiply(0u))
        assertEquals(Duration.fromHours(25u), Duration.fromHours(5u).functions.multiply(5u))
        assertEquals(Duration.fromDays(5000000u), Duration.fromDays(1000000u).functions.multiply(5u))
    }

    @Test
    fun durationDivideTest() {
        assertEquals(Duration.fromDays(2u), Duration.fromDays(10u).functions.divide(5u))
        assertEquals(Duration.fromHours(12u), Duration.fromDays(10u).functions.divide(20u))
        assertEquals(Duration.fromDays(1000000u), Duration.fromDays(5000000u).functions.divide(5u))
        assertFailsWith<PreconditionFailure> { Duration.fromDays(2u).functions.divide(0u) }
    }

    @Test
    fun durationDiffTest() {
        assertEquals(
            Duration.fromDays(5u),
            DurationUtiltites.durationDiff(Duration.fromDays(10u), Duration.fromDays(5u))
        )
        assertEquals(
            Duration.fromDays(5u),
            DurationUtiltites.durationDiff(Duration.fromDays(0u), Duration.fromDays(5u))
        )
        assertEquals(
            Duration.fromDays(5u),
            DurationUtiltites.durationDiff(Duration.fromDays(5u), Duration.fromDays(10u))
        )
        assertEquals(
            Duration.fromDays(0u),
            DurationUtiltites.durationDiff(Duration.fromDays(10u), Duration.fromDays(10u))
        )
        assertEquals(
            Duration.fromDays(1999999995u),
            DurationUtiltites.durationDiff(Duration.fromDays(2000000000u), Duration.fromDays(5u))
        )
        assertEquals(
            Duration.fromMillis(15u),
            DurationUtiltites.durationDiff(Duration.fromMillis(20u), Duration.fromMillis(5u))
        )
    }

    @Test
    fun durationToMillisTest() {
        assertEquals(12u, mk_Duration(12u).milliseconds)
        assertEquals(0u, mk_Duration(0u).milliseconds)
        assertEquals(2000000000u, mk_Duration(2000000000u).milliseconds)
    }

    @Test
    fun durationFromMillisTest() {
        assertEquals(mk_Duration(12u), Duration.fromMillis(12u))
        assertEquals(mk_Duration(0u), Duration.fromMillis(0u))
        assertEquals(mk_Duration(2000000000u), Duration.fromMillis(2000000000u))
    }

    @Test
    fun durationToSecondsTest() {
        assertEquals(12u, mk_Duration(12000u).properties.seconds)
        assertEquals(0u, mk_Duration(0u).properties.seconds)
        assertEquals(2000000u, mk_Duration(2000000000u).properties.seconds)
    }

    @Test
    fun durationFromSecondsTest() {
        assertEquals(mk_Duration(60000u), Duration.fromSeconds(60u))
        assertEquals(mk_Duration(0u), Duration.fromSeconds(0u))
        assertEquals(mk_Duration(2000000000u), Duration.fromSeconds(2000000u))
    }

    @Test
    fun durationToMinutesTest() {
        assertEquals(10u, mk_Duration(600000u).properties.minutes)
        assertEquals(0u, mk_Duration(0u).properties.minutes)
        assertEquals(2000u, mk_Duration(120000000u).properties.minutes)
    }

    @Test
    fun durationFromMinutesTest() {
        assertEquals(mk_Duration(3600000u), Duration.fromMinutes(60u))
        assertEquals(mk_Duration(0u), Duration.fromMinutes(0u))
        assertEquals(mk_Duration(120000000000u), Duration.fromMinutes(2000000u))
    }

    @Test
    fun durationModSecondsTest() {
        assertEquals(
            Duration.fromMillis(10u),
            Duration.fromSeconds(100u).functions.addDuration(Duration.fromMillis(10u)).functions.modSeconds()
        )
        assertEquals(
            Duration.fromHours(0u),
            Duration.fromSeconds(100u).functions.modSeconds()
        )
        assertEquals(
            Duration.fromMillis(100u),
            Duration.fromSeconds(0u).functions.addDuration(Duration.fromMillis(100u)).functions.modSeconds()
        )
        assertEquals(
            Duration.fromMillis(10u),
            Duration.fromDays(10000u).functions.addDuration(Duration.fromMillis(10u)).functions.modSeconds()
        )

    }

    @Test
    fun durationModMinutesTest() {
        assertEquals(
            Duration.fromSeconds(10u),
            Duration.fromMinutes(5u).functions.addDuration(Duration.fromSeconds(10u)).functions.modMinutes()
        )
        assertEquals(
            Duration.fromDays(0u),
            Duration.fromMinutes(5u).functions.modMinutes()
        )
        assertEquals(
            Duration.fromSeconds(10u),
            Duration.fromMinutes(0u).functions.addDuration(Duration.fromSeconds(10u)).functions.modMinutes()
        )
        assertEquals(
            Duration.fromSeconds(10u),
            Duration.fromMinutes(2000000u).functions.addDuration(Duration.fromSeconds(10u)).functions.modMinutes()
        )
    }

    @Test
    fun durationToHoursTest() {
        assertEquals(10u, mk_Duration(36000000u).properties.hours)
        assertEquals(0u, mk_Duration(0u).properties.hours)
        assertEquals(200u, mk_Duration(720000000u).properties.hours)
        assertEquals(876600u, Duration.durationFromFirstYearUpToStartOfYear(100u).properties.hours)
    }

    @Test
    fun durationFromHoursTest() {
        assertEquals(mk_Duration(216000000u), Duration.fromHours(60u))
        assertEquals(mk_Duration(0u), Duration.fromHours(0u))
        assertEquals(mk_Duration(7200000000u), Duration.fromHours(2000u))
    }

    @Test
    fun durationModHoursTest() {
        assertEquals(
            Duration.fromSeconds(10u),
            Duration.fromHours(5u).functions.addDuration(Duration.fromSeconds(10u)).functions.modHours()
        )
        assertEquals(
            Duration.fromSeconds(10u),
            Duration.fromHours(0u).functions.addDuration(Duration.fromSeconds(10u)).functions.modHours()
        )
        assertEquals(
            Duration.fromSeconds(0u),
            Duration.fromHours(110u).functions.modHours()
        )
        assertEquals(
            Duration.fromSeconds(70u),
            Duration.fromHours(2000000u).functions.addDuration(Duration.fromSeconds(70u)).functions.modHours()
        )
    }

    @Test
    fun durationToDaysTest() {
        assertEquals(10u, mk_Duration(864000000u).properties.days)
        assertEquals(9u, mk_Duration(863999999u).properties.days)
        assertEquals(0u, mk_Duration(0u).properties.days)
        assertEquals(20u, mk_Duration(1728000000u).properties.days)
    }

    @Test
    fun durationFromDaysTest() {
        assertEquals(mk_Duration(864000000u), Duration.fromDays(10u))
        assertEquals(mk_Duration(0u), Duration.fromDays(0u))
        assertEquals(mk_Duration(172800000000u), Duration.fromDays(2000u))
    }

    @Test
    fun dateToDayOfWeekTest() {
        assertEquals(DayOfWeek.Thursday, mk_Date(2009u, 8u, 13u).properties.dayOfWeek)
        assertEquals(DayOfWeek.Saturday, mk_Date(2000u, 4u, 1u).properties.dayOfWeek)
        assertEquals(DayOfWeek.Monday, mk_Date(1u, 1u, 1u).properties.dayOfWeek)
        assertEquals(DayOfWeek.Wednesday, mk_Date(2019u, 10u, 9u).properties.dayOfWeek)
        assertEquals(DayOfWeek.Saturday, mk_Date(2017u, 10u, 28u).properties.dayOfWeek)
        assertEquals(DayOfWeek.Wednesday, mk_Date(2020u, 1u, 1u).properties.dayOfWeek)
    }

    @Test
    fun dateToDtgTest() {
        assertEquals(
            mk_Dtg(mk_Date(1u, 1u, 1u), FirstTime),
            mk_Date(1u, 1u, 1u).properties.dtgAtStartOfDay
        )
        assertEquals(
            mk_Dtg(mk_Date(1000u, 12u, 11u), FirstTime),
            mk_Date(1000u, 12u, 11u).properties.dtgAtStartOfDay
        )
        assertEquals(
            mk_Dtg(mk_Date(2000u, 2u, 29u), FirstTime),
            mk_Date(2000u, 2u, 29u).properties.dtgAtStartOfDay
        )
        assertEquals(
            mk_Dtg(mk_Date(9999u, 12u, 31u), FirstTime),
            mk_Date(9999u, 12u, 31u).properties.dtgAtStartOfDay
        )
        assertEquals(
            mk_Dtg(mk_Date(2001u, 2u, 28u), FirstTime),
            mk_Date(2001u, 2u, 28u).properties.dtgAtStartOfDay
        )
    }

    @Test
    fun durationModDaysTest() {
        assertEquals(
            Duration.fromSeconds(10u),
            Duration.fromDays(5u).functions.addDuration(Duration.fromSeconds(10u)).functions.modDays()
        )
        assertEquals(
            Duration.fromMinutes(0u),
            Duration.fromDays(5u).functions.modDays()
        )
        assertEquals(
            Duration.fromHours(10u),
            Duration.fromDays(0u).functions.addDuration(Duration.fromHours(10u)).functions.modDays()
        )
        assertEquals(
            Duration.fromSeconds(10u),
            Duration.fromDays(200000u).functions.addDuration(Duration.fromSeconds(10u)).functions.modDays()
        )
    }

    @Test
    fun durationToMonthsInGivenYearTest() {
        assertEquals(0u, Duration.fromDays(30u).functions.toMonthsInGivenYear(1990u))
        assertEquals(1u, Duration.fromDays(31u).functions.toMonthsInGivenYear(1990u))
        assertEquals(0u, Duration.fromDays(0u).functions.toMonthsInGivenYear(1990u))
        assertEquals(11u, Duration.fromDays(364u).functions.toMonthsInGivenYear(1990u))
        assertFailsWith<PreconditionFailure> { Duration.fromDays(365u).functions.toMonthsInGivenYear(1990u) }
    }

    @Test
    fun durationFromMonthTest() {
        assertEquals(Duration.fromDays(31u), Duration.fromMonth(1990u, 1u))
        assertEquals(Duration.fromDays(28u), Duration.fromMonth(1990u, 2u))
        assertEquals(Duration.fromDays(30u), Duration.fromMonth(1990u, 9u))
        assertEquals(Duration.fromDays(29u), Duration.fromMonth(1992u, 2u))
    }

    @Test
    fun durationUpToMonthTest() {
        assertEquals(Duration.fromDays(90u), Duration.durationInYearUpToStartOfMonth(1990u, 4u))
        assertEquals(Duration.fromDays(59u), Duration.durationInYearUpToStartOfMonth(1990u, 3u))
        assertEquals(Duration.fromDays(60u), Duration.durationInYearUpToStartOfMonth(1992u, 3u))
    }

    @Test
    fun durationToYearsAfterGivenYearTest() {
        assertEquals(0u, Duration.fromDays(0u).functions.toYearsAfterGivenYear(1990u))
        assertEquals(2u, Duration.fromDays(800u).functions.toYearsAfterGivenYear(1990u))
        assertEquals(0u, Duration.fromDays(365u).functions.toYearsAfterGivenYear(1992u))
        assertEquals(1u, Duration.fromDays(365u).functions.toYearsAfterGivenYear(1990u))
    }

    @Test
    fun durationToYearsAfterFirstYearTest() {
        assertEquals(0u, Duration.fromDays(0u).functions.toYearsAfterFirstYear())
        assertEquals(2u, Duration.fromDays(800u).functions.toYearsAfterFirstYear())
        assertEquals(0u, Duration.fromDays(365u).functions.toYearsAfterFirstYear())
        assertEquals(1u, Duration.fromDays(366u).functions.toYearsAfterFirstYear())
    }

    @Test
    fun durationFromYearTest() {
        assertEquals(Duration.fromDays(365u), Duration.fromYear(1990u))
        assertEquals(Duration.fromDays(366u), Duration.fromYear(2020u))
    }

    @Test
    fun durationUpToYearTest() {
        assertEquals(Duration.fromDays(1461u), Duration.durationFromFirstYearUpToStartOfYear(4u))
        assertEquals(Duration.fromDays(366u), Duration.durationFromFirstYearUpToStartOfYear(1u))
        assertEquals(NoDuration, Duration.durationFromFirstYearUpToStartOfYear(0u))
    }

    @Test
    fun durationToDtgAfterFirstDtgTest() {
        assertEquals(
            mk_Date(0u, 1u, 6u).properties.dtgAtStartOfDay,
            Duration.fromDays(5u).functions.toDtgAfterFirstDtg()
        )
        assertEquals(
            mk_Date(0u, 1u, 1u).properties.dtgAtStartOfDay,
            Duration.fromDays(0u).functions.toDtgAfterFirstDtg()
        )
        assertEquals(
            mk_Date(0u, 2u, 7u).properties.dtgAtStartOfDay,
            Duration.fromDays(37u).functions.toDtgAfterFirstDtg()
        )
        assertEquals(
            mk_Dtg(FirstDate, mk_Time(0u, 0u, 24u, 0u)),
            Duration.fromSeconds(24u).functions.toDtgAfterFirstDtg()
        )
    }

    @Test
    fun durationToDtgAfterGivenDtgTest() {
        assertEquals(
            mk_Date(1000u, 1u, 6u).properties.dtgAtStartOfDay,
            Duration.fromDays(5u).functions.toDtgAfterGivenDtg(
                mk_Date(
                    1000u,
                    1u,
                    1u
                ).properties.dtgAtStartOfDay
            )
        )
        assertEquals(
            mk_Date(100u, 1u, 1u).properties.dtgAtStartOfDay,
            Duration.fromDays(0u).functions.toDtgAfterGivenDtg(mk_Date(100u, 1u, 1u).properties.dtgAtStartOfDay)
        )
        assertEquals(
            mk_Date(2000u, 3u, 31u).properties.dtgAtStartOfDay,
            Duration.fromDays(90u).functions.toDtgAfterGivenDtg(
                mk_Date(
                    2000u,
                    1u,
                    1u
                ).properties.dtgAtStartOfDay
            )
        )
        assertEquals(
            mk_Dtg(mk_Date(2024u, 12u, 31u), mk_Time(13u, 0u, 0u, 0u)),
            Duration.fromHours(13u).functions.toDtgAfterGivenDtg(
                mk_Date(
                    2024u,
                    12u,
                    31u
                ).properties.dtgAtStartOfDay
            )
        )
    }

    @Test
    fun dtgToDurationSinceFirstDtgTest() {
        assertEquals(
            Duration.fromDays(6u),
            mk_Date(0u, 1u, 7u).properties.dtgAtStartOfDay.properties.durationSinceFirstDtg
        )
        assertEquals(
            Duration.fromDays(0u),
            mk_Date(0u, 1u, 1u).properties.dtgAtStartOfDay.properties.durationSinceFirstDtg
        )

        assertEquals(
            Duration.fromDays(37u),
            mk_Date(0u, 2u, 7u).properties.dtgAtStartOfDay.properties.durationSinceFirstDtg
        )
    }

    @Test
    fun durationToDateAfterFirstDateTest() {
        assertEquals(mk_Date(0u, 1u, 4u), Duration.fromDays(3u).functions.toDateAfterFirstDate())
        assertEquals(mk_Date(0u, 1u, 1u), Duration.fromDays(0u).functions.toDateAfterFirstDate())
    }

    @Test
    fun durationToDateAfterGivenDateTest() {
        assertEquals(
            mk_Date(1110u, 10u, 4u),
            Duration.fromDays(3u).functions.toDateAfterGivenDate(mk_Date(1110u, 10u, 1u))
        )
        assertEquals(
            mk_Date(1997u, 9u, 16u),
            Duration.fromDays(0u).functions.toDateAfterGivenDate(mk_Date(1997u, 9u, 16u))
        )
    }

    @Test
    fun dateToDurationSinceFirstDateTest() {
        assertEquals(Duration.fromDays(3u), mk_Date(0u, 1u, 4u).properties.durationSinceFirstDate)
        assertEquals(Duration.fromDays(0u), mk_Date(0u, 1u, 1u).properties.durationSinceFirstDate)
    }

    @Test
    fun durationToTimeAfterFirstTimeTest() {
        assertEquals(mk_Time(3u, 0u, 0u, 0u), Duration.fromHours(3u).functions.toTimeAfterFirstTime())
        assertEquals(FirstTime, Duration.fromHours(0u).functions.toTimeAfterFirstTime())
        assertFailsWith<PreconditionFailure> { Duration.fromHours(25u).functions.toTimeAfterFirstTime() }
    }

    @Test
    fun durationToTimeAfterGivenTimeTest() {
        assertEquals(
            mk_Time(3u, 0u, 0u, 5u),
            Duration.fromHours(1u).functions.toTimeAfterGivenTime(mk_Time(2u, 0u, 0u, 5u))
        )
        assertEquals(
            mk_Time(10u, 10u, 10u, 10u),
            Duration.fromHours(0u).functions.toTimeAfterGivenTime(mk_Time(10u, 10u, 10u, 10u))
        )
    }

    @Test
    fun timeToDurationSinceFirstTimeTest() {
        assertEquals(Duration.fromHours(4u), mk_Time(4u, 0u, 0u, 0u).properties.durationSinceFirstTime)
        assertEquals(Duration.fromHours(0u), mk_Time(0u, 0u, 0u, 0u).properties.durationSinceFirstTime)
    }

    @Test
    fun timeInZoneToDurationSinceFirstTimeTest() {
        assertEquals(
            Duration.fromHours(4u),
            mk_TimeInZone(
                mk_Time(3u, 0u, 0u, 0u),
                mk_Offset(Duration.fromHours(1u), PlusOrMinus.Minus)
            ).properties.normalisedDurationSinceFirstTime
        )
        assertEquals(
            Duration.fromHours(2u),
            mk_TimeInZone(
                mk_Time(3u, 0u, 0u, 0u),
                mk_Offset(Duration.fromHours(1u), PlusOrMinus.Plus)
            ).properties.normalisedDurationSinceFirstTime
        )
        assertEquals(
            Duration.fromHours(1u),
            mk_TimeInZone(
                mk_Time(23u, 0u, 0u, 0u),
                mk_Offset(Duration.fromHours(2u), PlusOrMinus.Minus)
            ).properties.normalisedDurationSinceFirstTime
        )
        assertEquals(
            Duration.fromHours(23u),
            mk_TimeInZone(
                mk_Time(1u, 0u, 0u, 0u),
                mk_Offset(Duration.fromHours(2u), PlusOrMinus.Plus)
            ).properties.normalisedDurationSinceFirstTime
        )
        assertEquals(
            Duration.fromHours(0u),
            mk_TimeInZone(
                FirstTime,
                mk_Offset(Duration.fromHours(0u), PlusOrMinus.None)
            ).properties.normalisedDurationSinceFirstTime
        )
    }

    @Test
    fun intervalDurationTest() {
        assertEquals(
            Duration.fromDays(5u),
            mk_Interval(
                mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay
            ).properties.duration
        )
        assertEquals(
            Duration.fromMillis(1u),
            FirstDtg.properties.instant.properties.duration
        )
        assertEquals(
            Duration.fromHours(2u),
            mk_Interval(
                mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 0u, 0u, 0u)),
                mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(3u, 0u, 0u, 0u))
            ).properties.duration
        )
    }

    @Test
    fun dtgFinestGranularityTest() {
        assertFailsWith<PreconditionFailure> {
            FirstDtg.functions.finestGranularity(Duration.fromDays(0u))
        }
        assertEquals(
            true,
            mk_Dtg(mk_Date(0u, 1u, 1u), mk_Time(10u, 0u, 0u, 0u)).functions.finestGranularity(Duration.fromHours(1u))
        )
        assertEquals(
            false,
            mk_Dtg(mk_Date(0u, 1u, 1u), mk_Time(10u, 0u, 0u, 0u)).functions.finestGranularity(Duration.fromHours(3u))
        )
    }

    @Test
    fun intervalFinestGranularityTest() {
        assertFailsWith<PreconditionFailure> {
            FirstDtg.properties.instant.functions.finestGranularity(Duration.fromDays(0u))
        }
        assertEquals(
            true,
            mk_Interval(
                mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 0u, 0u, 0u)),
                mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(3u, 0u, 0u, 0u))
            ).functions.finestGranularity(Duration.fromHours(1u))
        )
        assertEquals(
            false,
            mk_Interval(
                mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 0u, 0u, 0u)),
                mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(10u, 0u, 0u, 0u))
            ).functions.finestGranularity(Duration.fromHours(2u))
        )

    }

    @Test
    fun minDtgTest() {
        assertEquals(
            mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 0u, 0u, 0u)),
            DtgUtilities.minDtg(
                mk_Set1(
                    mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 0u, 0u, 0u)),
                    mk_Dtg(mk_Date(1990u, 1u, 6u), mk_Time(1u, 0u, 0u, 0u)),
                    mk_Dtg(mk_Date(1990u, 2u, 1u), mk_Time(1u, 0u, 0u, 0u)),
                    mk_Dtg(mk_Date(1990u, 1u, 14u), mk_Time(1u, 0u, 0u, 0u))
                )
            )
        )
        assertEquals(
            mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(0u, 10u, 0u, 0u)),
            DtgUtilities.minDtg(
                mk_Set1(
                    mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(0u, 10u, 0u, 0u)),
                    mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(14u, 0u, 0u, 0u)),
                    mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 0u, 50u, 0u)),
                    mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 30u, 0u, 0u))
                )
            )
        )
        assertEquals(
            mk_Dtg(mk_Date(0u, 1u, 1u), mk_Time(1u, 0u, 0u, 0u)),
            DtgUtilities.minDtg(
                mk_Set1(
                    mk_Dtg(mk_Date(0u, 1u, 1u), mk_Time(1u, 0u, 0u, 0u)),
                    mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 0u, 0u, 0u)),
                    mk_Dtg(mk_Date(300u, 1u, 1u), mk_Time(1u, 0u, 0u, 0u)),
                    mk_Dtg(mk_Date(3000u, 1u, 1u), mk_Time(1u, 0u, 0u, 0u))
                )
            )
        )
    }

    @Test
    fun maxDtgTest() {
        assertEquals(
            mk_Dtg(mk_Date(1990u, 2u, 1u), mk_Time(1u, 0u, 0u, 0u)),
            DtgUtilities.maxDtg(
                mk_Set1(
                    mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 0u, 0u, 0u)),
                    mk_Dtg(mk_Date(1990u, 1u, 6u), mk_Time(1u, 0u, 0u, 0u)),
                    mk_Dtg(mk_Date(1990u, 2u, 1u), mk_Time(1u, 0u, 0u, 0u)),
                    mk_Dtg(mk_Date(1990u, 1u, 14u), mk_Time(1u, 0u, 0u, 0u))
                )
            )
        )
        assertEquals(
            mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(14u, 0u, 0u, 0u)),
            DtgUtilities.maxDtg(
                mk_Set1(
                    mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(0u, 10u, 0u, 0u)),
                    mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(14u, 0u, 0u, 0u)),
                    mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 0u, 50u, 0u)),
                    mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 30u, 0u, 0u))
                )
            )
        )
        assertEquals(
            mk_Dtg(mk_Date(9999u, 1u, 1u), mk_Time(1u, 0u, 0u, 0u)),
            DtgUtilities.maxDtg(
                mk_Set1(
                    mk_Dtg(mk_Date(0u, 1u, 1u), mk_Time(1u, 0u, 0u, 0u)),
                    mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 0u, 0u, 0u)),
                    mk_Dtg(mk_Date(300u, 1u, 1u), mk_Time(1u, 0u, 0u, 0u)),
                    mk_Dtg(mk_Date(9999u, 1u, 1u), mk_Time(1u, 0u, 0u, 0u))
                )
            )

        )
    }

    @Test
    fun minDateTest() {
        assertEquals(
            mk_Date(1990u, 1u, 4u),
            DateUtilities.minDate(
                mk_Set1(
                    mk_Date(1990u, 4u, 1u),
                    mk_Date(1990u, 1u, 4u),
                    mk_Date(1990u, 1u, 31u),
                    mk_Date(1990u, 12u, 1u)
                )
            )
        )
        assertEquals(
            mk_Date(0u, 1u, 1u),
            DateUtilities.minDate(
                mk_Set1(
                    mk_Date(0u, 1u, 1u),
                    mk_Date(1990u, 1u, 1u),
                    mk_Date(300u, 1u, 1u),
                    mk_Date(9999u, 1u, 1u)
                )
            )
        )
    }

    @Test
    fun maxDateTest() {
        assertEquals(
            mk_Date(1990u, 12u, 1u),
            DateUtilities.maxDate(
                mk_Set1(
                    mk_Date(1990u, 4u, 1u),
                    mk_Date(1990u, 1u, 4u),
                    mk_Date(1990u, 1u, 31u),
                    mk_Date(1990u, 12u, 1u)
                )
            )
        )
        assertEquals(
            mk_Date(3000u, 1u, 1u),
            DateUtilities.maxDate(
                mk_Set1(
                    mk_Date(0u, 1u, 1u),
                    mk_Date(1990u, 1u, 1u),
                    mk_Date(300u, 1u, 1u),
                    mk_Date(3000u, 1u, 1u)
                )
            )
        )
    }

    @Test
    fun minTimeTest() {
        assertEquals(
            mk_Time(0u, 0u, 12u, 1u),
            TimeUtilities.minTime(
                mk_Set1(
                    mk_Time(1u, 0u, 4u, 1u),
                    mk_Time(0u, 20u, 1u, 4u),
                    mk_Time(3u, 0u, 1u, 31u),
                    mk_Time(0u, 0u, 12u, 1u)
                )
            )
        )
        assertEquals(
            mk_Time(0u, 0u, 0u, 0u),
            TimeUtilities.minTime(
                mk_Set1(
                    mk_Time(0u, 0u, 0u, 0u),
                    mk_Time(23u, 0u, 1u, 1u),
                    mk_Time(0u, 59u, 1u, 1u),
                    mk_Time(0u, 0u, 59u, 1u)
                )
            )
        )
    }

    @Test
    fun maxTimeTest() {
        assertEquals(
            mk_Time(3u, 0u, 1u, 31u),
            TimeUtilities.maxTime(
                mk_Set1(
                    mk_Time(1u, 0u, 4u, 1u),
                    mk_Time(0u, 20u, 1u, 4u),
                    mk_Time(3u, 0u, 1u, 31u),
                    mk_Time(0u, 0u, 12u, 1u)
                )
            )
        )
        assertEquals(
            mk_Time(23u, 0u, 1u, 1u),
            TimeUtilities.maxTime(
                mk_Set1(
                    mk_Time(0u, 0u, 1u, 1u),
                    mk_Time(23u, 0u, 1u, 1u),
                    mk_Time(0u, 59u, 1u, 1u),
                    mk_Time(0u, 0u, 59u, 1u)
                )
            )

        )
    }

    @Test
    fun minDurationTest() {
        assertEquals(
            Duration.fromMinutes(40u), DurationUtiltites.minDuration(
                mk_Set1(Duration.fromHours(3u), Duration.fromMinutes(40u), Duration.fromDays(2u), Duration.fromHours(5u))
            )
        )
        assertEquals(
            Duration.fromHours(0u),
            DurationUtiltites.minDuration(
                mk_Set1(Duration.fromHours(0u), Duration.fromMinutes(14u), Duration.fromDays(2u), Duration.fromHours(23u))
            )
        )
    }

    @Test
    fun maxDurationTest() {
        assertEquals(
            Duration.fromDays(2u),
            DurationUtiltites.maxDuration(
                mk_Set1(Duration.fromHours(3u), Duration.fromMinutes(40u), Duration.fromDays(2u), Duration.fromHours(5u))
            )
        )
        assertEquals(
            Duration.fromDays(2000000000u),
            DurationUtiltites.maxDuration(
                mk_Set1(
                    Duration.fromHours(0u),
                    Duration.fromMinutes(14u),
                    Duration.fromDays(2000000000u),
                    Duration.fromHours(23u)
                )
            )
        )
    }

    @Test
    fun sumDurationTest() {
        assertEquals(
            mk_Duration(204000000u),
            DurationUtiltites.sumDuration(
                mk_Seq(Duration.fromHours(3u), Duration.fromMinutes(40u), Duration.fromDays(2u), Duration.fromHours(5u))
            )
        )
        assertEquals(
            mk_Duration(0u),
            DurationUtiltites.sumDuration(mk_Seq(Duration.fromHours(0u), Duration.fromMinutes(0u), Duration.fromDays(0u)))
        )
    }

    @Test
    fun instantTest() {
        assertEquals(
            mk_Interval(
                mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 0u, 0u, 0u)),
                mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 0u, 0u, 1u))
            ),
            mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 0u, 0u, 0u)).properties.instant
        )
        assertEquals(
            mk_Interval(
                mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(23u, 59u, 59u, 999u)),
                mk_Date(1990u, 1u, 2u).properties.dtgAtStartOfDay
            ),
            mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(23u, 59u, 59u, 999u)).properties.instant
        )
    }

    @Test
    fun nextDateWithSameDayAsGivenDateTest() {
        assertEquals(
            mk_Date(1990u, 2u, 1u),
            DateUtilities.nextDateWithSameDayAsGivenDate(mk_Date(1990u, 1u, 1u))
        )
        assertEquals(
            mk_Date(1990u, 3u, 31u),
            DateUtilities.nextDateWithSameDayAsGivenDate(mk_Date(1990u, 1u, 31u))
        )
    }

    @Test
    fun nextDateWithSameDayAsGivenDayTest() {
        assertEquals(
            mk_Date(0u, 1u, 4u),
            DateUtilities.nextDateWithSameDayAsGivenDay(mk_Date(0u, 1u, 1u), 4u)
        )
        assertEquals(
            mk_Date(1990u, 3u, 31u),
            DateUtilities.nextDateWithSameDayAsGivenDay(mk_Date(1990u, 1u, 31u), 31u)
        )
        assertEquals(
            mk_Date(1u, 1u, 14u),
            DateUtilities.nextDateWithSameDayAsGivenDay(mk_Date(0u, 12u, 15u), 14u)
        )
    }

    @Test
    fun previousDateWithSameDayAsGivenDateTest() {
        assertEquals(
            mk_Date(1990u, 1u, 3u),
            DateUtilities.previousDateWithSameDayAsGivenDate(mk_Date(1990u, 2u, 3u))
        )
        assertEquals(
            mk_Date(1989u, 12u, 3u),
            DateUtilities.previousDateWithSameDayAsGivenDate(mk_Date(1990u, 1u, 3u))
        )
    }

    @Test
    fun previousDateWithSameDayAsGivenDayTest() {
        assertEquals(
            mk_Date(1990u, 1u, 12u),
            DateUtilities.previousDateWithSameDayAsGivenDay(mk_Date(1990u, 1u, 31u), 12u)
        )
        assertEquals(
            mk_Date(1989u, 12u, 12u),
            DateUtilities.previousDateWithSameDayAsGivenDay(mk_Date(1990u, 1u, 4u), 12u)
        )
    }

    @Test
    fun normaliseDtgInZoneTest() {
        assertEquals(
            mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(3u, 0u, 0u, 0u)),
            mk_DtgInZone(
                mk_Date(1990u, 1u, 1u),
                mk_TimeInZone(
                    mk_Time(5u, 0u, 0u, 0u), mk_Offset(Duration.fromHours(2u), PlusOrMinus.Plus)
                )
            ).properties.normalised
        )
        assertEquals(
            mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(7u, 0u, 0u, 0u)),
            mk_DtgInZone(
                mk_Date(1990u, 1u, 1u),
                mk_TimeInZone(
                    mk_Time(5u, 0u, 0u, 0u), mk_Offset(Duration.fromHours(2u), PlusOrMinus.Minus)
                )
            ).properties.normalised
        )
        assertEquals(
            mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(5u, 0u, 0u, 0u)),
            mk_DtgInZone(
                mk_Date(1990u, 1u, 1u),
                mk_TimeInZone(
                    mk_Time(5u, 0u, 0u, 0u), mk_Offset(Duration.fromHours(0u), PlusOrMinus.None)
                )
            ).properties.normalised
        )
    }

    @Test
    fun normaliseTimeInZoneTest() {
        assertEquals(
            mk_NormalisedTime(mk_Time(7u, 23u, 12u, 0u), PlusOrMinus.None),
            mk_TimeInZone(
                mk_Time(5u, 23u, 12u, 0u), mk_Offset(Duration.fromHours(2u), PlusOrMinus.Minus)
            ).properties.normalisedTime
        )
        assertEquals(
            mk_NormalisedTime(mk_Time(1u, 23u, 12u, 0u), PlusOrMinus.Minus),
            mk_TimeInZone(
                mk_Time(23u, 23u, 12u, 0u), mk_Offset(Duration.fromHours(2u), PlusOrMinus.Minus)
            ).properties.normalisedTime
        )
        assertEquals(
            mk_NormalisedTime(mk_Time(23u, 23u, 12u, 0u), PlusOrMinus.None),
            mk_TimeInZone(
                mk_Time(23u, 23u, 12u, 0u), mk_Offset(Duration.fromHours(0u), PlusOrMinus.None)
            ).properties.normalisedTime
        )
        assertEquals(
            mk_NormalisedTime(mk_Time(3u, 23u, 12u, 0u), PlusOrMinus.None),
            mk_TimeInZone(
                mk_Time(5u, 23u, 12u, 0u), mk_Offset(Duration.fromHours(2u), PlusOrMinus.Plus)
            ).properties.normalisedTime
        )
        assertEquals(
            mk_NormalisedTime(mk_Time(23u, 23u, 12u, 0u), PlusOrMinus.Plus),
            mk_TimeInZone(
                mk_Time(1u, 23u, 12u, 0u), mk_Offset(Duration.fromHours(2u), PlusOrMinus.Plus)
            ).properties.normalisedTime
        )

    }

    @Test
    fun formatDtgTest() {
        assertEquals(
            "1990-01-01T03:00:00",
            mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(3u, 0u, 0u, 0u)).properties.formatted
        )
        assertEquals(
            "1990-10-11T03:00:01",
            mk_Dtg(mk_Date(1990u, 10u, 11u), mk_Time(3u, 0u, 1u, 0u)).properties.formatted
        )
        assertEquals(
            "0000-01-01T03:00:00",
            mk_Dtg(mk_Date(0u, 1u, 1u), mk_Time(3u, 0u, 0u, 0u)).properties.formatted
        )
        assertEquals(
            "9999-01-01T03:00:00.010",
            mk_Dtg(mk_Date(9999u, 1u, 1u), mk_Time(3u, 0u, 0u, 10u)).properties.formatted
        )
    }

    @Test
    fun formatDtgInZoneTest() {
        assertEquals(
            "1990-01-01T03:00:00+02:00",
            mk_DtgInZone(
                mk_Date(1990u, 1u, 1u),
                mk_TimeInZone(
                    mk_Time(3u, 0u, 0u, 0u), mk_Offset(Duration.fromHours(2u), PlusOrMinus.Plus)
                )
            ).properties.formatted
        )
        assertEquals(
            "1990-01-01T03:00:00-02:00",
            mk_DtgInZone(
                mk_Date(1990u, 1u, 1u),
                mk_TimeInZone(
                    mk_Time(3u, 0u, 0u, 0u), mk_Offset(Duration.fromHours(2u), PlusOrMinus.Minus)
                )
            ).properties.formatted
        )
        assertEquals(
            "1990-01-01T03:00:00Z",
            mk_DtgInZone(
                mk_Date(1990u, 1u, 1u),
                mk_TimeInZone(
                    mk_Time(3u, 0u, 0u, 0u), mk_Offset(Duration.fromHours(0u), PlusOrMinus.None)
                )
            ).properties.formatted
        )
    }

    @Test
    fun formatDateTest() {
        assertEquals("1990-01-01", mk_Date(1990u, 1u, 1u).properties.formatted)
        assertEquals("0000-01-01", mk_Date(0u, 1u, 1u).properties.formatted)
    }

    @Test
    fun formatTimeTest() {
        assertEquals("10:07:14", mk_Time(10u, 7u, 14u, 0u).properties.formatted)
        assertEquals("00:00:00", mk_Time(0u, 0u, 0u, 0u).properties.formatted)
        assertEquals("00:00:00.050", mk_Time(0u, 0u, 0u, 50u).properties.formatted)
    }

    @Test
    fun formatTimeInZoneTest() {
        assertEquals(
            "03:00:00+02:00",
            mk_TimeInZone(
                mk_Time(3u, 0u, 0u, 0u), mk_Offset(Duration.fromHours(2u), PlusOrMinus.Plus)
            ).properties.formatted
        )
        assertEquals(
            "03:00:00-02:00",
            mk_TimeInZone(
                mk_Time(3u, 0u, 0u, 0u), mk_Offset(Duration.fromHours(2u), PlusOrMinus.Minus)
            ).properties.formatted
        )
        assertEquals(
            "03:00:00Z",
            mk_TimeInZone(
                mk_Time(3u, 0u, 0u, 0u), mk_Offset(Duration.fromHours(0u), PlusOrMinus.None)
            ).properties.formatted
        )
    }

    @Test
    fun formatOffsetTest() {
        assertEquals(
            "+02:00",
            mk_Offset(Duration.fromHours(2u), PlusOrMinus.Plus).properties.formatted
        )
        assertEquals(
            "-03:00",
            mk_Offset(Duration.fromHours(3u), PlusOrMinus.Minus).properties.formatted
        )
        assertEquals("00:00", mk_Offset(NoDuration, PlusOrMinus.None).properties.formatted)
    }

    @Test
    fun formatIntervalTest() {
        assertEquals(
            "1990-01-01T00:00:00/1990-01-06T00:00:00",
            mk_Interval(
                mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay
            ).properties.formatted
        )
        assertEquals(
            "1990-10-10T00:00:00/1991-01-06T05:00:00",
            mk_Interval(
                mk_Dtg(mk_Date(1990u, 10u, 10u), mk_Time(0u, 0u, 0u, 0u)),
                mk_Dtg(mk_Date(1991u, 1u, 6u), mk_Time(5u, 0u, 0u, 0u))
            ).properties.formatted
        )
    }

    @Test
    fun formatDurationTest() {
        assertEquals(
            "P2DT6H",
            Duration.fromHours(6u).functions.addDuration(Duration.fromDays(2u)).properties.formatted
        )
        assertEquals("PT0S", Duration.fromHours(0u).properties.formatted)
        assertEquals(
            "PT1.001S",
            Duration.fromSeconds(1u).functions.addDuration(Duration.fromMillis(1u)).properties.formatted
        )
    }

    @Test
    fun dtgAddMonthsTest() {
        assertEquals(
            mk_Date(1990u, 3u, 31u).properties.dtgAtStartOfDay,
            mk_Date(1990u, 1u, 31u).properties.dtgAtStartOfDay.functions.addMonths(2)
        )
        assertEquals(
            mk_Dtg(mk_Date(1990u, 3u, 28u), mk_Time(3u, 0u, 0u, 0u)),
            mk_Dtg(
                mk_Date(1990u, 1u, 31u),
                mk_Time(3u, 0u, 0u, 0u)
            ).functions.addMonths(1).functions.addMonths(1)
        )
        assertEquals(
            mk_Date(1991u, 1u, 30u).properties.dtgAtStartOfDay,
            mk_Date(1990u, 11u, 30u).properties.dtgAtStartOfDay.functions.addMonths(2)
        )
    }

    @Test
    fun dtgSubtractMonthsTest() {
        assertFailsWith<PreconditionFailure> { FirstDtg.functions.subtractMonths(1) }
        assertEquals(
            mk_Date(1990u, 2u, 2u).properties.dtgAtStartOfDay,
            mk_Date(1990u, 4u, 2u).properties.dtgAtStartOfDay.functions.subtractMonths(2)
        )
        assertEquals(
            mk_Dtg(mk_Date(1990u, 1u, 28u), mk_Time(3u, 0u, 0u, 0u)),
            (mk_Dtg(
                mk_Date(1990u, 3u, 31u),
                mk_Time(3u, 0u, 0u, 0u)
            )).functions.subtractMonths(1).functions.subtractMonths(1)
        )
        assertEquals(
            mk_Date(1990u, 11u, 2u).properties.dtgAtStartOfDay,
            mk_Date(1991u, 1u, 2u).properties.dtgAtStartOfDay.functions.subtractMonths(2)
        )
    }

    @Test
    fun dateAddMonthsTest() {
        assertEquals(
            mk_Date(1990u, 1u, 31u), mk_Date(1990u, 1u, 31u).functions.addMonths(0)
        )
        assertEquals(
            mk_Date(1990u, 3u, 31u), mk_Date(1990u, 1u, 31u).functions.addMonths(2)
        )
        assertEquals(
            mk_Date(1990u, 3u, 28u),
            mk_Date(1990u, 1u, 31u).functions.addMonths(1).functions.addMonths(1)
        )
        assertEquals(
            mk_Date(1991u, 1u, 30u), mk_Date(1990u, 11u, 30u).functions.addMonths(2)
        )
    }

    @Test
    fun dateSubtractMonthsTest() {
        assertFailsWith<PreconditionFailure> { FirstDate.functions.subtractMonths(1) }
        assertEquals(
            mk_Date(1990u, 4u, 2u), mk_Date(1990u, 4u, 2u).functions.subtractMonths(0)
        )
        assertEquals(
            mk_Date(1990u, 2u, 2u), mk_Date(1990u, 4u, 2u).functions.subtractMonths(2)
        )
        assertEquals(
            mk_Date(1990u, 1u, 28u),
            mk_Date(1990u, 3u, 31u).functions.subtractMonths(1).functions.subtractMonths(1)
        )
        assertEquals(
            mk_Date(1990u, 11u, 2u), mk_Date(1991u, 1u, 2u).functions.subtractMonths(2)
        )
    }

    @Test
    fun dtgAddDays() {
        assertEquals(
            mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
            mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay.functions.addDays(0u)
        )
        assertEquals(
            mk_Date(1990u, 1u, 10u).properties.dtgAtStartOfDay,
            mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay.functions.addDays(9u)
        )
        assertEquals(
            mk_Dtg(mk_Date(1990u, 2u, 1u), LastTime),
            mk_Dtg(mk_Date(1990u, 1u, 1u), LastTime).functions.addDays(31u)
        )
        assertEquals(
            mk_Date(1990u, 2u, 2u).properties.dtgAtStartOfDay,
            mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay.functions.addDays(32u)
        )
        assertEquals(
            mk_Date(1992u, 3u, 1u).properties.dtgAtStartOfDay,
            mk_Date(1992u, 2u, 1u).properties.dtgAtStartOfDay.functions.addDays(29u)
        )
        assertEquals(
            mk_Date(1991u, 1u, 2u).properties.dtgAtStartOfDay,
            mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay.functions.addDays(366u)
        )
    }

    @Test
    fun dtgSubtractDays() {
        assertFailsWith<PreconditionFailure> { FirstDtg.functions.subtractDays(5u) }
        assertEquals(
            mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
            mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay.functions.subtractDays(0u)
        )
        assertEquals(
            mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
            mk_Date(1990u, 1u, 10u).properties.dtgAtStartOfDay.functions.subtractDays(9u)
        )
        assertEquals(
            mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
            mk_Date(1990u, 2u, 1u).properties.dtgAtStartOfDay.functions.subtractDays(31u)
        )
        assertEquals(
            mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
            mk_Date(1990u, 2u, 2u).properties.dtgAtStartOfDay.functions.subtractDays(32u)
        )
        assertEquals(
            mk_Date(1992u, 2u, 1u).properties.dtgAtStartOfDay,
            mk_Date(1992u, 3u, 1u).properties.dtgAtStartOfDay.functions.subtractDays(29u)
        )
        assertEquals(
            mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
            mk_Date(1991u, 1u, 2u).properties.dtgAtStartOfDay.functions.subtractDays(366u)
        )
    }

    @Test
    fun dateAddDays() {
        assertEquals(mk_Date(1990u, 1u, 1u), mk_Date(1990u, 1u, 1u).functions.addDays(0u))
        assertEquals(mk_Date(1990u, 1u, 10u), mk_Date(1990u, 1u, 1u).functions.addDays(9u))
        assertEquals(mk_Date(1990u, 2u, 1u), mk_Date(1990u, 1u, 1u).functions.addDays(31u))
        assertEquals(mk_Date(1990u, 2u, 2u), mk_Date(1990u, 1u, 1u).functions.addDays(32u))
        assertEquals(mk_Date(1992u, 3u, 1u), mk_Date(1992u, 2u, 1u).functions.addDays(29u))
        assertEquals(mk_Date(1991u, 1u, 2u), mk_Date(1990u, 1u, 1u).functions.addDays(366u))
    }

    @Test
    fun dateSubtractDays() {
        assertFailsWith<PreconditionFailure> { FirstDate.functions.subtractDays(5u) }
        assertEquals(
            mk_Date(1990u, 1u, 1u),
            mk_Date(1990u, 1u, 1u).functions.subtractDays(0u)
        )
        assertEquals(
            mk_Date(1990u, 1u, 1u),
            mk_Date(1990u, 1u, 10u).functions.subtractDays(9u)
        )
        assertEquals(
            mk_Date(1990u, 1u, 1u),
            mk_Date(1990u, 2u, 1u).functions.subtractDays(31u)
        )
        assertEquals(
            mk_Date(1990u, 1u, 1u),
            mk_Date(1990u, 2u, 2u).functions.subtractDays(32u)
        )
        assertEquals(
            mk_Date(1992u, 2u, 1u),
            mk_Date(1992u, 3u, 1u).functions.subtractDays(29u)
        )
        assertEquals(
            mk_Date(1990u, 1u, 1u),
            mk_Date(1991u, 1u, 2u).functions.subtractDays(366u)
        )
    }

    @Test
    fun monthsBetweenDtgsTest() {
        assertFailsWith<PreconditionFailure> { DtgUtilities.monthsBetweenDtgs(LastDtg, FirstDtg) }
        assertEquals(0u,
            DtgUtilities.monthsBetweenDtgs(
                mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay
            )
        )
        assertEquals(0u,
            DtgUtilities.monthsBetweenDtgs(
                mk_Date(1990u, 1u, 12u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 2u, 1u).properties.dtgAtStartOfDay
            )
        )
        assertEquals(6u,
            DtgUtilities.monthsBetweenDtgs(
                mk_Date(1990u, 12u, 12u).properties.dtgAtStartOfDay,
                mk_Date(1991u, 6u, 13u).properties.dtgAtStartOfDay
            )
        )
    }

    @Test
    fun yearsBetweenDtgsTest() {
        assertFailsWith<PreconditionFailure> { DtgUtilities.yearsBetweenDtgs(LastDtg, FirstDtg) }
        assertEquals(0u,
            DtgUtilities.yearsBetweenDtgs(
                mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay
            )
        )
        assertEquals(0u,
            DtgUtilities.yearsBetweenDtgs(
                mk_Date(1990u, 1u, 12u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 12u, 1u).properties.dtgAtStartOfDay
            )
        )
        assertEquals(2u,
            DtgUtilities.yearsBetweenDtgs(
                mk_Date(1990u, 1u, 12u).properties.dtgAtStartOfDay,
                mk_Date(1992u, 3u, 13u).properties.dtgAtStartOfDay
            )
        )
    }

    @Test
    fun monthsBetweenDtgs_OneMilliShort() {
        val time1 = mk_Time(hour=0U, minute=0U, second=0U, millisecond=1U)
        val time2 = mk_Time(hour=0U, minute=0U, second=0U, millisecond=0U)

        val dtg1 = mk_Dtg(mk_Date(year=2008U, month=1U, day=1U), time1)
        val dtg2 = mk_Dtg(mk_Date(year=2008U, month=2U, day=1U), time2)

        assertEquals(0U, DtgUtilities.monthsBetweenDtgs(dtg1, dtg2))
    }

    @Test
    fun monthsBetweenDtgs_OneDayShort() {
        val midnight = mk_Time(hour=0U, minute=0U, second=0U, millisecond=0U)

        val dtg1 = mk_Dtg(mk_Date(year=2008U, month=1U, day=2U), midnight)
        val dtg2 = mk_Dtg(mk_Date(year=2008U, month=2U, day=1U), midnight)

        assertEquals(0U, DtgUtilities.monthsBetweenDtgs(dtg1, dtg2))
    }

    @Test
    fun monthsBetweenDtgs_OneDayOneMilliShort() {
        val time1 = mk_Time(hour=0U, minute=0U, second=0U, millisecond=1U)
        val time2 = mk_Time(hour=0U, minute=0U, second=0U, millisecond=0U)

        val dtg1 = mk_Dtg(mk_Date(year=2008U, month=1U, day=2U), time1)
        val dtg2 = mk_Dtg(mk_Date(year=2008U, month=2U, day=1U), time2)

        assertEquals(0U, DtgUtilities.monthsBetweenDtgs(dtg1, dtg2))
    }

    @Test
    fun monthsBetweenDtgs_OneMilliShortOfTwo() {
        val time1 = mk_Time(hour=0U, minute=0U, second=0U, millisecond=1U)
        val time2 = mk_Time(hour=0U, minute=0U, second=0U, millisecond=0U)

        val dtg1 = mk_Dtg(mk_Date(year=2008U, month=1U, day=1U), time1)
        val dtg2 = mk_Dtg(mk_Date(year=2008U, month=3U, day=1U), time2)

        assertEquals(1U, DtgUtilities.monthsBetweenDtgs(dtg1, dtg2))
    }

    @Test
    fun monthsBetweenDtgs_OneDayOneMilliShortOfTwo() {
        val time1 = mk_Time(hour=0U, minute=0U, second=0U, millisecond=1U)
        val time2 = mk_Time(hour=0U, minute=0U, second=0U, millisecond=0U)

        val dtg1 = mk_Dtg(mk_Date(year=2008U, month=1U, day=2U), time1)
        val dtg2 = mk_Dtg(mk_Date(year=2008U, month=3U, day=1U), time2)

        assertEquals(1U, DtgUtilities.monthsBetweenDtgs(dtg1, dtg2))
    }

    @Test
    fun monthsBetweenDtgs_OneDayLongOneMilliShort() {
        val time1 = mk_Time(hour=0U, minute=0U, second=0U, millisecond=1U)
        val time2 = mk_Time(hour=0U, minute=0U, second=0U, millisecond=0U)

        val dtg1 = mk_Dtg(mk_Date(year=2008U, month=2U, day=1U), time1)
        val dtg2 = mk_Dtg(mk_Date(year=2008U, month=3U, day=2U), time2)

        assertEquals(1U, DtgUtilities.monthsBetweenDtgs(dtg1, dtg2))
    }

    @Test
    fun monthsBetweenDtgs_IncompleteDifferentMonth() {
        val midnight = mk_Time(hour=0U, minute=0U, second=0U, millisecond=0U)

        val dtg1 = mk_Dtg(mk_Date(year=2008U, month=1U, day=1U), midnight)
        val dtg2 = mk_Dtg(mk_Date(year=2009U, month=2U, day=2U), midnight)

        //(earlierDate.month <= laterDate.month) &&
//    (laterDate.day < earlierDate.day || laterDate.day == earlierDate.day && laterDtg.time < earlierDtg.time)
        assertEquals(13U, DtgUtilities.monthsBetweenDtgs(dtg1, dtg2))
    }

    @Test
    fun monthsBetweenDtgs_YearStraddle() {
        val midnight = mk_Time(hour=0U, minute=0U, second=0U, millisecond=0U)

        val dtg1 = mk_Dtg(mk_Date(year=2008U, month=12U, day=1U), midnight)
        val dtg2 = mk_Dtg(mk_Date(year=2009U, month=2U, day=1U), midnight)

        assertEquals(2U, DtgUtilities.monthsBetweenDtgs(dtg1, dtg2))
    }

    @Test
    fun monthsBetweenDtgs_YearPlusMonth() {
        val midnight = mk_Time(hour=0U, minute=0U, second=0U, millisecond=0U)

        val dtg1 = mk_Dtg(mk_Date(year=2008U, month=12U, day=1U), midnight)
        val dtg2 = mk_Dtg(mk_Date(year=2010U, month=1U, day=1U), midnight)

        assertEquals(13U, DtgUtilities.monthsBetweenDtgs(dtg1, dtg2))
    }

    @Test
    fun monthsBetweenDtgs_ExactlyOneMonth_Common() {
        val midnight = mk_Time(hour=0U, minute=0U, second=0U, millisecond=0U)

        val dtg1 = mk_Dtg(mk_Date(year=2006U, month=2U, day=28U), midnight)
        val dtg2 = mk_Dtg(mk_Date(year=2006U, month=3U, day=28U), midnight)

        assertEquals(1U, DtgUtilities.monthsBetweenDtgs(dtg1, dtg2))
    }

    @Test
    fun monthsBetweenDtgs_ExactlyOneMonth_BeforeLeapDay() {
        val midnight = mk_Time(hour=0U, minute=0U, second=0U, millisecond=0U)

        val dtg1 = mk_Dtg(mk_Date(year=2008U, month=2U, day=28U), midnight)
        val dtg2 = mk_Dtg(mk_Date(year=2008U, month=3U, day=28U), midnight)

        assertEquals(1U, DtgUtilities.monthsBetweenDtgs(dtg1, dtg2))
    }

    @Test
    fun monthsBetweenDtgs_ExactlyOneMonth_AfterLeapDay() {
        val midnight = mk_Time(hour=0U, minute=0U, second=0U, millisecond=0U)

        val dtg1 = mk_Dtg(mk_Date(year=2008U, month=3U, day=1U), midnight)
        val dtg2 = mk_Dtg(mk_Date(year=2008U, month=4U, day=1U), midnight)

        assertEquals(1U, DtgUtilities.monthsBetweenDtgs(dtg1, dtg2))
    }

    @Test
    fun monthsBetweenDtgs_ExactlyOneYear_Common() {
        val midnight = mk_Time(hour=0U, minute=0U, second=0U, millisecond=0U)

        val dtg1 = mk_Dtg(mk_Date(year=2006U, month=2U, day=28U), midnight)
        val dtg2 = mk_Dtg(mk_Date(year=2007U, month=2U, day=28U), midnight)

        assertEquals(12U, DtgUtilities.monthsBetweenDtgs(dtg1, dtg2))
    }

    @Test
    fun monthsBetweenDtgs_ExactlyOneYear_BeforeLeapDay() {
        val midnight = mk_Time(hour=0U, minute=0U, second=0U, millisecond=0U)

        val dtg1 = mk_Dtg(mk_Date(year=2008U, month=2U, day=28U), midnight)
        val dtg2 = mk_Dtg(mk_Date(year=2009U, month=2U, day=28U), midnight)

        assertEquals(12U, DtgUtilities.monthsBetweenDtgs(dtg1, dtg2))
    }

    @Test
    fun monthsBetweenDtgs_ExactlyOneYear_AfterLeapDay() {
        val midnight = mk_Time(hour=0U, minute=0U, second=0U, millisecond=0U)

        val dtg1 = mk_Dtg(mk_Date(year=2008U, month=3U, day=1U), midnight)
        val dtg2 = mk_Dtg(mk_Date(year=2009U, month=3U, day=1U), midnight)

        assertEquals(12U, DtgUtilities.monthsBetweenDtgs(dtg1, dtg2))
    }

    @Test
    fun yearsBetweenDtgs_ExactlyOneYear_Common() {
        val midnight = mk_Time(hour=0U, minute=0U, second=0U, millisecond=0U)

        val dtg1 = mk_Dtg(mk_Date(year=2006U, month=2U, day=28U), midnight)
        val dtg2 = mk_Dtg(mk_Date(year=2007U, month=2U, day=28U), midnight)

        assertEquals(1U, DtgUtilities.yearsBetweenDtgs(dtg1, dtg2))
    }

    @Test
    fun yearsBetweenDtgs_ExactlyOneYear_BeforeLeapDay() {
        val midnight = mk_Time(hour=0U, minute=0U, second=0U, millisecond=0U)

        val dtg1 = mk_Dtg(mk_Date(year=2008U, month=2U, day=28U), midnight)
        val dtg2 = mk_Dtg(mk_Date(year=2009U, month=2U, day=28U), midnight)

        assertEquals(1U, DtgUtilities.yearsBetweenDtgs(dtg1, dtg2))
    }

    @Test
    fun yearsBetweenDtgs_ExactlyOneYear_AfterLeapDay() {
        val midnight = mk_Time(hour=0U, minute=0U, second=0U, millisecond=0U)

        val dtg1 = mk_Dtg(mk_Date(year=2008U, month=3U, day=1U), midnight)
        val dtg2 = mk_Dtg(mk_Date(year=2009U, month=3U, day=1U), midnight)

        assertEquals(1U, DtgUtilities.yearsBetweenDtgs(dtg1, dtg2))
    }

    @Test
    fun yearsBetweenDtgs_LeapToLeap() {
        val midnight = mk_Time(hour=0U, minute=0U, second=0U, millisecond=0U)

        val dtg1 = mk_Dtg(mk_Date(year=2008U, month=2U, day=29U), midnight)
        val dtg2 = mk_Dtg(mk_Date(year=2012U, month=2U, day=29U), midnight)

        assertEquals(4U, DtgUtilities.yearsBetweenDtgs(dtg1, dtg2))
    }

    @Test
    fun isStringADateTest() {
        assertEquals(true, DateFormattingUtilities.isStringIsoDate("2018-04-01"))
        assertEquals(false, DateFormattingUtilities.isStringIsoDate("2018/04/01"))
        assertEquals(false, DateFormattingUtilities.isStringIsoDate("2018-04"))
        assertEquals(false, DateFormattingUtilities.isStringIsoDate("2018-02-30"))
        assertEquals(false, DateFormattingUtilities.isStringIsoDate("2018-AA-01"))
        assertEquals(false, DateFormattingUtilities.isStringIsoDate("20.8-04-01"))
        assertEquals(false, DateFormattingUtilities.isStringIsoDate("-128-04-01"))
        assertEquals(false, DateFormattingUtilities.isStringIsoDate("2018-24-01"))
        assertEquals(false, DateFormattingUtilities.isStringIsoDate("2018-04-66"))

    }

    @Test
    fun stringToDateTest() {
        assertEquals(mk_Date(2018u, 4u, 1u), DateFormattingUtilities.stringToDate("2018-04-01"))
        assertFailsWith<PreconditionFailure> { DateFormattingUtilities.stringToDate("2018-04-41") }
    }

    @Test
    fun isStringADtgTest() {
        assertEquals(true, DateFormattingUtilities.isStringIsoDtg("1990-01-01T00:00:00"))
        assertEquals(false, DateFormattingUtilities.isStringIsoDtg("-990-01-01T00:00:00"))
        assertEquals(false, DateFormattingUtilities.isStringIsoDtg("1990-01-01!00:00:00"))
        assertEquals(false, DateFormattingUtilities.isStringIsoDtg("1990-01-01T"))
        assertEquals(false, DateFormattingUtilities.isStringIsoDtg("1990-G1-01T00:00:00"))
        assertEquals(false, DateFormattingUtilities.isStringIsoDtg("1990-13-01T00:00:00"))
        assertEquals(false, DateFormattingUtilities.isStringIsoDtg("1990-01-41T00:00:00"))
        assertEquals(false, DateFormattingUtilities.isStringIsoDtg("1990-01-01T24:00:00"))
        assertEquals(false, DateFormattingUtilities.isStringIsoDtg("1990-01-01T00:60:00"))
        assertEquals(false, DateFormattingUtilities.isStringIsoDtg("1990-01-01T00:00:60"))
        assertEquals(false, DateFormattingUtilities.isStringIsoDtg("19.0-01-01T00:00:00"))

        assertEquals(true, DateFormattingUtilities.isStringIsoDtg("1990-01-01T00:00:00.000"))
        assertEquals(false, DateFormattingUtilities.isStringIsoDtg("1990-01-01T00:00:00.FFF"))
        assertEquals(false, DateFormattingUtilities.isStringIsoDtg("1990-01-01T00:00:00-000"))
        assertEquals(false, DateFormattingUtilities.isStringIsoDtg("1990-01-01T00:00:00.-01"))

    }

    @Test
    fun stringToDtgTest() {
        assertEquals(
            mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(12u, 23u, 0u, 0u)),
            DateFormattingUtilities.stringToDtg("1990-01-01T12:23:00")
        )
        assertEquals(
            mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(12u, 23u, 0u, 1u)),
            DateFormattingUtilities.stringToDtg("1990-01-01T12:23:00.001")
        )
        assertFailsWith<PreconditionFailure> { DateFormattingUtilities.stringToDtg("1990-01-01T25:24:00.001") }
    }
}