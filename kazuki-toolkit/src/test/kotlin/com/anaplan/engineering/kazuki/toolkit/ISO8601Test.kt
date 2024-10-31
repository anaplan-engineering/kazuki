package com.anaplan.engineering.kazuki.toolkit

import com.anaplan.engineering.kazuki.core.PreconditionFailure
import com.anaplan.engineering.kazuki.core.mk_Seq
import com.anaplan.engineering.kazuki.core.mk_Set1
import com.anaplan.engineering.kazuki.toolkit.ISO8601.*
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Date_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.ISO8601.DtgInZone_Module.mk_DtgInZone
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Dtg_Module.mk_Dtg
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Duration_Module.mk_Duration
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Interval_Module.mk_Interval
import com.anaplan.engineering.kazuki.toolkit.ISO8601.NormalisedTime_Module.mk_NormalisedTime
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Offset_Module.mk_Offset
import com.anaplan.engineering.kazuki.toolkit.ISO8601.TimeInZone_Module.mk_TimeInZone
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Time_Module.mk_Time
import junit.framework.TestCase.assertEquals
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ISO8601Test {

    @Test
    fun isLeapTest() {
        assertEquals(true, isLeap(1992))
        assertEquals(false, isLeap(1993))
    }

    @Test
    fun daysInMonthTest() {
        assertEquals(29, daysInMonth(1992, 2))
        assertEquals(28, daysInMonth(1991, 2))
    }

    @Test
    fun daysInYearTest() {
        assertEquals(366, daysInYear(1992))
        assertEquals(365, daysInYear(1))
        assertEquals(365, daysInYear(1991))
    }

    @Test
    fun dtgInRangeDayTest() {
        assertEquals(
            true,
            mk_Dtg(mk_Date(1990, 1, 3), FirstTime).functions.inRange(
                mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
                mk_Dtg(mk_Date(1990, 1, 6), FirstTime)
            )
        )
        assertEquals(
            false,
            mk_Dtg(mk_Date(1990, 1, 7), FirstTime).functions.inRange(
                mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
                mk_Dtg(mk_Date(1990, 1, 6), FirstTime)
            )
        )
        assertEquals(
            true,
            mk_Dtg(mk_Date(1990, 1, 1), FirstTime).functions.inRange(
                mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
                mk_Dtg(mk_Date(1990, 1, 6), FirstTime)
            )
        )
        assertEquals(
            false,
            mk_Dtg(mk_Date(1990, 1, 3), FirstTime).functions.inRange(
                mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
                mk_Dtg(mk_Date(1990, 1, 3), FirstTime)
            )
        )
    }

    @Test
    fun dtgInRangeTimeTest() {
        assertEquals(
            true,
            mk_Dtg(FirstDate, mk_Time(2, 30, 0, 0)).functions.inRange(
                mk_Dtg(FirstDate, mk_Time(2, 0, 0, 0)),
                mk_Dtg(FirstDate, mk_Time(3, 0, 0, 0))
            )
        )
        assertEquals(
            false,
            mk_Dtg(FirstDate, mk_Time(3, 30, 0, 0)).functions.inRange(
                mk_Dtg(FirstDate, mk_Time(2, 0, 0, 0)),
                mk_Dtg(FirstDate, mk_Time(3, 0, 0, 0))
            )
        )
        assertEquals(
            true,
            mk_Dtg(FirstDate, mk_Time(2, 0, 0, 0)).functions.inRange(
                mk_Dtg(FirstDate, mk_Time(2, 0, 0, 0)),
                mk_Dtg(FirstDate, mk_Time(3, 0, 0, 0))
            )
        )
        assertEquals(
            false,
            mk_Dtg(FirstDate, mk_Time(3, 0, 0, 0)).functions.inRange(
                mk_Dtg(FirstDate, mk_Time(2, 0, 0, 0)),
                mk_Dtg(FirstDate, mk_Time(3, 0, 0, 0))
            )
        )
    }

    @Test
    fun intervalContainsDtgTest() {
        assertEquals(
            true,
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
                mk_Dtg(mk_Date(1990, 1, 6), FirstTime)
            ).functions.contains(mk_Dtg(mk_Date(1990, 1, 3), FirstTime))
        )
        assertEquals(
            false,
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
                mk_Dtg(mk_Date(1990, 1, 6), FirstTime)
            ).functions.contains(mk_Dtg(mk_Date(1990, 1, 7), FirstTime))
        )
        assertEquals(
            true,
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
                mk_Dtg(mk_Date(1990, 1, 6), FirstTime)
            ).functions.contains(mk_Dtg(mk_Date(1990, 1, 1), FirstTime))

        )
        assertEquals(
            false,
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
                mk_Dtg(mk_Date(1990, 1, 3), FirstTime)
            ).functions.contains(mk_Dtg(mk_Date(1990, 1, 3), FirstTime))
        )
        assertEquals(
            true,
            mk_Interval(
                mk_Dtg(FirstDate, mk_Time(2, 0, 0, 0)),
                mk_Dtg(FirstDate, mk_Time(3, 0, 0, 0))
            ).functions.contains(mk_Dtg(FirstDate, mk_Time(2, 30, 0, 0)))
        )
        assertEquals(
            false,
            mk_Interval(
                mk_Dtg(FirstDate, mk_Time(2, 0, 0, 0)),
                mk_Dtg(FirstDate, mk_Time(3, 0, 0, 0))
            ).functions.contains(mk_Dtg(FirstDate, mk_Time(3, 30, 0, 0)))
        )
        assertEquals(
            true,
            mk_Interval(
                mk_Dtg(FirstDate, mk_Time(2, 0, 0, 0)),
                mk_Dtg(FirstDate, mk_Time(3, 0, 0, 0))
            ).functions.contains(mk_Dtg(FirstDate, mk_Time(2, 0, 0, 0)))
        )
        assertEquals(
            false,
            mk_Interval(
                mk_Dtg(FirstDate, mk_Time(2, 0, 0, 0)),
                mk_Dtg(FirstDate, mk_Time(3, 0, 0, 0))
            ).functions.contains(mk_Dtg(FirstDate, mk_Time(3, 0, 0, 0)))
        )
    }

    @Test
    fun dtgWithinTest() {
        assertEquals(
            true,
            mk_Dtg(mk_Date(1989, 1, 3), FirstTime).functions.within(
                Duration.fromDays(3), mk_Dtg(mk_Date(1989, 1, 1), FirstTime)
            )
        )
        assertEquals(
            true,
            mk_Dtg(mk_Date(1989, 12, 30), FirstTime).functions.within(
                Duration.fromDays(3), mk_Dtg(mk_Date(1990, 1, 1), FirstTime)
            )
        )
        assertEquals(
            true,
            mk_Dtg(mk_Date(1990, 1, 1), FirstTime).functions.within(
                Duration.fromDays(0), mk_Dtg(mk_Date(1990, 1, 1), FirstTime)
            )
        )
        assertEquals(
            false,
            mk_Dtg(mk_Date(1990, 1, 6), FirstTime).functions.within(
                Duration.fromDays(3), mk_Dtg(mk_Date(1990, 1, 1), FirstTime)
            )
        )
        assertEquals(
            false,
            mk_Dtg(mk_Date(1989, 12, 27), FirstTime).functions.within(
                Duration.fromDays(3), mk_Dtg(mk_Date(1990, 1, 1), FirstTime)
            )
        )
    }

    @Test
    fun dtgInIntervalTest() {
        assertEquals(
            true,
            mk_Dtg(mk_Date(1990, 1, 3), FirstTime).functions.inInterval(
                mk_Interval(
                    mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
                    mk_Dtg(mk_Date(1990, 1, 6), FirstTime)
                )
            )
        )
        assertEquals(
            false,
            mk_Dtg(mk_Date(1990, 1, 7), FirstTime).functions.inInterval(
                mk_Interval(
                    mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
                    mk_Dtg(mk_Date(1990, 1, 6), FirstTime)
                )
            )
        )
        assertEquals(
            true,
            mk_Dtg(mk_Date(1990, 1, 1), FirstTime).functions.inInterval(
                mk_Interval(
                    mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
                    mk_Dtg(mk_Date(1990, 1, 6), FirstTime)
                )
            )

        )
        assertEquals(
            false,
            mk_Dtg(mk_Date(1990, 1, 3), FirstTime).functions.inInterval(
                mk_Interval(
                    mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
                    mk_Dtg(mk_Date(1990, 1, 3), FirstTime)
                )
            )
        )
        assertEquals(
            true,
            mk_Dtg(FirstDate, mk_Time(2, 30, 0, 0)).functions.inInterval(
                mk_Interval(
                    mk_Dtg(FirstDate, mk_Time(2, 0, 0, 0)),
                    mk_Dtg(FirstDate, mk_Time(3, 0, 0, 0))
                )
            )
        )
        assertEquals(
            false,
            mk_Dtg(FirstDate, mk_Time(3, 30, 0, 0)).functions.inInterval(
                mk_Interval(
                    mk_Dtg(FirstDate, mk_Time(2, 0, 0, 0)),
                    mk_Dtg(FirstDate, mk_Time(3, 0, 0, 0))
                )
            )

        )
        assertEquals(
            true,
            mk_Dtg(FirstDate, mk_Time(2, 0, 0, 0)).functions.inInterval(
                mk_Interval(
                    mk_Dtg(FirstDate, mk_Time(2, 0, 0, 0)),
                    mk_Dtg(FirstDate, mk_Time(3, 0, 0, 0))
                )
            )

        )
        assertEquals(
            false,
            mk_Dtg(FirstDate, mk_Time(3, 0, 0, 0)).functions.inInterval(
                mk_Interval(
                    mk_Dtg(FirstDate, mk_Time(2, 0, 0, 0)),
                    mk_Dtg(FirstDate, mk_Time(3, 0, 0, 0))
                )
            )
        )
    }

    @Test
    fun intervalOverlapTest() {
        assertEquals(
            true,
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 2), FirstTime),
                mk_Dtg(mk_Date(1990, 1, 4), FirstTime)
            ).functions.overlap(
                mk_Interval(
                    mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
                    mk_Dtg(mk_Date(1990, 1, 3), FirstTime)
                )
            )
        )
        assertEquals(
            true,
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
                mk_Dtg(mk_Date(1990, 1, 3), FirstTime)
            ).functions.overlap(
                mk_Interval(
                    mk_Dtg(mk_Date(1990, 1, 2), FirstTime),
                    mk_Dtg(mk_Date(1990, 1, 4), FirstTime)
                )
            )
        )
        assertEquals(
            true,
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
                mk_Dtg(mk_Date(1990, 1, 6), FirstTime)
            ).functions.overlap(
                mk_Interval(
                    mk_Dtg(mk_Date(1990, 1, 2), FirstTime),
                    mk_Dtg(mk_Date(1990, 1, 4), FirstTime)
                )
            )
        )
        assertEquals(
            true,
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
                mk_Dtg(mk_Date(1990, 1, 6), FirstTime)
            ).functions.overlap(
                mk_Interval(
                    mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
                    mk_Dtg(mk_Date(1990, 1, 6), FirstTime)
                )
            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
                mk_Dtg(mk_Date(1990, 1, 6), FirstTime)
            ).functions.overlap(
                mk_Interval(
                    mk_Dtg(mk_Date(1990, 1, 6), FirstTime),
                    mk_Dtg(mk_Date(1990, 1, 8), FirstTime)
                )
            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
                mk_Dtg(mk_Date(1990, 1, 6), FirstTime)
            ).functions.overlap(
                mk_Interval(
                    mk_Dtg(mk_Date(1990, 1, 8), FirstTime),
                    mk_Dtg(mk_Date(1990, 1, 10), FirstTime)
                )
            )
        )
    }

    @Test
    fun intervalWithinTest() {
        assertEquals(
            true,
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 3), FirstTime),
                mk_Dtg(mk_Date(1990, 1, 6), FirstTime)
            ).functions.within(
                mk_Interval(
                    mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
                    mk_Dtg(mk_Date(1990, 1, 10), FirstTime)
                )
            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 12), FirstTime),
                mk_Dtg(mk_Date(1990, 1, 14), FirstTime)
            ).functions.within(
                mk_Interval(
                    mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
                    mk_Dtg(mk_Date(1990, 1, 10), FirstTime)
                )
            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 3), FirstTime),
                mk_Dtg(mk_Date(1990, 1, 6), FirstTime)
            ).functions.within(
                mk_Interval(
                    mk_Dtg(mk_Date(1990, 1, 5), FirstTime),
                    mk_Dtg(mk_Date(1990, 1, 10), FirstTime)
                )
            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 8), FirstTime),
                mk_Dtg(mk_Date(1990, 1, 12), FirstTime)
            ).functions.within(
                mk_Interval(
                    mk_Dtg(mk_Date(1990, 1, 5), FirstTime),
                    mk_Dtg(mk_Date(1990, 1, 10), FirstTime)
                )
            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 3), FirstTime),
                mk_Dtg(mk_Date(1990, 1, 6), FirstTime)
            ).functions.within(
                mk_Interval(
                    mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
                    mk_Dtg(mk_Date(1990, 1, 3), FirstTime)
                )
            )
        )
        assertEquals(
            true,
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 3), FirstTime),
                mk_Dtg(mk_Date(1990, 1, 6), FirstTime)
            ).functions.within(
                mk_Interval(
                    mk_Dtg(mk_Date(1990, 1, 3), FirstTime),
                    mk_Dtg(mk_Date(1990, 1, 6), FirstTime)
                )
            )
        )
        assertEquals(
            true,
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 3), FirstTime),
                mk_Dtg(mk_Date(1990, 1, 6), FirstTime)
            ).functions.within(
                mk_Interval(
                    mk_Dtg(mk_Date(1990, 1, 2), FirstTime),
                    mk_Dtg(mk_Date(1990, 1, 6), FirstTime)
                )
            )
        )
        assertEquals(
            true,
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 2), FirstTime),
                mk_Dtg(mk_Date(1990, 1, 5), FirstTime)
            ).functions.within(
                mk_Interval(
                    mk_Dtg(mk_Date(1990, 1, 2), FirstTime),
                    mk_Dtg(mk_Date(1990, 1, 6), FirstTime)
                )
            )
        )
    }

    @Test
    fun dtgAddTest() {
        assertEquals(
            mk_Dtg(mk_Date(1990, 1, 5), FirstTime),
            mk_Dtg(mk_Date(1990, 1, 2), FirstTime).functions.addDuration(Duration.fromDays(3))
        )
        assertEquals(
            mk_Dtg(mk_Date(1990, 1, 2), mk_Time(5, 20, 0, 0)),
            mk_Dtg(mk_Date(1990, 1, 2), mk_Time(2, 0, 0, 0)).functions.addDuration(
                Duration.fromHours(3).functions.addDuration(Duration.fromMinutes(20))
            )
        )
        assertEquals(
            mk_Dtg(mk_Date(1990, 1, 5), mk_Time(5, 20, 10, 5)),
            mk_Dtg(mk_Date(1990, 1, 1), mk_Time(2, 0, 0, 0)).functions.addDuration(
                Duration.fromDays(4).functions.addDuration(
                    Duration.fromHours(3).functions.addDuration(
                        Duration.fromMinutes(20).functions.addDuration(
                            Duration.fromSeconds(10).functions.addDuration(
                                Duration.fromMillis(5)
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    fun dtgSubtractTest() {
        assertEquals(
            mk_Dtg(mk_Date(1990, 1, 2), FirstTime),
            mk_Dtg(mk_Date(1990, 1, 5), FirstTime).functions.subtractDuration(Duration.fromDays(3))
        )
        assertEquals(
            mk_Dtg(mk_Date(1990, 1, 5), mk_Time(3, 20, 0, 0)),
            mk_Dtg(mk_Date(1990, 1, 5), mk_Time(6, 40, 0, 0)).functions.subtractDuration(
                Duration.fromHours(3).functions.addDuration(Duration.fromMinutes(20))
            )
        )
        assertEquals(
            mk_Dtg(mk_Date(1990, 1, 2), mk_Time(2, 20, 20, 5)),

            mk_Dtg(mk_Date(1990, 1, 6), mk_Time(5, 40, 30, 10)).functions.subtractDuration(
                Duration.fromDays(4).functions.addDuration(
                    Duration.fromHours(3).functions.addDuration(
                        Duration.fromMinutes(20).functions.addDuration(
                            Duration.fromSeconds(10).functions.addDuration(
                                Duration.fromMillis(5)
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
            Duration.fromDays(5).functions.addDuration(
                Duration.fromHours(5).functions.addDuration(
                    Duration.fromMinutes(5).functions.addDuration(
                        Duration.fromSeconds(5).functions.addDuration(
                            Duration.fromMillis(5)
                        )
                    )
                )
            ),
            dtgDiff(
                mk_Dtg(mk_Date(1990, 1, 1), mk_Time(1, 1, 1, 1)),
                mk_Dtg(mk_Date(1990, 1, 6), mk_Time(6, 6, 6, 6))
            )
        )
        assertEquals(
            Duration.fromDays(5).functions.addDuration(
                Duration.fromHours(5).functions.addDuration(
                    Duration.fromMinutes(5).functions.addDuration(
                        Duration.fromSeconds(5).functions.addDuration(
                            Duration.fromMillis(5)
                        )
                    )
                )
            ),
            dtgDiff(
                mk_Dtg(mk_Date(1990, 1, 6), mk_Time(6, 6, 6, 6)),
                mk_Dtg(mk_Date(1990, 1, 1), mk_Time(1, 1, 1, 1))
            )
        )
        assertEquals(
            Duration.fromDays(5).functions.addDuration(
                Duration.fromHours(5).functions.addDuration(
                    Duration.fromMinutes(5).functions.addDuration(
                        Duration.fromSeconds(5).functions.addDuration(
                            Duration.fromMillis(5)
                        )
                    )
                )
            ),
            dtgDiff(
                mk_Dtg(mk_Date(1990, 1, 31), mk_Time(1, 1, 1, 1)),
                mk_Dtg(mk_Date(1990, 2, 5), mk_Time(6, 6, 6, 6))
            )
        )
        assertEquals(
            Duration.fromDays(5).functions.addDuration(
                Duration.fromHours(5).functions.addDuration(
                    Duration.fromMinutes(5).functions.addDuration(
                        Duration.fromSeconds(5).functions.addDuration(
                            Duration.fromMillis(5)
                        )
                    )
                )
            ),
            dtgDiff(
                mk_Dtg(mk_Date(1990, 2, 5), mk_Time(6, 6, 6, 6)),
                mk_Dtg(mk_Date(1990, 1, 31), mk_Time(1, 1, 1, 1))
            )
        )
    }

    @Test
    fun durationAddTest() {
        assertEquals(Duration.fromDays(5), Duration.fromDays(2).functions.addDuration(Duration.fromDays(3)))
        assertEquals(Duration.fromDays(2), Duration.fromDays(2).functions.addDuration(Duration.fromDays(0)))
    }

    @Test
    fun durationSubtractTest() {
        assertEquals(Duration.fromDays(5), Duration.fromDays(8).functions.subtractDuration(Duration.fromDays(3)))
        assertFailsWith<PreconditionFailure> { Duration.fromDays(2).functions.subtractDuration(Duration.fromDays(3)) }
        assertEquals(Duration.fromDays(8), Duration.fromDays(8).functions.subtractDuration(Duration.fromDays(0)))
        assertEquals(Duration.fromDays(0), Duration.fromDays(8).functions.subtractDuration(Duration.fromDays(8)))
    }

    @Test
    fun durationMultiplyTest() {
        assertEquals(Duration.fromDays(10), Duration.fromDays(2).functions.multiply(5))
        assertEquals(Duration.fromHours(25), Duration.fromHours(5).functions.multiply(5))
        assertEquals(Duration.fromDays(5000000), Duration.fromDays(1000000).functions.multiply(5))
    }

    @Test
    fun durationDivideTest() {
        assertEquals(Duration.fromDays(2), Duration.fromDays(10).functions.divide(5))
        assertEquals(Duration.fromHours(12), Duration.fromDays(10).functions.divide(20))
        assertEquals(Duration.fromDays(1000000), Duration.fromDays(5000000).functions.divide(5))
    }

    @Test
    fun durationDiffTest() {
        assertEquals(Duration.fromDays(5), durationDiff(Duration.fromDays(10), Duration.fromDays(5)))
        assertEquals(Duration.fromDays(5), durationDiff(Duration.fromDays(5), Duration.fromDays(10)))
        assertEquals(Duration.fromDays(0), durationDiff(Duration.fromDays(10), Duration.fromDays(10)))
        assertEquals(Duration.fromDays(1999999995), durationDiff(Duration.fromDays(2000000000), Duration.fromDays(5)))
        assertEquals(Duration.fromMillis(15), durationDiff(Duration.fromMillis(20), Duration.fromMillis(5)))
    }

    @Test
    fun durationToMillisTest() {
        assertEquals(12, mk_Duration(12).functions.toMillis())
        assertEquals(0, mk_Duration(0).functions.toMillis())
        assertEquals(2000000000, mk_Duration(2000000000).functions.toMillis())
    }

    @Test
    fun durationFromMillisTest() {
        assertEquals(12, Duration.fromMillis(12).milliseconds)
        assertEquals(0, Duration.fromMillis(0).milliseconds)
        assertEquals(2000000000, Duration.fromMillis(2000000000).milliseconds)
    }

    @Test
    fun durationToSecondsTest() {
        assertEquals(12, mk_Duration(12000).functions.toSeconds())
        assertEquals(0, mk_Duration(0).functions.toSeconds())
        assertEquals(2000000, mk_Duration(2000000000).functions.toSeconds())
    }

    @Test
    fun durationFromSecondsTest() {
        assertEquals(60000, Duration.fromSeconds(60).milliseconds)
        assertEquals(0, Duration.fromSeconds(0).milliseconds)
        assertEquals(2000000000, Duration.fromSeconds(2000000).milliseconds)
    }

    @Test
    fun durationToMinutesTest() {
        assertEquals(10, mk_Duration(600000).functions.toMinutes())
        assertEquals(0, mk_Duration(0).functions.toMinutes())
        assertEquals(2000, mk_Duration(120000000).functions.toMinutes())
    }

    @Test
    fun durationFromMinutesTest() {
        assertEquals(3600000, Duration.fromMinutes(60).milliseconds)
        assertEquals(0, Duration.fromMinutes(0).milliseconds)
        assertEquals(120000000000, Duration.fromMinutes(2000000).milliseconds)
    }

    @Test
    fun durationModSecondsTest() {
        assertEquals(
            Duration.fromMillis(10),
            Duration.fromSeconds(100).functions.addDuration(Duration.fromMillis(10)).functions.modSeconds()
        )
        assertEquals(
            Duration.fromMillis(100),
            Duration.fromSeconds(0).functions.addDuration(Duration.fromMillis(100)).functions.modSeconds()
        )
        assertEquals(
            Duration.fromMillis(10),
            Duration.fromDays(10000).functions.addDuration(Duration.fromMillis(10)).functions.modSeconds()
        )

    }

    @Test
    fun durationModMinutesTest() {
        assertEquals(
            Duration.fromSeconds(10),
            Duration.fromMinutes(5).functions.addDuration(Duration.fromSeconds(10)).functions.modMinutes()
        )
        assertEquals(
            Duration.fromSeconds(10),
            Duration.fromMinutes(0).functions.addDuration(Duration.fromSeconds(10)).functions.modMinutes()
        )
        assertEquals(
            Duration.fromSeconds(10),
            Duration.fromMinutes(2000000).functions.addDuration(Duration.fromSeconds(10)).functions.modMinutes()
        )
    }

    @Test
    fun durationToHoursTest() {
        assertEquals(10, mk_Duration(36000000).functions.toHours())
        assertEquals(0, mk_Duration(0).functions.toHours())
        assertEquals(200, mk_Duration(720000000).functions.toHours())
        assertEquals(876600, Duration.durationUpToYear(100).functions.toHours())
    }

    @Test
    fun durationFromHoursTest() {
        assertEquals(216000000, Duration.fromHours(60).milliseconds)
        assertEquals(0, Duration.fromHours(0).milliseconds)
        assertEquals(7200000000, Duration.fromHours(2000).milliseconds)
    }

    @Test
    fun durationModHoursTest() {
        assertEquals(
            Duration.fromSeconds(10),
            Duration.fromHours(5).functions.addDuration(Duration.fromSeconds(10)).functions.modHours()
        )
        assertEquals(
            Duration.fromSeconds(10),
            Duration.fromHours(0).functions.addDuration(Duration.fromSeconds(10)).functions.modHours()
        )
        assertEquals(
            Duration.fromSeconds(10),
            Duration.fromHours(2000000).functions.addDuration(Duration.fromSeconds(10)).functions.modHours()
        )
    }

    @Test
    fun durationToDaysTest() {
        assertEquals(10, mk_Duration(864000000).functions.toDays())
        assertEquals(9, mk_Duration(863999999).functions.toDays())
        assertEquals(0, mk_Duration(0).functions.toDays())
        assertEquals(20, mk_Duration(1728000000).functions.toDays())
    }

    @Test
    fun durationFromDaysTest() {
        assertEquals(864000000, Duration.fromDays(10).milliseconds)
        assertEquals(0, Duration.fromDays(0).milliseconds)
        assertEquals(172800000000, Duration.fromDays(2000).milliseconds)
    }

    @Test
    fun dateToDayOfWeekTest() {
        assertEquals(DayOfWeek.Thursday, mk_Date(2009, 8, 13).functions.toDayOfWeek())
        assertEquals(DayOfWeek.Saturday, mk_Date(2000, 4, 1).functions.toDayOfWeek())
        assertEquals(DayOfWeek.Monday, mk_Date(1, 1, 1).functions.toDayOfWeek())
        assertEquals(DayOfWeek.Wednesday, mk_Date(2019, 10, 9).functions.toDayOfWeek())
        assertEquals(DayOfWeek.Saturday, mk_Date(2017, 10, 28).functions.toDayOfWeek())
        assertEquals(DayOfWeek.Wednesday, mk_Date(2020, 1, 1).functions.toDayOfWeek())
    }

    @Test
    fun durationModDaysTest() {
        assertEquals(
            Duration.fromSeconds(10),
            Duration.fromDays(5).functions.addDuration(Duration.fromSeconds(10)).functions.modDays()
        )
        assertEquals(
            Duration.fromSeconds(10),
            Duration.fromDays(0).functions.addDuration(Duration.fromSeconds(10)).functions.modDays()
        )
        assertEquals(
            Duration.fromSeconds(10),
            Duration.fromDays(200000).functions.addDuration(Duration.fromSeconds(10)).functions.modDays()
        )
    }

    @Test
    fun durationToMonthTest() {
        assertEquals(0, Duration.fromDays(30).functions.toMonthInYear(1990))
        assertEquals(1, Duration.fromDays(31).functions.toMonthInYear(1990))
        assertEquals(0, Duration.fromDays(0).functions.toMonthInYear(1990))
        assertEquals(11, Duration.fromDays(364).functions.toMonthInYear(1990))
    }

    @Test
    fun durationFromMonthTest() {
        assertEquals(Duration.fromDays(31), Duration.fromMonth(1990, 1))
        assertEquals(Duration.fromDays(28), Duration.fromMonth(1990, 2))
        assertEquals(Duration.fromDays(30), Duration.fromMonth(1990, 9))
    }

    @Test
    fun durationUpToMonthTest() {
        assertEquals(Duration.fromDays(90), Duration.durationUpToMonth(1990, 4))
        assertEquals(Duration.fromDays(59), Duration.durationUpToMonth(1990, 3))
    }

    @Test
    fun durationToYearTest() {
        assertEquals(0, Duration.fromDays(0).functions.toYearsAfterGivenYear(1990))
        assertEquals(2, Duration.fromDays(800).functions.toYearsAfterGivenYear(1990))
    }

    @Test
    fun durationFromYearTest() {
        assertEquals(Duration.fromDays(365), Duration.fromYear(1990))
        assertEquals(Duration.fromDays(366), Duration.fromYear(2020))
    }

    @Test
    fun durationUpToYearTest() {
        assertEquals(Duration.fromDays(366), Duration.durationUpToYear(1))
    }

    @Test
    fun durationToDtgTest() {
        assertEquals(
            mk_Dtg(mk_Date(0, 1, 6), mk_Time(0, 0, 0, 0)),
            Duration.fromDays(5).functions.toDtg()
        )
        assertEquals(
            mk_Dtg(mk_Date(0, 1, 1), mk_Time(0, 0, 0, 0)),
            Duration.fromDays(0).functions.toDtg()
        )
        assertEquals(
            mk_Dtg(mk_Date(0, 2, 7), mk_Time(0, 0, 0, 0)),
            Duration.fromDays(37).functions.toDtg()
        )
    }

    @Test
    fun dtgToDurationTest() {
        assertEquals(
            Duration.fromDays(6),
            mk_Dtg(mk_Date(0, 1, 7), mk_Time(0, 0, 0, 0)).functions.toDuration()
        )
        assertEquals(
            Duration.fromDays(0),
            mk_Dtg(mk_Date(0, 1, 1), mk_Time(0, 0, 0, 0)).functions.toDuration()
        )

        assertEquals(
            Duration.fromDays(37),
            mk_Dtg(mk_Date(0, 2, 7), mk_Time(0, 0, 0, 0)).functions.toDuration()
        )
    }

    @Test
    fun durationToDateTest() {
        assertEquals(mk_Date(0, 1, 4), Duration.fromDays(3).functions.toDate())
        assertEquals(mk_Date(0, 1, 1), Duration.fromDays(0).functions.toDate())
    }

    @Test
    fun durationFromDateTest() {
        assertEquals(Duration.fromDays(3), mk_Date(0, 1, 4).functions.toDuration())
        assertEquals(Duration.fromDays(0), mk_Date(0, 1, 1).functions.toDuration())
    }

    @Test
    fun durationToTimeTest() {
        assertEquals(mk_Time(3, 0, 0, 0), Duration.fromHours(3).functions.toTime())
        assertEquals(mk_Time(0, 0, 0, 0), Duration.fromHours(0).functions.toTime())
    }

    @Test
    fun durationFromTimeTest() {
        assertEquals(Duration.fromHours(4), mk_Time(4, 0, 0, 0).functions.toDuration())
        assertEquals(Duration.fromHours(0), mk_Time(0, 0, 0, 0).functions.toDuration())
    }

    @Test
    fun durationFromTimeInZoneTest() {
        assertEquals(
            Duration.fromHours(4),
            mk_TimeInZone(
                mk_Time(3, 0, 0, 0),
                mk_Offset(Duration.fromHours(1), PlusOrMinus.Minus)
            ).functions.toDuration()
        )
        assertEquals(
            Duration.fromHours(2),
            mk_TimeInZone(
                mk_Time(3, 0, 0, 0),
                mk_Offset(Duration.fromHours(1), PlusOrMinus.Plus)
            ).functions.toDuration()
        )
        assertEquals(
            Duration.fromHours(0),
            mk_TimeInZone(
                mk_Time(0, 0, 0, 0),
                mk_Offset(Duration.fromHours(0), PlusOrMinus.None)
            ).functions.toDuration()
        )
    }

    @Test
    fun durationFromIntervalTest() {
        assertEquals(
            Duration.fromDays(5),
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
                mk_Dtg(mk_Date(1990, 1, 6), FirstTime)
            ).functions.intervalDuration()
        )
        assertEquals(
            Duration.fromHours(2),
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)),
                mk_Dtg(mk_Date(1990, 1, 1), mk_Time(3, 0, 0, 0))
            ).functions.intervalDuration()
        )
    }

    @Test
    fun dtgFinestGranularityTest() {
        assertEquals(
            true,
            mk_Dtg(mk_Date(0, 1, 1), mk_Time(10, 0, 0, 0)).functions.finestGranularity(
                Duration.fromHours(1)
            )
        )
        assertEquals(
            false,
            mk_Dtg(mk_Date(0, 1, 1), mk_Time(10, 0, 0, 0)).functions.finestGranularity(
                Duration.fromHours(3)
            )
        )
    }

    @Test
    fun intervalFinestGranularityTest() {
        assertEquals(
            true,
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)),
                mk_Dtg(mk_Date(1990, 1, 1), mk_Time(3, 0, 0, 0))
            ).functions.finestGranularity(
                Duration.fromHours(1)
            )
        )
        assertEquals(
            false,
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)),
                mk_Dtg(mk_Date(1990, 1, 1), mk_Time(10, 0, 0, 0))
            ).functions.finestGranularity(
                Duration.fromHours(2)
            )
        )

    }

    @Test
    fun minDtgTest() {
        assertEquals(
            mk_Dtg(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)),
            minDtg(
                mk_Set1(
                    mk_Dtg(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)),
                    mk_Dtg(mk_Date(1990, 1, 6), mk_Time(1, 0, 0, 0)),
                    mk_Dtg(mk_Date(1990, 2, 1), mk_Time(1, 0, 0, 0)),
                    mk_Dtg(mk_Date(1990, 1, 14), mk_Time(1, 0, 0, 0))
                )
            )
        )
        assertEquals(
            mk_Dtg(mk_Date(1990, 1, 1), mk_Time(0, 10, 0, 0)),
            minDtg(
                mk_Set1(
                    mk_Dtg(mk_Date(1990, 1, 1), mk_Time(0, 10, 0, 0)),
                    mk_Dtg(mk_Date(1990, 1, 1), mk_Time(14, 0, 0, 0)),
                    mk_Dtg(mk_Date(1990, 1, 1), mk_Time(1, 0, 50, 0)),
                    mk_Dtg(mk_Date(1990, 1, 1), mk_Time(1, 30, 0, 0))
                )
            )
        )
        assertEquals(
            mk_Dtg(mk_Date(0, 1, 1), mk_Time(1, 0, 0, 0)),
            minDtg(
                mk_Set1(
                    mk_Dtg(mk_Date(0, 1, 1), mk_Time(1, 0, 0, 0)),
                    mk_Dtg(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)),
                    mk_Dtg(mk_Date(300, 1, 1), mk_Time(1, 0, 0, 0)),
                    mk_Dtg(mk_Date(3000, 1, 1), mk_Time(1, 0, 0, 0))
                )
            )
        )
    }

    @Test
    fun maxDtgTest() {
        assertEquals(
            mk_Dtg(mk_Date(1990, 2, 1), mk_Time(1, 0, 0, 0)),
            maxDtg(
                mk_Set1(
                    mk_Dtg(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)),
                    mk_Dtg(mk_Date(1990, 1, 6), mk_Time(1, 0, 0, 0)),
                    mk_Dtg(mk_Date(1990, 2, 1), mk_Time(1, 0, 0, 0)),
                    mk_Dtg(mk_Date(1990, 1, 14), mk_Time(1, 0, 0, 0))
                )
            )
        )
        assertEquals(
            mk_Dtg(mk_Date(1990, 1, 1), mk_Time(14, 0, 0, 0)),
            maxDtg(
                mk_Set1(
                    mk_Dtg(mk_Date(1990, 1, 1), mk_Time(0, 10, 0, 0)),
                    mk_Dtg(mk_Date(1990, 1, 1), mk_Time(14, 0, 0, 0)),
                    mk_Dtg(mk_Date(1990, 1, 1), mk_Time(1, 0, 50, 0)),
                    mk_Dtg(mk_Date(1990, 1, 1), mk_Time(1, 30, 0, 0))
                )
            )
        )
        assertEquals(
            mk_Dtg(mk_Date(9999, 1, 1), mk_Time(1, 0, 0, 0)),
            maxDtg(
                mk_Set1(
                    mk_Dtg(mk_Date(0, 1, 1), mk_Time(1, 0, 0, 0)),
                    mk_Dtg(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)),
                    mk_Dtg(mk_Date(300, 1, 1), mk_Time(1, 0, 0, 0)),
                    mk_Dtg(mk_Date(9999, 1, 1), mk_Time(1, 0, 0, 0))
                )
            )

        )
    }

    @Test
    fun minDateTest() {
        assertEquals(
            mk_Date(1990, 1, 4),
            minDate(
                mk_Set1(
                    mk_Date(1990, 4, 1),
                    mk_Date(1990, 1, 4),
                    mk_Date(1990, 1, 31),
                    mk_Date(1990, 12, 1)
                )
            )
        )
        assertEquals(
            mk_Date(0, 1, 1),
            minDate(
                mk_Set1(
                    mk_Date(0, 1, 1),
                    mk_Date(1990, 1, 1),
                    mk_Date(300, 1, 1),
                    mk_Date(9999, 1, 1)
                )
            )
        )
    }

    @Test
    fun maxDateTest() {
        assertEquals(
            mk_Date(1990, 12, 1),
            maxDate(
                mk_Set1(
                    mk_Date(1990, 4, 1),
                    mk_Date(1990, 1, 4),
                    mk_Date(1990, 1, 31),
                    mk_Date(1990, 12, 1)
                )
            )
        )
        assertEquals(
            mk_Date(3000, 1, 1),
            maxDate(
                mk_Set1(
                    mk_Date(0, 1, 1),
                    mk_Date(1990, 1, 1),
                    mk_Date(300, 1, 1),
                    mk_Date(3000, 1, 1)
                )
            )
        )
    }

    @Test
    fun minTimeTest() {
        assertEquals(
            mk_Time(0, 0, 12, 1),
            minTime(
                mk_Set1(
                    mk_Time(1, 0, 4, 1),
                    mk_Time(0, 20, 1, 4),
                    mk_Time(3, 0, 1, 31),
                    mk_Time(0, 0, 12, 1)
                )
            )
        )
        assertEquals(
            mk_Time(0, 0, 0, 0),
            minTime(
                mk_Set1(
                    mk_Time(0, 0, 0, 0),
                    mk_Time(23, 0, 1, 1),
                    mk_Time(0, 59, 1, 1),
                    mk_Time(0, 0, 59, 1)
                )
            )
        )
    }

    @Test
    fun maxTimeTest() {
        assertEquals(
            mk_Time(3, 0, 1, 31),
            maxTime(
                mk_Set1(
                    mk_Time(1, 0, 4, 1),
                    mk_Time(0, 20, 1, 4),
                    mk_Time(3, 0, 1, 31),
                    mk_Time(0, 0, 12, 1)
                )
            )
        )
        assertEquals(
            mk_Time(23, 0, 1, 1),
            maxTime(
                mk_Set1(
                    mk_Time(0, 0, 1, 1),
                    mk_Time(23, 0, 1, 1),
                    mk_Time(0, 59, 1, 1),
                    mk_Time(0, 0, 59, 1)
                )
            )

        )
    }

    @Test
    fun minDurationTest() {
        assertEquals(
            Duration.fromMinutes(40), minDuration(
                mk_Set1(Duration.fromHours(3), Duration.fromMinutes(40), Duration.fromDays(2), Duration.fromHours(5))
            )
        )
        assertEquals(
            Duration.fromHours(0),
            minDuration(
                mk_Set1(Duration.fromHours(0), Duration.fromMinutes(14), Duration.fromDays(2), Duration.fromHours(23))
            )
        )
    }

    @Test
    fun maxDurationTest() {
        assertEquals(
            Duration.fromDays(2),
            maxDuration(
                mk_Set1(Duration.fromHours(3), Duration.fromMinutes(40), Duration.fromDays(2), Duration.fromHours(5))
            )
        )
        assertEquals(
            Duration.fromDays(2000000000),
            maxDuration(
                mk_Set1(
                    Duration.fromHours(0),
                    Duration.fromMinutes(14),
                    Duration.fromDays(2000000000),
                    Duration.fromHours(23)
                )
            )
        )
    }

    @Test
    fun sumDurationTest() {
        assertEquals(
            mk_Duration(204000000),
            sumDuration(
                mk_Seq(Duration.fromHours(3), Duration.fromMinutes(40), Duration.fromDays(2), Duration.fromHours(5))
            )
        )
        assertEquals(
            mk_Duration(0),
            sumDuration(mk_Seq(Duration.fromHours(0), Duration.fromMinutes(0), Duration.fromDays(0)))
        )
    }

    @Test
    fun instantTest() {
        assertEquals(
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)),
                mk_Dtg(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 1))
            ),
            mk_Dtg(mk_Date(1990, 1, 1), mk_Time(1, 0, 0, 0)).functions.instant()
        )
        assertEquals(
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 1), mk_Time(23, 59, 59, 999)),
                mk_Dtg(mk_Date(1990, 1, 2), mk_Time(0, 0, 0, 0))
            ),
            mk_Dtg(mk_Date(1990, 1, 1), mk_Time(23, 59, 59, 999)).functions.instant()
        )
    }

    @Test
    fun nextDateForYMTest() {
        assertEquals(mk_Date(1990, 2, 1), nextDateWithSameDayAsGivenDate(mk_Date(1990, 1, 1)))
        assertEquals(mk_Date(1990, 3, 31), nextDateWithSameDayAsGivenDate(mk_Date(1990, 1, 31)))
    }

    @Test
    fun nextDateForDayTest() {
        assertEquals(mk_Date(0, 1, 4), nextDateWithSameDayAsGivenDay(mk_Date(0, 1, 1), 4))
        assertEquals(mk_Date(1990, 3, 31), nextDateWithSameDayAsGivenDay(mk_Date(1990, 1, 31), 31))
        assertEquals(mk_Date(1, 1, 14), nextDateWithSameDayAsGivenDay(mk_Date(0, 12, 15), 14))
    }

    @Test
    fun previousDateForYMTest() {
        assertEquals(mk_Date(1990, 1, 3), previousDateWithSameDayAsGivenDate(mk_Date(1990, 2, 3)))
        assertEquals(mk_Date(1989, 12, 3), previousDateWithSameDayAsGivenDate(mk_Date(1990, 1, 3)))
    }

    @Test
    fun previousDateForDayTest() {
        assertEquals(mk_Date(1990, 1, 12), previousDateWithSameDayAsGivenDay(mk_Date(1990, 1, 31), 12))
        assertEquals(mk_Date(1989, 12, 12), previousDateWithSameDayAsGivenDay(mk_Date(1990, 1, 4), 12))
    }

    @Test
    fun normaliseDtgInZoneTest() {
        assertEquals(
            mk_Dtg(mk_Date(1990, 1, 1), mk_Time(3, 0, 0, 0)),
            mk_DtgInZone(
                mk_Date(1990, 1, 1),
                mk_TimeInZone(
                    mk_Time(5, 0, 0, 0), mk_Offset(Duration.fromHours(2), PlusOrMinus.Plus)
                )
            ).functions.normalise()
        )
        assertEquals(
            mk_Dtg(mk_Date(1990, 1, 1), mk_Time(7, 0, 0, 0)),
            mk_DtgInZone(
                mk_Date(1990, 1, 1),
                mk_TimeInZone(
                    mk_Time(5, 0, 0, 0), mk_Offset(Duration.fromHours(2), PlusOrMinus.Minus)
                )
            ).functions.normalise()
        )
        assertEquals(
            mk_Dtg(mk_Date(1990, 1, 1), mk_Time(5, 0, 0, 0)),
            mk_DtgInZone(
                mk_Date(1990, 1, 1),
                mk_TimeInZone(
                    mk_Time(5, 0, 0, 0), mk_Offset(Duration.fromHours(0), PlusOrMinus.Minus)
                )
            ).functions.normalise()
        )
    }

    @Test
    fun normaliseTimeInZoneTest() {
        assertEquals(
            mk_NormalisedTime(mk_Time(7, 23, 12, 0), PlusOrMinus.None),
            mk_TimeInZone(
                mk_Time(5, 23, 12, 0), mk_Offset(Duration.fromHours(2), PlusOrMinus.Minus)
            ).functions.normaliseTimeInZone()
        )
        assertEquals(
            mk_NormalisedTime(mk_Time(1, 23, 12, 0), PlusOrMinus.Minus),
            mk_TimeInZone(
                mk_Time(23, 23, 12, 0), mk_Offset(Duration.fromHours(2), PlusOrMinus.Minus)
            ).functions.normaliseTimeInZone()
        )
        assertEquals(
            mk_NormalisedTime(mk_Time(23, 23, 12, 0), PlusOrMinus.None),
            mk_TimeInZone(
                mk_Time(23, 23, 12, 0), mk_Offset(Duration.fromHours(0), PlusOrMinus.Minus)
            ).functions.normaliseTimeInZone()
        )
        assertEquals(
            mk_NormalisedTime(mk_Time(3, 23, 12, 0), PlusOrMinus.None),
            mk_TimeInZone(
                mk_Time(5, 23, 12, 0), mk_Offset(Duration.fromHours(2), PlusOrMinus.Plus)
            ).functions.normaliseTimeInZone()
        )
        assertEquals(
            mk_NormalisedTime(mk_Time(23, 23, 12, 0), PlusOrMinus.Plus),
            mk_TimeInZone(
                mk_Time(1, 23, 12, 0), mk_Offset(Duration.fromHours(2), PlusOrMinus.Plus)
            ).functions.normaliseTimeInZone()
        )

    }

    @Test
    fun formatDtgTest() {
        assertEquals(
            "1990-01-01T03:00:00",
            mk_Dtg(mk_Date(1990, 1, 1), mk_Time(3, 0, 0, 0)).functions.format()
        )
        assertEquals(
            "1990-01-01T03:00:00",
            mk_Dtg(mk_Date(1990, 1, 1), mk_Time(3, 0, 0, 0)).functions.format()
        )
        assertEquals(
            "0000-01-01T03:00:00",
            mk_Dtg(mk_Date(0, 1, 1), mk_Time(3, 0, 0, 0)).functions.format()
        )
    }

    @Test
    fun formatDtgInZoneTest() {
        assertEquals(
            "1990-01-01T03:00:00+02:00",
            mk_DtgInZone(
                mk_Date(1990, 1, 1),
                mk_TimeInZone(
                    mk_Time(3, 0, 0, 0), mk_Offset(Duration.fromHours(2), PlusOrMinus.Plus)
                )
            ).functions.format()
        )
        assertEquals(
            "1990-01-01T03:00:00-02:00",
            mk_DtgInZone(
                mk_Date(1990, 1, 1),
                mk_TimeInZone(
                    mk_Time(3, 0, 0, 0), mk_Offset(Duration.fromHours(2), PlusOrMinus.Minus)
                )
            ).functions.format()
        )
        assertEquals(
            "1990-01-01T03:00:00Z",
            mk_DtgInZone(
                mk_Date(1990, 1, 1),
                mk_TimeInZone(
                    mk_Time(3, 0, 0, 0), mk_Offset(Duration.fromHours(0), PlusOrMinus.None)
                )
            ).functions.format()
        )
    }

    @Test
    fun formatDateTest() {
        assertEquals("1990-01-01", mk_Date(1990, 1, 1).functions.format())
        assertEquals("0000-01-01", mk_Date(0, 1, 1).functions.format())
    }

    @Test
    fun formatTimeTest() {
        assertEquals("10:07:14", mk_Time(10, 7, 14, 0).functions.format())
        assertEquals("00:00:00", mk_Time(0, 0, 0, 0).functions.format())
        assertEquals("00:00:00.050", mk_Time(0, 0, 0, 50).functions.format())
    }

    @Test
    fun formatTimeInZoneTest() {
        assertEquals(
            "03:00:00+02:00",
            mk_TimeInZone(
                mk_Time(3, 0, 0, 0), mk_Offset(Duration.fromHours(2), PlusOrMinus.Plus)
            ).functions.format()
        )
        assertEquals(
            "03:00:00-02:00",
            mk_TimeInZone(
                mk_Time(3, 0, 0, 0), mk_Offset(Duration.fromHours(2), PlusOrMinus.Minus)
            ).functions.format()
        )
        assertEquals(
            "03:00:00Z",
            mk_TimeInZone(
                mk_Time(3, 0, 0, 0), mk_Offset(Duration.fromHours(0), PlusOrMinus.None)
            ).functions.format()
        )
    }

    @Test
    fun formatIntervalTest() {
        assertEquals(
            "1990-01-01T00:00:00/1990-01-06T00:00:00",
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
                mk_Dtg(mk_Date(1990, 1, 6), FirstTime)
            ).functions.format()
        )
        assertEquals(
            "1990-01-01T00:00:00/1990-01-06T05:00:00",
            mk_Interval(
                mk_Dtg(mk_Date(1990, 1, 1), mk_Time(0, 0, 0, 0)),
                mk_Dtg(mk_Date(1990, 1, 6), mk_Time(5, 0, 0, 0))
            ).functions.format()
        )
    }

    @Test
    fun formatDurationTest() {
        assertEquals("P2DT6H", Duration.fromHours(6).functions.addDuration(Duration.fromDays(2)).functions.format())
        assertEquals("PT0S", Duration.fromHours(0).functions.format())
        assertEquals(
            "PT1.001S",
            Duration.fromSeconds(1).functions.addDuration(Duration.fromMillis(1)).functions.format()
        )
    }

    @Test
    fun dtgAddMonthsTest() {
        assertEquals(
            mk_Dtg(mk_Date(1990, 3, 31), FirstTime),
            mk_Dtg(mk_Date(1990, 1, 31), FirstTime).functions.addMonths(2)
        )
        assertEquals(
            mk_Dtg(mk_Date(1990, 3, 28), mk_Time(3, 0, 0, 0)),
            mk_Dtg(mk_Date(1990, 1, 31), mk_Time(3, 0, 0, 0)).functions.addMonths(1).functions.addMonths(1)
        )
        assertEquals(
            mk_Dtg(mk_Date(1991, 1, 30), FirstTime),
            mk_Dtg(mk_Date(1990, 11, 30), FirstTime).functions.addMonths(2)
        )
    }

    @Test
    fun dtgSubtractMonthsTest() {
        assertEquals(
            mk_Dtg(mk_Date(1990, 2, 2), FirstTime),
            mk_Dtg(mk_Date(1990, 4, 2), FirstTime).functions.subtractMonths(2)
        )
        assertEquals(
            mk_Dtg(mk_Date(1990, 1, 28), mk_Time(3, 0, 0, 0)),
            (mk_Dtg(mk_Date(1990, 3, 31), mk_Time(3, 0, 0, 0))).functions.subtractMonths(1).functions.subtractMonths(1)
        )
        assertEquals(
            mk_Dtg(mk_Date(1990, 11, 2), FirstTime),
            mk_Dtg(mk_Date(1991, 1, 2), FirstTime).functions.subtractMonths(2)
        )
    }

    @Test
    fun dateAddMonthsTest() {
        assertEquals(
            mk_Date(1990, 3, 31), mk_Date(1990, 1, 31).functions.addMonths(2)
        )
        assertEquals(
            mk_Date(1990, 3, 28), mk_Date(1990, 1, 31).functions.addMonths(1).functions.addMonths(1)
        )
        assertEquals(
            mk_Date(1991, 1, 30), mk_Date(1990, 11, 30).functions.addMonths(2)
        )
    }

    @Test
    fun dateSubtractMonthsTest() {
        assertEquals(
            mk_Date(1990, 2, 2), mk_Date(1990, 4, 2).functions.subtractMonths(2)
        )
        assertEquals(
            mk_Date(1990, 1, 28), mk_Date(1990, 3, 31).functions.subtractMonths(1).functions.subtractMonths(1)
        )
        assertEquals(
            mk_Date(1990, 11, 2), mk_Date(1991, 1, 2).functions.subtractMonths(2)
        )
    }

    @Test
    fun dtgAddDays() {
        assertEquals(
            mk_Dtg(mk_Date(1990, 1, 10), FirstTime),
            mk_Dtg(mk_Date(1990, 1, 1), FirstTime).functions.addDays(9)
        )
        assertEquals(
            mk_Dtg(mk_Date(1990, 2, 1), FirstTime),
            mk_Dtg(mk_Date(1990, 1, 1), FirstTime).functions.addDays(31)
        )
        assertEquals(
            mk_Dtg(mk_Date(1990, 2, 2), FirstTime),
            mk_Dtg(mk_Date(1990, 1, 1), FirstTime).functions.addDays(32)
        )
        assertEquals(
            mk_Dtg(mk_Date(1992, 3, 1), FirstTime),
            mk_Dtg(mk_Date(1992, 2, 1), FirstTime).functions.addDays(29)
        )
        assertEquals(
            mk_Dtg(mk_Date(1991, 1, 2), FirstTime),
            mk_Dtg(mk_Date(1990, 1, 1), FirstTime).functions.addDays(366)
        )
    }

    @Test
    fun dtgSubtractDays() {
        assertEquals(
            mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
            mk_Dtg(mk_Date(1990, 1, 10), FirstTime).functions.subtractDays(9)
        )
        assertEquals(
            mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
            mk_Dtg(mk_Date(1990, 2, 1), FirstTime).functions.subtractDays(31)
        )
        assertEquals(
            mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
            mk_Dtg(mk_Date(1990, 2, 2), FirstTime).functions.subtractDays(32)
        )
        assertEquals(
            mk_Dtg(mk_Date(1992, 2, 1), FirstTime),
            mk_Dtg(mk_Date(1992, 3, 1), FirstTime).functions.subtractDays(29)
        )
        assertEquals(
            mk_Dtg(mk_Date(1990, 1, 1), FirstTime),
            mk_Dtg(mk_Date(1991, 1, 2), FirstTime).functions.subtractDays(366)
        )
    }

    @Test
    fun dateAddDays() {
        assertEquals(mk_Date(1990, 1, 10), mk_Date(1990, 1, 1).functions.addDays(9))
        assertEquals(mk_Date(1990, 2, 1), mk_Date(1990, 1, 1).functions.addDays(31))
        assertEquals(mk_Date(1990, 2, 2), mk_Date(1990, 1, 1).functions.addDays(32))
        assertEquals(mk_Date(1992, 3, 1), mk_Date(1992, 2, 1).functions.addDays(29))
        assertEquals(mk_Date(1991, 1, 2), mk_Date(1990, 1, 1).functions.addDays(366))
    }

    @Test
    fun dateSubtractDays() {
        assertEquals(mk_Date(1990, 1, 1), mk_Date(1990, 1, 10).functions.subtractDays(9))
        assertEquals(mk_Date(1990, 1, 1), mk_Date(1990, 2, 1).functions.subtractDays(31))
        assertEquals(mk_Date(1990, 1, 1), mk_Date(1990, 2, 2).functions.subtractDays(32))
        assertEquals(mk_Date(1992, 2, 1), mk_Date(1992, 3, 1).functions.subtractDays(29))
        assertEquals(mk_Date(1990, 1, 1), mk_Date(1991, 1, 2).functions.subtractDays(366))
    }

    @Test
    fun monthsBetweenDatesTest() {
        assertEquals(
            0,
            monthsBetweenDtgs(
                mk_Dtg(mk_Date(1990, 1, 1), FirstTime), mk_Dtg(mk_Date(1990, 1, 1), FirstTime)
            )
        )
        assertEquals(
            0,
            monthsBetweenDtgs(
                mk_Dtg(mk_Date(1990, 1, 12), FirstTime), mk_Dtg(mk_Date(1990, 2, 1), FirstTime)
            )
        )
        assertEquals(
            6,
            monthsBetweenDtgs(
                mk_Dtg(mk_Date(1990, 12, 12), FirstTime), mk_Dtg(mk_Date(1991, 6, 13), FirstTime)
            )
        )
    }

    @Test
    fun yearsBetweenDatesTest() {
        assertEquals(
            0,
            yearsBetweenDtgs(
                mk_Dtg(mk_Date(1990, 1, 1), FirstTime), mk_Dtg(mk_Date(1990, 1, 1), FirstTime)
            )
        )
        assertEquals(
            0,
            yearsBetweenDtgs(
                mk_Dtg(mk_Date(1990, 1, 12), FirstTime), mk_Dtg(mk_Date(1990, 12, 1), FirstTime)
            )
        )
        assertEquals(
            2,
            yearsBetweenDtgs(
                mk_Dtg(mk_Date(1990, 1, 12), FirstTime), mk_Dtg(mk_Date(1992, 3, 13), FirstTime)
            )
        )
    }

    @Test
    fun isStringADateTest() {
        assertEquals(true, isStringIsoDate("2018-04-01"))
        assertEquals(false, isStringIsoDate("2018/04/01"))
        assertEquals(false, isStringIsoDate("2018-04"))
        assertEquals(false, isStringIsoDate("2018-02-30"))
        assertEquals(false, isStringIsoDate("2018-AA-01"))
        assertEquals(false, isStringIsoDate("20.8-04-01"))
        assertEquals(false, isStringIsoDate("-128-04-01"))
        assertEquals(false, isStringIsoDate("2018-24-01"))
        assertEquals(false, isStringIsoDate("2018-04-66"))

    }

    @Test
    fun strToDateTest() {
        assertEquals(mk_Date(2018, 4, 1), strToDate("2018-04-01"))
        assertFailsWith<PreconditionFailure> { strToDate("2018-04-011") }
    }

    @Test
    fun isStringADtgTest() {
        assertEquals(true, isStringIsoDtg("1990-01-01T00:00:00"))
        assertEquals(false, isStringIsoDtg("-990-01-01T00:00:00"))
        assertEquals(false, isStringIsoDtg("1990-01-01!00:00:00"))
        assertEquals(false, isStringIsoDtg("1990-01-01T"))
        assertEquals(false, isStringIsoDtg("1990-G1-01T00:00:00"))
        assertEquals(false, isStringIsoDtg("1990-13-01T00:00:00"))
        assertEquals(false, isStringIsoDtg("1990-01-41T00:00:00"))
        assertEquals(false, isStringIsoDtg("1990-01-01T24:00:00"))
        assertEquals(false, isStringIsoDtg("1990-01-01T00:60:00"))
        assertEquals(false, isStringIsoDtg("1990-01-01T00:00:60"))
        assertEquals(false, isStringIsoDtg("19.0-01-01T00:00:00"))

        assertEquals(true, isStringIsoDtg("1990-01-01T00:00:00.000"))
        assertEquals(false, isStringIsoDtg("1990-01-01T00:00:00.FFF"))
        assertEquals(false, isStringIsoDtg("1990-01-01T00:00:00-000"))
        assertEquals(false, isStringIsoDtg("1990-01-01T00:00:00.-01"))

    }

    @Test
    fun strToDtgTest() {
        assertEquals(
            mk_Dtg(mk_Date(1990, 1, 1), mk_Time(12, 23, 0, 0)),
            strToDtg("1990-01-01T12:23:00")
        )
        assertEquals(
            mk_Dtg(mk_Date(1990, 1, 1), mk_Time(12, 23, 0, 1)),
            strToDtg("1990-01-01T12:23:00.001")
        )
        assertFailsWith<PreconditionFailure> { strToDtg("1990-01-01T12:23:00.0011") }
    }
}

