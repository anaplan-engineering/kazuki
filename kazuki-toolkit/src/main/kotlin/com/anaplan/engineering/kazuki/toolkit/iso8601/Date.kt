package com.anaplan.engineering.kazuki.toolkit.iso8601

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.iso8601.DateUtilities.daysInMonth
import com.anaplan.engineering.kazuki.toolkit.iso8601.Date_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.iso8601.Dtg_Module.mk_Dtg
import kotlin.math.min

@Module
interface Date : Comparable<Date> {
    val year: Year
    val month: Month
    val day: Day

    // TODO - remove once full support for primitive invariants implemented
    @Invariant
    fun primitivesValid() = yearInRange(year) && monthInRange(month) && dayInRange(day)

    @Invariant
    fun isDayValid() = day <= daysInMonth(year, month)

    private val dateComparator get() = compareBy<Date> { it.year }.thenBy { it.month }.thenBy { it.day }
    override fun compareTo(other: Date) = dateComparator.compare(this, other)

    @FunctionProvider(DateFunctions::class)
    val functions: DateFunctions

    @FunctionProvider(DateProperties::class)
    val properties: DateProperties

}

class DateProperties(private val date: Date) {
    val formatted by lazy {
        String.format(
            "%04d-%02d-%02d",
            date.year.safeToInt(),
            date.month.safeToInt(),
            date.day.safeToInt()
        )
    }

    val dayOfWeek by lazy {
        DayOfWeek.entries[((durationSinceFirstDate.properties.days - 365u) % 7u).safeToInt()]
    }

    val dtgAtStartOfDay by lazy { mk_Dtg(date, FirstTime) }

    val durationSinceFirstDate by lazy {
        Duration.durationFromFirstYearUpToStartOfYear(date.year).functions.addDuration(
            Duration.durationInYearUpToStartOfMonth(date.year, date.month).functions.addDuration(
                Duration.fromDays(date.day - 1uL)
            )
        )
    }
}

class DateFunctions(private val date: Date) {
    val addYears = function(
        command = { n: nat ->
            val nextYear = date.year + n
            val nextMonth = date.month
            val nextDay = min(date.day, daysInMonth(nextYear, nextMonth))

            mk_Date(nextYear, nextMonth, nextDay)
        },
        post = { n, result -> result == date.functions.addMonths(n * MonthsPerYear) }
    )

    val subtractYears = function(
        command = { n: nat ->
            val nextYear = date.year - n
            val nextMonth = date.month
            val nextDay = min(date.day, daysInMonth(nextYear, nextMonth))

            mk_Date(nextYear, nextMonth, nextDay)
        },
        pre = { n -> date.year > n },
        post = { n, result -> result == date.functions.subtractMonths(n * MonthsPerYear) }
    )

    val addMonths: (nat) -> Date = function(
        command = { n ->
            val yearsToAdd = n / MonthsPerYear
            val monthsToAdd = n % MonthsPerYear
            val willOverflow = MonthsPerYear < date.month + monthsToAdd

            val nextYear = if (willOverflow) {
                date.year + (yearsToAdd + 1u)
            } else {
                date.year + yearsToAdd
            }

            val nextMonth = if (willOverflow) {
                date.month + (monthsToAdd - MonthsPerYear)
            } else {
                date.month + monthsToAdd
            }

            val nextDay = min(date.day, daysInMonth(nextYear, nextMonth))

            mk_Date(nextYear, nextMonth, nextDay)
        },
    )

    val subtractMonths: (nat) -> Date = function(
        command = { n ->
            val yearsToSubtract = n / MonthsPerYear
            val monthsToSubtract = n % MonthsPerYear
            val willUnderflow = date.month <= monthsToSubtract

            val nextYear = if (willUnderflow) {
                date.year - (yearsToSubtract + 1u)
            } else {
                date.year - yearsToSubtract
            }

            val nextMonth = if (willUnderflow) {
                (date.month + MonthsPerYear) - monthsToSubtract
            } else {
                date.month - monthsToSubtract
            }

            val nextDay = min(date.day, daysInMonth(nextYear, nextMonth))

            mk_Date(nextYear, nextMonth, nextDay)
        },
        pre = { n -> (date.year * MonthsPerYear) + date.month > n },
    )

    val addDays: (nat) -> Date = function(
        command = { n -> date.properties.durationSinceFirstDate.functions.addDuration(Duration.fromDays(n)).functions.toDateAfterFirstDate() },
        post = { n, result -> result.functions.subtractDays(n) == date }
    )

    val subtractDays: (nat) -> Date = function(
        command = { n -> date.properties.durationSinceFirstDate.functions.subtractDuration(Duration.fromDays(n)).functions.toDateAfterFirstDate() },
        pre = { n -> date.properties.durationSinceFirstDate.properties.days >= n },
//            post = {n, result -> result.functions.addDays(n) == date}
//        This post condition uses a function whose post condition uses this function as a post condition.
//        If not commented, the two functions will recur until a stack overflow error occurs.
//        However, it is still a valid post condition so is left here for completeness.

    )
}

