package com.anaplan.engineering.kazuki.toolkit.ISO8601

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Date_Module.mk_Date

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

        val toDuration: () -> Duration = function<Duration>(
            command = {
                Duration.durationUpToYear(date.year).functions.addDuration(
                    Duration.durationUpToMonth(date.year, date.month).functions.addDuration(
                        Duration.fromDays(date.day - 1L)
                    )
                )
            },
//        post = { result -> result.functions.toDate() == date }
        )

        val format: () -> String = function<String>(
            command = { String.format("%04d-%02d-%02d", date.year, date.month, date.day) }
        )

        val toDayOfWeek: () -> DayOfWeek = function<DayOfWeek>(
            command = {
                DayOfWeek.entries.find {
                    it.dayNumber == (date.functions.toDuration().functions.toDays().toInt() - 365) % 7
                }!!
            }
        )

        val addMonths: (nat) -> Date = function(
            command = { n ->
                val nextMonth = ((date.month + n - 1) % MonthsPerYear) + 1
                val nextYear = date.year + (date.month + n - 1) / MonthsPerYear
                if (date.day > daysInMonth(nextYear, nextMonth)) {
                    mk_Date(nextYear, nextMonth, daysInMonth(nextYear, nextMonth))
                } else {
                    mk_Date(nextYear, nextMonth, date.day)
                }
            },
        )

        val subtractMonths: (nat) -> Date = function(
            command = { n ->
                val nextMonth = (date.month - n - 1).mod(MonthsPerYear) + 1
                val nextYear = date.year + (date.month - n - 12) / MonthsPerYear
                if (date.day > daysInMonth(nextYear, nextMonth)) {
                    mk_Date(nextYear, nextMonth, daysInMonth(nextYear, nextMonth))
                } else {
                    mk_Date(nextYear, nextMonth, date.day)
                }
            }
        )

        val addDays: (nat) -> Date = function(
            command = { n -> date.functions.toDuration().functions.addDuration(Duration.fromDays(n.toLong())).functions.toDate() },
            post = { n, result -> result.functions.subtractDays(n) == date }
        )

        val subtractDays: (nat) -> Date = function(
            command = { n -> date.functions.toDuration().functions.subtractDuration(Duration.fromDays(n.toLong())).functions.toDate() },
            pre = { n -> date.functions.toDuration() >= Duration.fromDays(n.toLong()) },
//            post = {n, result -> result.functions.addDays(n) == date}
        )

    }
}

@PrimitiveInvariant(name = "Year", base = nat::class)
fun yearNotInRange(year: nat) = year in FirstYear..LastYear

@PrimitiveInvariant(name = "Month", base = nat1::class)
fun monthNotInRange(month: nat1) = month <= MonthsPerYear

@PrimitiveInvariant(name = "Day", base = nat1::class)
fun dayNotInRange(day: nat1) = day <= MaxDaysPerMonth

val isLeap: (Year) -> bool = function(
    command = { year -> year % 4 == 0 && ((year % 100 == 0) implies { year % 400 == 0 }) }
)

val daysInYear: (Year) -> nat1 = function(
    command = { year -> seq(1..MonthsPerYear) { daysInMonth(year, it) }.sum() }
)

val daysInMonth: (Year, Month) -> nat1 = function(
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

// TODO Review below functions
val nextDateForYM: (Date) -> Date = function(
    command = { date -> nextDateForDay(date, date.day) }
)
val nextDateForDay: (Date, Day) -> Date = function(
    command = { date, day -> nextYMDForDay(date.year, date.month, date.day, day) },
    pre = { _, day -> day <= MaxDaysPerMonth }
)
val nextYMDForDay: (Year, Month, Day, Day) -> Date by lazy {
    function(
        command = { dateYear, dateMonth, dateDay, targetDay ->
            val nextMonth = if (dateMonth == MonthsPerYear) 1 else dateMonth + 1
            val nextYear = if (dateMonth == MonthsPerYear) dateYear + 1 else dateYear

            if (dateDay < targetDay && targetDay <= daysInMonth(dateYear, dateMonth)) {
                mk_Date(dateYear, dateMonth, targetDay)
            } else if (targetDay == 1) {
                mk_Date(nextYear, nextMonth, targetDay)
            } else {
                nextYMDForDay(nextYear, nextMonth, 1, targetDay)
            }
        },
        pre = { dateYear, dateMonth, dateDay, _ -> dateDay <= daysInMonth(dateYear, dateMonth) },
        measure = { dateYear, dateMonth, _, _ -> ((LastYear + 1 - dateYear) * MonthsPerYear) - dateMonth }
    )
}

val previousDateWithSameDay: (Date) -> Date = function(
    command = { date -> previousDateWithDayMatchingDay(date, date.day) }
)
val previousDateWithDayMatchingDay: (Date, Day) -> Date = function(
    command = { date, day -> previousYMDForDay(date.year, date.month, date.day, day) },
    pre = { _, day -> day <= MaxDaysPerMonth }
)
val previousYMDForDay: (Year, Month, Day, Day) -> Date by lazy {
    function(
        command = { dateYear, dateMonth, dateDay, targetDay ->
            val prevMonth = if (dateMonth > 1) dateMonth - 1 else MonthsPerYear
            val prevYear = if (dateMonth > 1) dateYear else dateYear - 1

            if (targetDay < dateDay) {
                mk_Date(dateYear, dateMonth, targetDay)
            } else if (targetDay <= daysInMonth(prevYear, prevMonth)) {
                mk_Date(prevYear, prevMonth, targetDay)
            } else {
                previousYMDForDay(prevYear, prevMonth, 1, targetDay)
            }
        },
        pre = { dateYear, dateMonth, dateDay, _ -> dateDay <= daysInMonth(dateYear, dateMonth) },
        measure = { dateYear, dateMonth, _, _ -> dateYear * MonthsPerYear + dateMonth }
    )
}
