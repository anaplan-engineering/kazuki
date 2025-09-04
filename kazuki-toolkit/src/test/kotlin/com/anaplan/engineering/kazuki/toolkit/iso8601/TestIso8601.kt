package com.anaplan.engineering.kazuki.toolkit.iso8601

import com.anaplan.engineering.kazuki.core.*
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
                    mk_Offset(DurationUtilities.fromHours(1u), PlusOrMinus.Plus)
                )
            )
        }
        assertFailsWith<InvariantFailure> {
            mk_DtgInZone(
                LastDate,
                mk_TimeInZone(
                    LastTime,
                    mk_Offset(DurationUtilities.fromHours(1u), PlusOrMinus.Minus)
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
                DurationUtilities.fromDays(3u), mk_Date(1989u, 1u, 1u).properties.dtgAtStartOfDay
            )
        )
        assertEquals(
            true,
            mk_Date(1989u, 12u, 30u).properties.dtgAtStartOfDay.functions.withinDurationOfDtg(
                DurationUtilities.fromDays(3u), mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay
            )
        )
        assertEquals(
            true,
            mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay.functions.withinDurationOfDtg(
                DurationUtilities.fromDays(0u), mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay
            )
        )
        assertEquals(
            false,
            mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay.functions.withinDurationOfDtg(
                DurationUtilities.fromDays(3u), mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay
            )
        )
        assertEquals(
            false,
            mk_Date(1989u, 12u, 27u).properties.dtgAtStartOfDay.functions.withinDurationOfDtg(
                DurationUtilities.fromDays(3u), mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay
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
            mk_Date(1990u, 1u, 2u).properties.dtgAtStartOfDay.functions.addDuration(DurationUtilities.fromDays(3u))
        )
        assertEquals(
            mk_Date(1990u, 1u, 2u).properties.dtgAtStartOfDay,
            mk_Date(1990u, 1u, 2u).properties.dtgAtStartOfDay.functions.addDuration(DurationUtilities.fromDays(0u))
        )
        assertEquals(
            mk_Dtg(mk_Date(1990u, 1u, 2u), mk_Time(5u, 20u, 0u, 0u)),
            mk_Dtg(mk_Date(1990u, 1u, 2u), mk_Time(2u, 0u, 0u, 0u)).functions.addDuration(
                DurationUtilities.fromHours(3u).functions.add(DurationUtilities.fromMinutes(20u))
            )
        )
        assertEquals(
            mk_Dtg(mk_Date(1990u, 1u, 5u), mk_Time(5u, 20u, 10u, 5u)),
            mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(2u, 0u, 0u, 0u)).functions.addDuration(
                DurationUtilities.fromDays(4u).functions.add(
                    DurationUtilities.fromHours(3u).functions.add(
                        DurationUtilities.fromMinutes(20u).functions.add(
                            DurationUtilities.fromSeconds(10u).functions.add(
                                DurationUtilities.fromMillis(5u)
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    fun dtgSubtractDurationTest() {
        assertFailsWith<PreconditionFailure> {
            DurationUtilities.fromSeconds(1u).functions.subtract(
                DurationUtilities.fromSeconds(
                    5u
                )
            )
        }
        assertEquals(
            mk_Date(1990u, 1u, 2u).properties.dtgAtStartOfDay,
            mk_Date(1990u, 1u, 5u).properties.dtgAtStartOfDay.functions.subtractDuration(DurationUtilities.fromDays(3u))
        )
        assertEquals(
            mk_Date(1990u, 1u, 5u).properties.dtgAtStartOfDay,
            mk_Date(1990u, 1u, 5u).properties.dtgAtStartOfDay.functions.subtractDuration(DurationUtilities.fromDays(0u))
        )
        assertEquals(
            mk_Dtg(mk_Date(1990u, 1u, 5u), mk_Time(3u, 20u, 0u, 0u)),
            mk_Dtg(
                mk_Date(1990u, 1u, 5u),
                mk_Time(6u, 40u, 0u, 0u)
            ).functions.subtractDuration(
                DurationUtilities.fromHours(3u).functions.add(DurationUtilities.fromMinutes(20u))
            )
        )
        assertEquals(
            mk_Dtg(mk_Date(1990u, 1u, 5u), mk_Time(2u, 40u, 0u, 0u)),
            mk_Dtg(
                mk_Date(1990u, 1u, 5u),
                mk_Time(6u, 20u, 0u, 0u)
            ).functions.subtractDuration(
                DurationUtilities.fromHours(3u).functions.add(DurationUtilities.fromMinutes(40u))
            )
        )
        assertEquals(
            mk_Dtg(mk_Date(1990u, 1u, 2u), mk_Time(2u, 20u, 20u, 5u)),
            mk_Dtg(
                mk_Date(1990u, 1u, 6u),
                mk_Time(5u, 40u, 30u, 10u)
            ).functions.subtractDuration(
                DurationUtilities.fromDays(4u).functions.add(
                    DurationUtilities.fromHours(3u).functions.add(
                        DurationUtilities.fromMinutes(20u).functions.add(
                            DurationUtilities.fromSeconds(10u).functions.add(
                                DurationUtilities.fromMillis(5u)
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
            DurationUtilities.fromDays(5u).functions.add(
                DurationUtilities.fromHours(5u).functions.add(
                    DurationUtilities.fromMinutes(5u).functions.add(
                        DurationUtilities.fromSeconds(5u).functions.add(
                            DurationUtilities.fromMillis(5u)
                        )
                    )
                )
            ),
            DtgUtilities.durationBetween(
                mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 1u, 1u, 1u)),
                mk_Dtg(mk_Date(1990u, 1u, 6u), mk_Time(6u, 6u, 6u, 6u))
            )
        )
        assertEquals(
            DurationUtilities.fromDays(5u).functions.add(
                DurationUtilities.fromHours(5u).functions.add(
                    DurationUtilities.fromMinutes(5u).functions.add(
                        DurationUtilities.fromSeconds(5u).functions.add(
                            DurationUtilities.fromMillis(5u)
                        )
                    )
                )
            ),
            DtgUtilities.durationBetween(
                mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 1u, 1u, 1u)),
                mk_Dtg(mk_Date(1990u, 1u, 6u), mk_Time(6u, 6u, 6u, 6u))
            )
        )
        assertEquals(
            DurationUtilities.fromDays(0u),
            DtgUtilities.durationBetween(
                mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 1u, 1u, 1u)),
                mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 1u, 1u, 1u))
            )
        )
        assertEquals(
            DurationUtilities.fromDays(5u).functions.add(
                DurationUtilities.fromHours(5u).functions.add(
                    DurationUtilities.fromMinutes(5u).functions.add(
                        DurationUtilities.fromSeconds(5u).functions.add(
                            DurationUtilities.fromMillis(5u)
                        )
                    )
                )
            ),
            DtgUtilities.durationBetween(
                mk_Dtg(mk_Date(1990u, 1u, 31u), mk_Time(1u, 1u, 1u, 1u)),
                mk_Dtg(mk_Date(1990u, 2u, 5u), mk_Time(6u, 6u, 6u, 6u))
            )
        )
        assertEquals(
            DurationUtilities.fromDays(5u).functions.add(
                DurationUtilities.fromHours(5u).functions.add(
                    DurationUtilities.fromMinutes(5u).functions.add(
                        DurationUtilities.fromSeconds(5u).functions.add(
                            DurationUtilities.fromMillis(5u)
                        )
                    )
                )
            ),
            DtgUtilities.durationBetween(
                mk_Dtg(mk_Date(1990u, 1u, 31u), mk_Time(1u, 1u, 1u, 1u)),
                mk_Dtg(mk_Date(1990u, 2u, 5u), mk_Time(6u, 6u, 6u, 6u)),
            )
        )
    }

    @Test
    fun durationAddTest() {
        assertEquals(
            DurationUtilities.fromDays(5u),
            DurationUtilities.fromDays(2u).functions.add(DurationUtilities.fromDays(3u))
        )
        assertEquals(
            DurationUtilities.fromDays(2u),
            DurationUtilities.fromDays(2u).functions.add(DurationUtilities.fromDays(0u))
        )
        assertEquals(
            DurationUtilities.fromDays(2u),
            DurationUtilities.fromDays(0u).functions.add(DurationUtilities.fromDays(2u))
        )
        assertEquals(
            DurationUtilities.fromDays(3u),
            DurationUtilities.fromDays(2u).functions.add(DurationUtilities.fromHours(24u))
        )
    }

    @Test
    fun durationSubtractTest() {
        assertEquals(
            DurationUtilities.fromDays(5u),
            DurationUtilities.fromDays(8u).functions.subtract(DurationUtilities.fromDays(3u))
        )
        assertFailsWith<PreconditionFailure> {
            DurationUtilities.fromDays(2u).functions.subtract(
                DurationUtilities.fromDays(
                    3u
                )
            )
        }
        assertEquals(
            DurationUtilities.fromDays(8u),
            DurationUtilities.fromDays(8u).functions.subtract(DurationUtilities.fromDays(0u))
        )
        assertEquals(
            DurationUtilities.fromDays(0u),
            DurationUtilities.fromDays(8u).functions.subtract(DurationUtilities.fromDays(8u))
        )
    }

    @Test
    fun durationMultiplyTest() {
        assertEquals(DurationUtilities.fromDays(10u), DurationUtilities.fromDays(2u).functions.multiply(5u))
        assertEquals(DurationUtilities.fromDays(0u), DurationUtilities.fromDays(2u).functions.multiply(0u))
        assertEquals(DurationUtilities.fromHours(25u), DurationUtilities.fromHours(5u).functions.multiply(5u))
        assertEquals(DurationUtilities.fromDays(5000000u), DurationUtilities.fromDays(1000000u).functions.multiply(5u))
    }

    @Test
    fun durationDivideTest() {
        assertEquals(DurationUtilities.fromDays(2u), DurationUtilities.fromDays(10u).functions.divide(5u))
        assertEquals(DurationUtilities.fromHours(12u), DurationUtilities.fromDays(10u).functions.divide(20u))
        assertEquals(DurationUtilities.fromDays(1000000u), DurationUtilities.fromDays(5000000u).functions.divide(5u))
        // TODO -- primitive invariant
        // assertFailsWith<PreconditionFailure> { DurationUtilities.fromDays(2u).functions.divide(0u) }
    }

    @Test
    fun durationDiffTest() {
        assertEquals(
            DurationUtilities.fromDays(5u),
            DurationUtilities.durationBetween(DurationUtilities.fromDays(10u), DurationUtilities.fromDays(5u))
        )
        assertEquals(
            DurationUtilities.fromDays(5u),
            DurationUtilities.durationBetween(DurationUtilities.fromDays(0u), DurationUtilities.fromDays(5u))
        )
        assertEquals(
            DurationUtilities.fromDays(5u),
            DurationUtilities.durationBetween(DurationUtilities.fromDays(5u), DurationUtilities.fromDays(10u))
        )
        assertEquals(
            DurationUtilities.fromDays(0u),
            DurationUtilities.durationBetween(DurationUtilities.fromDays(10u), DurationUtilities.fromDays(10u))
        )
        assertEquals(
            DurationUtilities.fromDays(1999999995u),
            DurationUtilities.durationBetween(DurationUtilities.fromDays(2000000000u), DurationUtilities.fromDays(5u))
        )
        assertEquals(
            DurationUtilities.fromMillis(15u),
            DurationUtilities.durationBetween(DurationUtilities.fromMillis(20u), DurationUtilities.fromMillis(5u))
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
        assertEquals(mk_Duration(12u), DurationUtilities.fromMillis(12u))
        assertEquals(mk_Duration(0u), DurationUtilities.fromMillis(0u))
        assertEquals(mk_Duration(2000000000u), DurationUtilities.fromMillis(2000000000u))
    }

    @Test
    fun durationToSecondsTest() {
        assertEquals(12u, mk_Duration(12000u).properties.seconds)
        assertEquals(0u, mk_Duration(0u).properties.seconds)
        assertEquals(2000000u, mk_Duration(2000000000u).properties.seconds)
    }

    @Test
    fun durationFromSecondsTest() {
        assertEquals(mk_Duration(60000u), DurationUtilities.fromSeconds(60u))
        assertEquals(mk_Duration(0u), DurationUtilities.fromSeconds(0u))
        assertEquals(mk_Duration(2000000000u), DurationUtilities.fromSeconds(2000000u))
    }

    @Test
    fun durationToMinutesTest() {
        assertEquals(10u, mk_Duration(600000u).properties.minutes)
        assertEquals(0u, mk_Duration(0u).properties.minutes)
        assertEquals(2000u, mk_Duration(120000000u).properties.minutes)
    }

    @Test
    fun durationFromMinutesTest() {
        assertEquals(mk_Duration(3600000u), DurationUtilities.fromMinutes(60u))
        assertEquals(mk_Duration(0u), DurationUtilities.fromMinutes(0u))
        assertEquals(mk_Duration(120000000000u), DurationUtilities.fromMinutes(2000000u))
    }

    @Test
    fun durationModSecondsTest() {
        assertEquals(
            DurationUtilities.fromMillis(10u),
            DurationUtilities.fromSeconds(100u).functions.add(DurationUtilities.fromMillis(10u)).properties.modSeconds
        )
        assertEquals(
            DurationUtilities.fromHours(0u),
            DurationUtilities.fromSeconds(100u).properties.modSeconds
        )
        assertEquals(
            DurationUtilities.fromMillis(100u),
            DurationUtilities.fromSeconds(0u).functions.add(DurationUtilities.fromMillis(100u)).properties.modSeconds
        )
        assertEquals(
            DurationUtilities.fromMillis(10u),
            DurationUtilities.fromDays(10000u).functions.add(DurationUtilities.fromMillis(10u)).properties.modSeconds
        )

    }

    @Test
    fun durationModMinutesTest() {
        assertEquals(
            DurationUtilities.fromSeconds(10u),
            DurationUtilities.fromMinutes(5u).functions.add(DurationUtilities.fromSeconds(10u)).properties.modMinutes
        )
        assertEquals(
            DurationUtilities.fromDays(0u),
            DurationUtilities.fromMinutes(5u).properties.modMinutes
        )
        assertEquals(
            DurationUtilities.fromSeconds(10u),
            DurationUtilities.fromMinutes(0u).functions.add(DurationUtilities.fromSeconds(10u)).properties.modMinutes
        )
        assertEquals(
            DurationUtilities.fromSeconds(10u),
            DurationUtilities.fromMinutes(2000000u).functions.add(DurationUtilities.fromSeconds(10u)).properties.modMinutes
        )
    }

    @Test
    fun durationToHoursTest() {
        assertEquals(10u, mk_Duration(36000000u).properties.hours)
        assertEquals(0u, mk_Duration(0u).properties.hours)
        assertEquals(200u, mk_Duration(720000000u).properties.hours)
        assertEquals(876600u, DurationUtilities.durationFromFirstYearUpToStartOfYear(100u).properties.hours)
    }

    @Test
    fun durationFromHoursTest() {
        assertEquals(mk_Duration(216000000u), DurationUtilities.fromHours(60u))
        assertEquals(mk_Duration(0u), DurationUtilities.fromHours(0u))
        assertEquals(mk_Duration(7200000000u), DurationUtilities.fromHours(2000u))
    }

    @Test
    fun durationModHoursTest() {
        assertEquals(
            DurationUtilities.fromSeconds(10u),
            DurationUtilities.fromHours(5u).functions.add(DurationUtilities.fromSeconds(10u)).properties.modHours
        )
        assertEquals(
            DurationUtilities.fromSeconds(10u),
            DurationUtilities.fromHours(0u).functions.add(DurationUtilities.fromSeconds(10u)).properties.modHours
        )
        assertEquals(
            DurationUtilities.fromSeconds(0u),
            DurationUtilities.fromHours(110u).properties.modHours
        )
        assertEquals(
            DurationUtilities.fromSeconds(70u),
            DurationUtilities.fromHours(2000000u).functions.add(DurationUtilities.fromSeconds(70u)).properties.modHours
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
        assertEquals(mk_Duration(864000000u), DurationUtilities.fromDays(10u))
        assertEquals(mk_Duration(0u), DurationUtilities.fromDays(0u))
        assertEquals(mk_Duration(172800000000u), DurationUtilities.fromDays(2000u))
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
            DurationUtilities.fromSeconds(10u),
            DurationUtilities.fromDays(5u).functions.add(DurationUtilities.fromSeconds(10u)).properties.modDays
        )
        assertEquals(
            DurationUtilities.fromMinutes(0u),
            DurationUtilities.fromDays(5u).properties.modDays
        )
        assertEquals(
            DurationUtilities.fromHours(10u),
            DurationUtilities.fromDays(0u).functions.add(DurationUtilities.fromHours(10u)).properties.modDays
        )
        assertEquals(
            DurationUtilities.fromSeconds(10u),
            DurationUtilities.fromDays(200000u).functions.add(DurationUtilities.fromSeconds(10u)).properties.modDays
        )
    }

    @Test
    fun durationToMonthsInGivenYearTest() {
        assertEquals(0u, DurationUtilities.fromDays(30u).functions.monthCountInYear(1990u))
        assertEquals(1u, DurationUtilities.fromDays(31u).functions.monthCountInYear(1990u))
        assertEquals(0u, DurationUtilities.fromDays(0u).functions.monthCountInYear(1990u))
        assertEquals(11u, DurationUtilities.fromDays(364u).functions.monthCountInYear(1990u))
        assertFailsWith<PreconditionFailure> { DurationUtilities.fromDays(365u).functions.monthCountInYear(1990u) }
    }

    @Test
    fun durationFromMonthTest() {
        assertEquals(DurationUtilities.fromDays(31u), DurationUtilities.fromMonth(1990u, 1u))
        assertEquals(DurationUtilities.fromDays(28u), DurationUtilities.fromMonth(1990u, 2u))
        assertEquals(DurationUtilities.fromDays(30u), DurationUtilities.fromMonth(1990u, 9u))
        assertEquals(DurationUtilities.fromDays(29u), DurationUtilities.fromMonth(1992u, 2u))
    }

    @Test
    fun durationUpToMonthTest() {
        assertEquals(DurationUtilities.fromDays(90u), DurationUtilities.durationInYearUpToStartOfMonth(1990u, 4u))
        assertEquals(DurationUtilities.fromDays(59u), DurationUtilities.durationInYearUpToStartOfMonth(1990u, 3u))
        assertEquals(DurationUtilities.fromDays(60u), DurationUtilities.durationInYearUpToStartOfMonth(1992u, 3u))
    }

    @Test
    fun durationToYearsAfterGivenYearTest() {
        assertEquals(0u, DurationUtilities.fromDays(0u).functions.yearCountFromYear(1990u))
        assertEquals(2u, DurationUtilities.fromDays(800u).functions.yearCountFromYear(1990u))
        assertEquals(0u, DurationUtilities.fromDays(365u).functions.yearCountFromYear(1992u))
        assertEquals(1u, DurationUtilities.fromDays(365u).functions.yearCountFromYear(1990u))
    }

    @Test
    fun durationToYearsAfterFirstYearTest() {
        assertEquals(0u, DurationUtilities.fromDays(0u).properties.yearsAfterFirstYear)
        assertEquals(2u, DurationUtilities.fromDays(800u).properties.yearsAfterFirstYear)
        assertEquals(0u, DurationUtilities.fromDays(365u).properties.yearsAfterFirstYear)
        assertEquals(1u, DurationUtilities.fromDays(366u).properties.yearsAfterFirstYear)
    }

    @Test
    fun durationFromYearTest() {
        assertEquals(DurationUtilities.fromDays(365u), DurationUtilities.fromYear(1990u))
        assertEquals(DurationUtilities.fromDays(366u), DurationUtilities.fromYear(2020u))
    }

    @Test
    fun durationUpToYearTest() {
        assertEquals(DurationUtilities.fromDays(1461u), DurationUtilities.durationFromFirstYearUpToStartOfYear(4u))
        assertEquals(DurationUtilities.fromDays(366u), DurationUtilities.durationFromFirstYearUpToStartOfYear(1u))
        assertEquals(NoDuration, DurationUtilities.durationFromFirstYearUpToStartOfYear(0u))
    }

    @Test
    fun durationToDtgAfterFirstDtgTest() {
        assertEquals(
            mk_Date(0u, 1u, 6u).properties.dtgAtStartOfDay,
            DurationUtilities.fromDays(5u).properties.dtgAfterFirstDtg
        )
        assertEquals(
            mk_Date(0u, 1u, 1u).properties.dtgAtStartOfDay,
            DurationUtilities.fromDays(0u).properties.dtgAfterFirstDtg
        )
        assertEquals(
            mk_Date(0u, 2u, 7u).properties.dtgAtStartOfDay,
            DurationUtilities.fromDays(37u).properties.dtgAfterFirstDtg
        )
        assertEquals(
            mk_Dtg(FirstDate, mk_Time(0u, 0u, 24u, 0u)),
            DurationUtilities.fromSeconds(24u).properties.dtgAfterFirstDtg
        )
    }

    @Test
    fun durationToDtgAfterGivenDtgTest() {
        assertEquals(
            mk_Date(1000u, 1u, 6u).properties.dtgAtStartOfDay,
            DurationUtilities.fromDays(5u).functions.addToDtg(
                mk_Date(
                    1000u,
                    1u,
                    1u
                ).properties.dtgAtStartOfDay
            )
        )
        assertEquals(
            mk_Date(100u, 1u, 1u).properties.dtgAtStartOfDay,
            DurationUtilities.fromDays(0u).functions.addToDtg(mk_Date(100u, 1u, 1u).properties.dtgAtStartOfDay)
        )
        assertEquals(
            mk_Date(2000u, 3u, 31u).properties.dtgAtStartOfDay,
            DurationUtilities.fromDays(90u).functions.addToDtg(
                mk_Date(
                    2000u,
                    1u,
                    1u
                ).properties.dtgAtStartOfDay
            )
        )
        assertEquals(
            mk_Dtg(mk_Date(2024u, 12u, 31u), mk_Time(13u, 0u, 0u, 0u)),
            DurationUtilities.fromHours(13u).functions.addToDtg(
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
            DurationUtilities.fromDays(6u),
            mk_Date(0u, 1u, 7u).properties.dtgAtStartOfDay.properties.durationSinceFirstDtg
        )
        assertEquals(
            DurationUtilities.fromDays(0u),
            mk_Date(0u, 1u, 1u).properties.dtgAtStartOfDay.properties.durationSinceFirstDtg
        )

        assertEquals(
            DurationUtilities.fromDays(37u),
            mk_Date(0u, 2u, 7u).properties.dtgAtStartOfDay.properties.durationSinceFirstDtg
        )
    }

    @Test
    fun durationToDateAfterFirstDateTest() {
        assertEquals(mk_Date(0u, 1u, 4u), DurationUtilities.fromDays(3u).properties.dateAfterFirstDate)
        assertEquals(mk_Date(0u, 1u, 1u), DurationUtilities.fromDays(0u).properties.dateAfterFirstDate)
    }

    @Test
    fun durationToDateAfterGivenDateTest() {
        assertEquals(
            mk_Date(1110u, 10u, 4u),
            DurationUtilities.fromDays(3u).functions.addToDate(mk_Date(1110u, 10u, 1u))
        )
        assertEquals(
            mk_Date(1997u, 9u, 16u),
            DurationUtilities.fromDays(0u).functions.addToDate(mk_Date(1997u, 9u, 16u))
        )
    }

    @Test
    fun dateToDurationSinceFirstDateTest() {
        assertEquals(DurationUtilities.fromDays(3u), mk_Date(0u, 1u, 4u).properties.durationSinceFirstDate)
        assertEquals(DurationUtilities.fromDays(0u), mk_Date(0u, 1u, 1u).properties.durationSinceFirstDate)
    }

    @Test
    fun durationToTimeAfterFirstTimeTest() {
        assertEquals(mk_Time(3u, 0u, 0u, 0u), DurationUtilities.fromHours(3u).properties.timeAfterFirstTime)
        assertEquals(FirstTime, DurationUtilities.fromHours(0u).properties.timeAfterFirstTime)
        assertFailsWith<PreconditionFailure> { DurationUtilities.fromHours(25u).properties.timeAfterFirstTime }
    }

    @Test
    fun durationToTimeAfterGivenTimeTest() {
        assertEquals(
            mk_Time(3u, 0u, 0u, 5u),
            DurationUtilities.fromHours(1u).functions.addToTime(mk_Time(2u, 0u, 0u, 5u))
        )
        assertEquals(
            mk_Time(10u, 10u, 10u, 10u),
            DurationUtilities.fromHours(0u).functions.addToTime(mk_Time(10u, 10u, 10u, 10u))
        )
    }

    @Test
    fun timeToDurationSinceFirstTimeTest() {
        assertEquals(DurationUtilities.fromHours(4u), mk_Time(4u, 0u, 0u, 0u).properties.durationSinceFirstTime)
        assertEquals(DurationUtilities.fromHours(0u), mk_Time(0u, 0u, 0u, 0u).properties.durationSinceFirstTime)
    }

    @Test
    fun timeInZoneToDurationSinceFirstTimeTest() {
        assertEquals(
            DurationUtilities.fromHours(4u),
            mk_TimeInZone(
                mk_Time(3u, 0u, 0u, 0u),
                mk_Offset(DurationUtilities.fromHours(1u), PlusOrMinus.Minus)
            ).properties.normalisedDurationSinceFirstTime
        )
        assertEquals(
            DurationUtilities.fromHours(2u),
            mk_TimeInZone(
                mk_Time(3u, 0u, 0u, 0u),
                mk_Offset(DurationUtilities.fromHours(1u), PlusOrMinus.Plus)
            ).properties.normalisedDurationSinceFirstTime
        )
        assertEquals(
            DurationUtilities.fromHours(1u),
            mk_TimeInZone(
                mk_Time(23u, 0u, 0u, 0u),
                mk_Offset(DurationUtilities.fromHours(2u), PlusOrMinus.Minus)
            ).properties.normalisedDurationSinceFirstTime
        )
        assertEquals(
            DurationUtilities.fromHours(23u),
            mk_TimeInZone(
                mk_Time(1u, 0u, 0u, 0u),
                mk_Offset(DurationUtilities.fromHours(2u), PlusOrMinus.Plus)
            ).properties.normalisedDurationSinceFirstTime
        )
        assertEquals(
            DurationUtilities.fromHours(0u),
            mk_TimeInZone(
                FirstTime,
                mk_Offset(DurationUtilities.fromHours(0u), PlusOrMinus.None)
            ).properties.normalisedDurationSinceFirstTime
        )
    }

    @Test
    fun intervalDurationTest() {
        assertEquals(
            DurationUtilities.fromDays(5u),
            mk_Interval(
                mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 6u).properties.dtgAtStartOfDay
            ).properties.duration
        )
        assertEquals(
            DurationUtilities.fromMillis(1u),
            FirstDtg.properties.instant.properties.duration
        )
        assertEquals(
            DurationUtilities.fromHours(2u),
            mk_Interval(
                mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 0u, 0u, 0u)),
                mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(3u, 0u, 0u, 0u))
            ).properties.duration
        )
    }

    @Test
    fun dtgFinestGranularityTest() {
        assertFailsWith<PreconditionFailure> {
            FirstDtg.functions.finestGranularity(DurationUtilities.fromDays(0u))
        }
        assertEquals(
            true,
            mk_Dtg(
                mk_Date(0u, 1u, 1u),
                mk_Time(10u, 0u, 0u, 0u)
            ).functions.finestGranularity(DurationUtilities.fromHours(1u))
        )
        assertEquals(
            false,
            mk_Dtg(
                mk_Date(0u, 1u, 1u),
                mk_Time(10u, 0u, 0u, 0u)
            ).functions.finestGranularity(DurationUtilities.fromHours(3u))
        )
    }

    @Test
    fun intervalFinestGranularityTest() {
        assertFailsWith<PreconditionFailure> {
            FirstDtg.properties.instant.functions.finestGranularity(DurationUtilities.fromDays(0u))
        }
        assertEquals(
            true,
            mk_Interval(
                mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 0u, 0u, 0u)),
                mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(3u, 0u, 0u, 0u))
            ).functions.finestGranularity(DurationUtilities.fromHours(1u))
        )
        assertEquals(
            false,
            mk_Interval(
                mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 0u, 0u, 0u)),
                mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(10u, 0u, 0u, 0u))
            ).functions.finestGranularity(DurationUtilities.fromHours(2u))
        )

    }

    @Test
    fun earliestDtgTest() {
        assertEquals(
            mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(1u, 0u, 0u, 0u)),
            DtgUtilities.earliest(
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
            DtgUtilities.earliest(
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
            DtgUtilities.earliest(
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
    fun latestDtgTest() {
        assertEquals(
            mk_Dtg(mk_Date(1990u, 2u, 1u), mk_Time(1u, 0u, 0u, 0u)),
            DtgUtilities.latest(
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
            DtgUtilities.latest(
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
            DtgUtilities.latest(
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
    fun earliestDateTest() {
        assertEquals(
            mk_Date(1990u, 1u, 4u),
            DateUtilities.earliest(
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
            DateUtilities.earliest(
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
    fun latestDateTest() {
        assertEquals(
            mk_Date(1990u, 12u, 1u),
            DateUtilities.latest(
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
            DateUtilities.latest(
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
    fun earliestTest() {
        assertEquals(
            mk_Time(0u, 0u, 12u, 1u),
            TimeUtilities.earliest(
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
            TimeUtilities.earliest(
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
    fun latestTimeTest() {
        assertEquals(
            mk_Time(3u, 0u, 1u, 31u),
            TimeUtilities.latest(
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
            TimeUtilities.latest(
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
    fun minTest() {
        assertEquals(
            DurationUtilities.fromMinutes(40u), DurationUtilities.min(
                mk_Set1(
                    DurationUtilities.fromHours(3u),
                    DurationUtilities.fromMinutes(40u),
                    DurationUtilities.fromDays(2u),
                    DurationUtilities.fromHours(5u)
                )
            )
        )
        assertEquals(
            DurationUtilities.fromHours(0u),
            DurationUtilities.min(
                mk_Set1(
                    DurationUtilities.fromHours(0u),
                    DurationUtilities.fromMinutes(14u),
                    DurationUtilities.fromDays(2u),
                    DurationUtilities.fromHours(23u)
                )
            )
        )
    }

    @Test
    fun maxDurationTest() {
        assertEquals(
            DurationUtilities.fromDays(2u),
            DurationUtilities.max(
                mk_Set1(
                    DurationUtilities.fromHours(3u),
                    DurationUtilities.fromMinutes(40u),
                    DurationUtilities.fromDays(2u),
                    DurationUtilities.fromHours(5u)
                )
            )
        )
        assertEquals(
            DurationUtilities.fromDays(2000000000u),
            DurationUtilities.max(
                mk_Set1(
                    DurationUtilities.fromHours(0u),
                    DurationUtilities.fromMinutes(14u),
                    DurationUtilities.fromDays(2000000000u),
                    DurationUtilities.fromHours(23u)
                )
            )
        )
    }

    @Test
    fun sumTest() {
        assertEquals(
            mk_Duration(204000000u),
            DurationUtilities.sum(
                mk_Seq(
                    DurationUtilities.fromHours(3u),
                    DurationUtilities.fromMinutes(40u),
                    DurationUtilities.fromDays(2u),
                    DurationUtilities.fromHours(5u)
                )
            )
        )
        assertEquals(
            mk_Duration(0u),
            DurationUtilities.sum(
                mk_Seq(
                    DurationUtilities.fromHours(0u),
                    DurationUtilities.fromMinutes(0u),
                    DurationUtilities.fromDays(0u)
                )
            )
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
                    mk_Time(5u, 0u, 0u, 0u), mk_Offset(DurationUtilities.fromHours(2u), PlusOrMinus.Plus)
                )
            ).properties.normalised
        )
        assertEquals(
            mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(7u, 0u, 0u, 0u)),
            mk_DtgInZone(
                mk_Date(1990u, 1u, 1u),
                mk_TimeInZone(
                    mk_Time(5u, 0u, 0u, 0u), mk_Offset(DurationUtilities.fromHours(2u), PlusOrMinus.Minus)
                )
            ).properties.normalised
        )
        assertEquals(
            mk_Dtg(mk_Date(1990u, 1u, 1u), mk_Time(5u, 0u, 0u, 0u)),
            mk_DtgInZone(
                mk_Date(1990u, 1u, 1u),
                mk_TimeInZone(
                    mk_Time(5u, 0u, 0u, 0u), mk_Offset(DurationUtilities.fromHours(0u), PlusOrMinus.None)
                )
            ).properties.normalised
        )
    }

    @Test
    fun normaliseTimeInZoneTest() {
        assertEquals(
            mk_NormalisedTime(mk_Time(7u, 23u, 12u, 0u), PlusOrMinus.None),
            mk_TimeInZone(
                mk_Time(5u, 23u, 12u, 0u), mk_Offset(DurationUtilities.fromHours(2u), PlusOrMinus.Minus)
            ).properties.normalisedTime
        )
        assertEquals(
            mk_NormalisedTime(mk_Time(1u, 23u, 12u, 0u), PlusOrMinus.Minus),
            mk_TimeInZone(
                mk_Time(23u, 23u, 12u, 0u), mk_Offset(DurationUtilities.fromHours(2u), PlusOrMinus.Minus)
            ).properties.normalisedTime
        )
        assertEquals(
            mk_NormalisedTime(mk_Time(23u, 23u, 12u, 0u), PlusOrMinus.None),
            mk_TimeInZone(
                mk_Time(23u, 23u, 12u, 0u), mk_Offset(DurationUtilities.fromHours(0u), PlusOrMinus.None)
            ).properties.normalisedTime
        )
        assertEquals(
            mk_NormalisedTime(mk_Time(3u, 23u, 12u, 0u), PlusOrMinus.None),
            mk_TimeInZone(
                mk_Time(5u, 23u, 12u, 0u), mk_Offset(DurationUtilities.fromHours(2u), PlusOrMinus.Plus)
            ).properties.normalisedTime
        )
        assertEquals(
            mk_NormalisedTime(mk_Time(23u, 23u, 12u, 0u), PlusOrMinus.Plus),
            mk_TimeInZone(
                mk_Time(1u, 23u, 12u, 0u), mk_Offset(DurationUtilities.fromHours(2u), PlusOrMinus.Plus)
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
                    mk_Time(3u, 0u, 0u, 0u), mk_Offset(DurationUtilities.fromHours(2u), PlusOrMinus.Plus)
                )
            ).properties.formatted
        )
        assertEquals(
            "1990-01-01T03:00:00-02:00",
            mk_DtgInZone(
                mk_Date(1990u, 1u, 1u),
                mk_TimeInZone(
                    mk_Time(3u, 0u, 0u, 0u), mk_Offset(DurationUtilities.fromHours(2u), PlusOrMinus.Minus)
                )
            ).properties.formatted
        )
        assertEquals(
            "1990-01-01T03:00:00Z",
            mk_DtgInZone(
                mk_Date(1990u, 1u, 1u),
                mk_TimeInZone(
                    mk_Time(3u, 0u, 0u, 0u), mk_Offset(DurationUtilities.fromHours(0u), PlusOrMinus.None)
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
                mk_Time(3u, 0u, 0u, 0u), mk_Offset(DurationUtilities.fromHours(2u), PlusOrMinus.Plus)
            ).properties.formatted
        )
        assertEquals(
            "03:00:00-02:00",
            mk_TimeInZone(
                mk_Time(3u, 0u, 0u, 0u), mk_Offset(DurationUtilities.fromHours(2u), PlusOrMinus.Minus)
            ).properties.formatted
        )
        assertEquals(
            "03:00:00Z",
            mk_TimeInZone(
                mk_Time(3u, 0u, 0u, 0u), mk_Offset(DurationUtilities.fromHours(0u), PlusOrMinus.None)
            ).properties.formatted
        )
    }

    @Test
    fun formatOffsetTest() {
        assertEquals(
            "+02:00",
            mk_Offset(DurationUtilities.fromHours(2u), PlusOrMinus.Plus).properties.formatted
        )
        assertEquals(
            "-03:00",
            mk_Offset(DurationUtilities.fromHours(3u), PlusOrMinus.Minus).properties.formatted
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
            DurationUtilities.fromHours(6u).functions.add(DurationUtilities.fromDays(2u)).properties.formatted
        )
        assertEquals("PT0S", DurationUtilities.fromHours(0u).properties.formatted)
        assertEquals(
            "PT1.001S",
            DurationUtilities.fromSeconds(1u).functions.add(DurationUtilities.fromMillis(1u)).properties.formatted
        )
    }

    @Test
    fun dtgAddMonthsTest() {
        assertEquals(
            mk_Date(1990u, 3u, 31u).properties.dtgAtStartOfDay,
            mk_Date(1990u, 1u, 31u).properties.dtgAtStartOfDay.functions.addMonths(2u)
        )
        assertEquals(
            mk_Dtg(mk_Date(1990u, 3u, 28u), mk_Time(3u, 0u, 0u, 0u)),
            mk_Dtg(
                mk_Date(1990u, 1u, 31u),
                mk_Time(3u, 0u, 0u, 0u)
            ).functions.addMonths(1u).functions.addMonths(1u)
        )
        assertEquals(
            mk_Date(1991u, 1u, 30u).properties.dtgAtStartOfDay,
            mk_Date(1990u, 11u, 30u).properties.dtgAtStartOfDay.functions.addMonths(2u)
        )
    }

    @Test
    fun dtgSubtractMonthsTest() {
        assertFailsWith<PreconditionFailure> { FirstDtg.functions.subtractMonths(1u) }
        assertEquals(
            mk_Date(1990u, 2u, 2u).properties.dtgAtStartOfDay,
            mk_Date(1990u, 4u, 2u).properties.dtgAtStartOfDay.functions.subtractMonths(2u)
        )
        assertEquals(
            mk_Dtg(mk_Date(1990u, 1u, 28u), mk_Time(3u, 0u, 0u, 0u)),
            (mk_Dtg(
                mk_Date(1990u, 3u, 31u),
                mk_Time(3u, 0u, 0u, 0u)
            )).functions.subtractMonths(1u).functions.subtractMonths(1u)
        )
        assertEquals(
            mk_Date(1990u, 11u, 2u).properties.dtgAtStartOfDay,
            mk_Date(1991u, 1u, 2u).properties.dtgAtStartOfDay.functions.subtractMonths(2u)
        )
    }

    @Test
    fun dateAddMonthsTest() {
        assertFailsWith<PreconditionFailure> { LastDate.functions.addMonths(1u) }
        assertEquals(
            mk_Date(1990u, 1u, 31u), mk_Date(1990u, 1u, 31u).functions.addMonths(0u)
        )
        assertEquals(
            mk_Date(1990u, 3u, 31u), mk_Date(1990u, 1u, 31u).functions.addMonths(2u)
        )
        assertEquals(
            mk_Date(1990u, 3u, 28u),
            mk_Date(1990u, 1u, 31u).functions.addMonths(1u).functions.addMonths(1u)
        )
        assertEquals(
            mk_Date(1991u, 1u, 30u), mk_Date(1990u, 11u, 30u).functions.addMonths(2u)
        )
    }

    @Test
    fun dateSubtractMonthsTest() {
        assertFailsWith<PreconditionFailure> { FirstDate.functions.subtractMonths(1u) }
        assertEquals(
            mk_Date(1990u, 4u, 2u), mk_Date(1990u, 4u, 2u).functions.subtractMonths(0u)
        )
        assertEquals(
            mk_Date(1990u, 2u, 2u), mk_Date(1990u, 4u, 2u).functions.subtractMonths(2u)
        )
        assertEquals(
            mk_Date(1990u, 1u, 28u),
            mk_Date(1990u, 3u, 31u).functions.subtractMonths(1u).functions.subtractMonths(1u)
        )
        assertEquals(
            mk_Date(1990u, 11u, 2u), mk_Date(1991u, 1u, 2u).functions.subtractMonths(2u)
        )
    }

    @Test
    fun dateAddYears() {
        assertFailsWith<PreconditionFailure> { LastDate.functions.addYears(1u) }
        assertEquals(
            mk_Date(1990u, 1u, 31u), mk_Date(1990u, 1u, 31u).functions.addYears(0u)
        )
        assertEquals(
            mk_Date(1995u, 3u, 31u), mk_Date(1990u, 3u, 31u).functions.addYears(5u)
        )
        assertEquals(
            mk_Date(2009u, 2u, 28u),
            mk_Date(2008u, 2u, 28u).functions.addYears(1u)
        )
        assertEquals(
            mk_Date(2012u, 2u, 29u),
            mk_Date(2008u, 2u, 29u).functions.addYears(4u)
        )
    }

    @Test
    fun dateSubtractYears() {
        assertFailsWith<PreconditionFailure> { FirstDate.functions.subtractYears(1u) }
        assertEquals(
            mk_Date(1990u, 4u, 2u), mk_Date(1990u, 4u, 2u).functions.subtractYears(0u)
        )
        assertEquals(
            mk_Date(1990u, 2u, 2u), mk_Date(1993u, 2u, 2u).functions.subtractYears(3u)
        )
        assertEquals(
            mk_Date(2007u, 2u, 28u),
            mk_Date(2008u, 2u, 29u).functions.subtractYears(1u)
        )
        assertEquals(
            mk_Date(2004u, 2u, 29u),
            mk_Date(2008u, 2u, 29u).functions.subtractYears(4u)
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
        assertFailsWith<PreconditionFailure> { DtgUtilities.monthsBetween(LastDtg, FirstDtg) }
        assertEquals(
            0u,
            DtgUtilities.monthsBetween(
                mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay
            )
        )
        assertEquals(
            0u,
            DtgUtilities.monthsBetween(
                mk_Date(1990u, 1u, 12u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 2u, 1u).properties.dtgAtStartOfDay
            )
        )
        assertEquals(
            6u,
            DtgUtilities.monthsBetween(
                mk_Date(1990u, 12u, 12u).properties.dtgAtStartOfDay,
                mk_Date(1991u, 6u, 13u).properties.dtgAtStartOfDay
            )
        )
    }

    @Test
    fun yearsBetweenDtgsTest() {
        assertFailsWith<PreconditionFailure> { DtgUtilities.yearsBetween(LastDtg, FirstDtg) }
        assertEquals(
            0u,
            DtgUtilities.yearsBetween(
                mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 1u, 1u).properties.dtgAtStartOfDay
            )
        )
        assertEquals(
            0u,
            DtgUtilities.yearsBetween(
                mk_Date(1990u, 1u, 12u).properties.dtgAtStartOfDay,
                mk_Date(1990u, 12u, 1u).properties.dtgAtStartOfDay
            )
        )
        assertEquals(
            2u,
            DtgUtilities.yearsBetween(
                mk_Date(1990u, 1u, 12u).properties.dtgAtStartOfDay,
                mk_Date(1992u, 3u, 13u).properties.dtgAtStartOfDay
            )
        )
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