@PrimitiveInvariant(name = "Year", base = nat::class)
fun yearInRange(year: nat) = year in FirstYear..LastYear

@PrimitiveInvariant(name = "Month", base = nat1::class)
fun monthInRange(month: nat1) = month in 1uL..MonthsPerYear

@PrimitiveInvariant(name = "Day", base = nat1::class)
fun dayInRange(day: nat1) = day in 1uL..DaysPerMonth.rng.max()

object DateUtilities {
    val isLeap: (Year) -> bool = function(
        command = { year ->
            val multipleOfFour = year % 4u == 0uL
            val multipleOfOneHundred = year % 100u == 0uL
            val multipleOfFourHundred = year % 400u == 0uL
            multipleOfFour && (multipleOfOneHundred implies multipleOfFourHundred)
        }
    )

    val daysInYear: (Year) -> nat1 = function(
        command = { year -> seq(1uL..MonthsPerYear) { daysInMonth(year, it) }.sum() }
    )

    val daysInMonth: (Year, Month) -> Day = function(
        command = { year, month -> if (isLeap(year)) DaysPerMonthLeap[month] else DaysPerMonth[month] }
    )

    val minDate: (Set1<Date>) -> Date = function(
        command = { dates -> dates.min() },
        post = { dates, result -> result in dates && forall(dates) { result <= it } }
    )
    val maxDate: (Set1<Date>) -> Date = function(
        command = { dates -> dates.max() },
        post = { dates, result -> result in dates && forall(dates) { result >= it } }
    )

    val nextDateWithSameDayAsGivenDate: (Date) -> Date = function(
        command = { date -> nextDateFromGivenYearMonthDayForGivenDay(date.year, date.month, date.day, date.day) }
    )
    val nextDateWithSameDayAsGivenDay: (Date, Day) -> Date = function(
        command = { date, day -> nextDateFromGivenYearMonthDayForGivenDay(date.year, date.month, date.day, day) },
        pre = { _, day -> day <= DaysPerMonth.rng.max() }
    )
    val nextDateFromGivenYearMonthDayForGivenDay: (Year, Month, Day, Day) -> Date by lazy {
        function(
            command = { dateYear, dateMonth, dateDay, targetDay ->
                val nextMonth = if (dateMonth == MonthsPerYear) 1uL else dateMonth + 1uL
                val nextYear = if (dateMonth == MonthsPerYear) dateYear + 1uL else dateYear
                if (dateDay < targetDay && targetDay <= daysInMonth(dateYear, dateMonth)) {
                    mk_Date(dateYear, dateMonth, targetDay)
                } else if (targetDay == 1uL) {
                    mk_Date(nextYear, nextMonth, targetDay)
                } else {
                    nextDateFromGivenYearMonthDayForGivenDay(nextYear, nextMonth, 1uL, targetDay)
                }
            },
            pre = { dateYear, dateMonth, dateDay, _ -> dateDay <= daysInMonth(dateYear, dateMonth) },
            measure = { dateYear, dateMonth, _, _ -> ((LastYear + 1uL - dateYear) * MonthsPerYear) - dateMonth }
        )
    }

    val previousDateWithSameDayAsGivenDate: (Date) -> Date = function(
        command = { date -> previousDateFromGivenYearMonthDayForGivenDay(date.year, date.month, date.day, date.day) }
    )
    val previousDateWithSameDayAsGivenDay: (Date, Day) -> Date = function(
        command = { date, day -> previousDateFromGivenYearMonthDayForGivenDay(date.year, date.month, date.day, day) },
        pre = { _, day -> day <= DaysPerMonth.rng.max() }
    )
    val previousDateFromGivenYearMonthDayForGivenDay: (Year, Month, Day, Day) -> Date by lazy {
        function(
            command = { dateYear, dateMonth, dateDay, targetDay ->
                val prevMonth = if (dateMonth > 1uL) dateMonth - 1uL else MonthsPerYear
                val prevYear = if (dateMonth > 1uL) dateYear else dateYear - 1uL

                if (targetDay < dateDay) {
                    mk_Date(dateYear, dateMonth, targetDay)
                } else if (targetDay <= daysInMonth(prevYear, prevMonth)) {
                    mk_Date(prevYear, prevMonth, targetDay)
                } else {
                    previousDateFromGivenYearMonthDayForGivenDay(prevYear, prevMonth, 1uL, targetDay)
                }
            },
            pre = { dateYear, dateMonth, dateDay, _ -> dateDay <= daysInMonth(dateYear, dateMonth) },
            measure = { dateYear, dateMonth, _, _ -> dateYear * MonthsPerYear + dateMonth }
        )
    }
}