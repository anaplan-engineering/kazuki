package com.anaplan.engineering.kazuki.toolkit.iso8601

import com.anaplan.engineering.kazuki.toolkit.iso8601.Date_Module.mk_Date
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class TestIso8601BetweenDtgs(
    private val name: String,
    private val earlier: Dtg,
    private val later: Dtg,
    // we can't use nat here, it breaks parameterized tests
    private val months: Int,
    private val years: Int
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun testInputs(): Collection<Array<Any?>> =
            listOf(
                //
                // Straightforward common year cases
                //
                case(
                    "basic case, common year",
                    earlier = midnightOn(year = 2006u, month = 1u, day = 1u),
                    later = midnightOn(year = 2007u, month = 3u, day = 1u),
                    months = 14,
                    years = 1,
                ),
                case(
                    "exactly one month, start on common year",
                    earlier = midnightOn(year = 2007u, month = 2u, day = 28u),
                    later = midnightOn(year = 2007u, month = 3u, day = 28u),
                    months = 1,
                    years = 0,
                ),
                case(
                    "exactly one year, start on common year",
                    earlier = midnightOn(year = 2007u, month = 2u, day = 28u),
                    later = midnightOn(year = 2008u, month = 2u, day = 28u),
                    months = 12,
                    years = 1,
                ),
                //
                // Edge cases on month and year crossover
                //
                case(
                    "one millisecond short of month boundary",
                    earlier = oneMilliAfterMidnightOn(year = 2008u, month = 1u, day = 1u),
                    later = midnightOn(year = 2008u, month = 2u, day = 1u),
                    months = 0,
                    years = 0,
                ),
                case(
                    "one millisecond short of year boundary",
                    earlier = oneMilliAfterMidnightOn(year = 2008u, month = 1u, day = 1u),
                    later = midnightOn(year = 2009u, month = 1u, day = 1u),
                    months = 11,
                    years = 0,
                ),
                case(
                    "one day short of month boundary",
                    earlier = midnightOn(year = 2008u, month = 1u, day = 2u),
                    later = midnightOn(year = 2008u, month = 2u, day = 1u),
                    months = 0,
                    years = 0,
                ),
                case(
                    "one day short of year boundary",
                    earlier = midnightOn(year = 2008u, month = 1u, day = 2u),
                    later = midnightOn(year = 2009u, month = 1u, day = 1u),
                    months = 11,
                    years = 0,
                ),
                case(
                    "one day one milli short of month boundary",
                    earlier = oneMilliAfterMidnightOn(year = 2007u, month = 1u, day = 2u),
                    later = midnightOn(year = 2007u, month = 2u, day = 1u),
                    months = 0,
                    years = 0,
                ),
                case(
                    "one day one milli short of year boundary",
                    earlier = oneMilliAfterMidnightOn(year = 2007u, month = 1u, day = 2u),
                    later = midnightOn(year = 2008u, month = 1u, day = 1u),
                    months = 11,
                    years = 0,
                ),
                //
                // Leap years
                //
                case(
                    "exactly one month, start on leap year, straddling leap day",
                    earlier = midnightOn(year = 2008u, month = 2u, day = 28u),
                    later = midnightOn(year = 2008u, month = 3u, day = 28u),
                    months = 1,
                    years = 0,
                ),
                case(
                    "exactly one month, start on leap year, just after leap day",
                    earlier = midnightOn(year = 2008u, month = 3u, day = 1u),
                    later = midnightOn(year = 2008u, month = 4u, day = 1u),
                    months = 1,
                    years = 0,
                ),
                case(
                    "exactly one year, start on leap year, straddling leap day",
                    earlier = midnightOn(year = 2008u, month = 2u, day = 28u),
                    later = midnightOn(year = 2009u, month = 2u, day = 28u),
                    months = 12,
                    years = 1,
                ),
                case(
                    "exactly one year, start on leap year, just after leap day",
                    earlier = midnightOn(year = 2008u, month = 3u, day = 1u),
                    later = midnightOn(year = 2009u, month = 3u, day = 1u),
                    months = 12,
                    years = 1,
                ),
                case(
                    "leap day to leap day",
                    earlier = midnightOn(year = 2008u, month = 2u, day = 29u),
                    later = midnightOn(year = 2012u, month = 2u, day = 29u),
                    months = 48,
                    years = 4,
                ),
                //
                // Other pathological cases and regressions
                //
                case(
                    "same dtg",
                    earlier = midnightOn(year = 2005u, month = 1u, day = 1u),
                    later = midnightOn(year = 2005u, month = 1u, day = 1u),
                    months = 0,
                    years = 0,
                ),
                case(
                    "very large timespan",
                    earlier = midnightOn(year = 1000u, month = 1u, day = 1u),
                    later = midnightOn(year = 2000u, month = 1u, day = 1u),
                    months = 12_000,
                    years = 1_000,
                ),
                case(
                    "one day long, one milli short",
                    earlier = oneMilliAfterMidnightOn(year=2008u, month=2u, day=1u),
                    later = midnightOn(year=2008u, month=3u, day=2u),
                    months = 1,
                    years = 0
                ),
                case(
                    "one milli short of two months",
                    earlier = oneMilliAfterMidnightOn(year=1995u, month=1u, day=1u),
                    later = midnightOn(year=1995u, month=3u, day=1u),
                    months = 1,
                    years = 0
                ),
                case(
                    "one day one milli short of two months",
                    earlier = oneMilliAfterMidnightOn(year=1995u, month=1u, day=1u),
                    later = midnightOn(year=1995u, month=2u, day=28u),
                    months = 1,
                    years = 0
                ),
                case(
                    "incomplete different month",
                    earlier = midnightOn(year=2008u, month=1u, day=1u),
                    later = midnightOn(year=2009u, month=2u, day=2u),
                    months = 13,
                    years = 1
                ),
                case(
                    "postcondition forwards clipping",
                    earlier = midnightOn(year = 2007u, month = 1u, day = 31u),
                    later = midnightOn(year = 2007u, month = 2u, day = 28u),
                    months = 0,
                    years = 0,
                ),
                case(
                    "postcondition backwards clipping",
                    earlier = midnightOn(year = 2007u, month = 2u, day = 28u),
                    later = midnightOn(year = 2007u, month = 3u, day = 31u),
                    months = 1,
                    years = 0,
                ),
            )

        private fun case(name: String, earlier: Dtg, later: Dtg, months: Int, years: Int): Array<Any?> =
            arrayOf(name, earlier, later, months, years)

        private fun midnightOn(year: Year, month: Month, day: Day) =
            mk_Date(year, month, day).properties.dtgAtStartOfDay

        private fun oneMilliAfterMidnightOn(year: Year, month: Month, day: Day) =
            midnightOn(year, month, day).functions.addDuration(OneMillisecondDuration)
    }

    @Test
    fun testMonths() {
        val actual = DtgUtilities.monthsBetweenDtgs(earlier, later)

        assertEquals(months, actual.toInt())
    }

    @Test
    fun testYears() {
        val actual = DtgUtilities.yearsBetweenDtgs(earlier, later)

        assertEquals(years, actual.toInt())
    }
}