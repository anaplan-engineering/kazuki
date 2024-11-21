package com.anaplan.engineering.kazuki.toolkit.ISO8601

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Date_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Dtg_Module.mk_Dtg
import kotlin.math.min

@Module
interface Date : Comparable<Date> {
    val year: Year
    val month: Month
    val day: Day

    @Invariant
    fun isDayValid() = day <= daysInMonth(year, month)

    private val dateComparator get() = compareBy<Date> { it.year }.thenBy { it.month }.thenBy { it.day }
    override fun compareTo(other: Date) = dateComparator.compare(this, other)


    @FunctionProvider(DateFunctions::class)
    val functions: DateFunctions

    class DateFunctions(private val date: Date) {

        val toDurationSinceFirstDate: () -> Duration = function<Duration>(
            command = {
                Duration.durationFromFirstYearUpToStartOfYear(date.year).functions.addDuration(
                    Duration.durationInYearUpToStartOfMonth(date.year, date.month).functions.addDuration(
                        Duration.fromDays(date.day - 1L)
                    )
                )
            },
//        post = { result -> result.functions.toDate() == date }
//        This post condition uses a function whose post condition uses this function as a post condition.
//        If not commented, the two functions will recur until a stack overflow error occurs.
//        However, it is still a valid post condition so is left here for completeness.
        )

        val format: () -> String = function(
            command = { String.format("%04d-%02d-%02d", date.year, date.month, date.day) },
            post = { result -> isStringIsoDate(result) }
        )

        val toDayOfWeek: () -> DayOfWeek = function<DayOfWeek>(
            command = {
                iota(DayOfWeek.entries) { day ->
                    day.ordinal == ((date.functions.toDurationSinceFirstDate().functions.toDays().toInt() - 365) % 7)
                }
            }
        )

        val addMonths: (int) -> Date = function(
            command = { n ->
                val nextMonth = (date.month + n - 1).mod(MonthsPerYear) + 1
                val yearShift = (date.month + n - 1).floorDiv(MonthsPerYear)
                val nextYear = date.year + yearShift
                val nextDay = min(date.day, daysInMonth(nextYear, nextMonth))
                mk_Date(nextYear, nextMonth, nextDay)
            },
            pre = { n -> date.year * 12 + date.month + n > 0 }
        )

        val subtractMonths: (int) -> Date = function(
            command = { n -> date.functions.addMonths(-n) },
        )

        val addDays: (nat) -> Date = function(
            command = { n -> date.functions.toDurationSinceFirstDate().functions.addDuration(Duration.fromDays(n.toLong())).functions.toDateAfterFirstDate() },
            post = { n, result -> result.functions.subtractDays(n) == date }
        )

        val subtractDays: (nat) -> Date = function(
            command = { n -> date.functions.toDurationSinceFirstDate().functions.subtractDuration(Duration.fromDays(n.toLong())).functions.toDateAfterFirstDate() },
            pre = { n -> date.functions.toDurationSinceFirstDate().functions.toDays() >= n },
//            post = {n, result -> result.functions.addDays(n) == date}
//        This post condition uses a function whose post condition uses this function as a post condition.
//        If not commented, the two functions will recur until a stack overflow error occurs.
//        However, it is still a valid post condition so is left here for completeness.

        )

        val toDtgAtStartOfDay: () -> Dtg = function<Dtg>(
            command = { mk_Dtg(date, FirstTime) }
        )

    }
}

@PrimitiveInvariant(name = "Year", base = nat::class)
fun yearNotInRange(year: nat) = year in FirstYear..LastYear

@PrimitiveInvariant(name = "Month", base = nat1::class)
fun monthNotInRange(month: nat1) = month in 1..MonthsPerYear

@PrimitiveInvariant(name = "Day", base = nat1::class)
fun dayNotInRange(day: nat1) = day in 1..DaysPerMonth.rng.max()

val isLeap: (Year) -> bool = function(
    command = { year ->
        val multipleOfFour = year % 4 == 0
        val multipleOfOneHundred = year % 100 == 0
        val multipleOfFourHundred = year % 400 == 0
        multipleOfFour && (multipleOfOneHundred implies multipleOfFourHundred)
    }
)

val daysInYear: (Year) -> nat1 = function(
    command = { year -> seq(1..MonthsPerYear) { daysInMonth(year, it) }.sum() }
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
            val nextMonth = if (dateMonth == MonthsPerYear) 1 else dateMonth + 1
            val nextYear = if (dateMonth == MonthsPerYear) dateYear + 1 else dateYear

            if (dateDay < targetDay && targetDay <= daysInMonth(dateYear, dateMonth)) {
                mk_Date(dateYear, dateMonth, targetDay)
            } else if (targetDay == 1) {
                mk_Date(nextYear, nextMonth, targetDay)
            } else {
                nextDateFromGivenYearMonthDayForGivenDay(nextYear, nextMonth, 1, targetDay)
            }
        },
        pre = { dateYear, dateMonth, dateDay, _ -> dateDay <= daysInMonth(dateYear, dateMonth) },
        measure = { dateYear, dateMonth, _, _ -> ((LastYear + 1 - dateYear) * MonthsPerYear) - dateMonth }
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
            val prevMonth = if (dateMonth > 1) dateMonth - 1 else MonthsPerYear
            val prevYear = if (dateMonth > 1) dateYear else dateYear - 1

            if (targetDay < dateDay) {
                mk_Date(dateYear, dateMonth, targetDay)
            } else if (targetDay <= daysInMonth(prevYear, prevMonth)) {
                mk_Date(prevYear, prevMonth, targetDay)
            } else {
                previousDateFromGivenYearMonthDayForGivenDay(prevYear, prevMonth, 1, targetDay)
            }
        },
        pre = { dateYear, dateMonth, dateDay, _ -> dateDay <= daysInMonth(dateYear, dateMonth) },
        measure = { dateYear, dateMonth, _, _ -> dateYear * MonthsPerYear + dateMonth }
    )
}
