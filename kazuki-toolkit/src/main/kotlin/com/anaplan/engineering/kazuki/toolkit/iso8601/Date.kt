package com.anaplan.engineering.kazuki.toolkit.iso8601

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.iso8601.DateUtilities.daysInMonth
import com.anaplan.engineering.kazuki.toolkit.iso8601.Date_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.iso8601.Dtg_Module.mk_Dtg
import com.anaplan.engineering.kazuki.toolkit.iso8601.Duration_Module.mk_Duration
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Module
interface Date : PrettyPrintable {
    val year: Year
    val month: Month
    val day: Day

    @Invariant
    fun isDayValid() = day <= daysInMonth(year, month)

    override fun pretty() = properties.formatted

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
        DurationUtilities.fromDays(ChronoUnit.DAYS.between(FirstDate.toLocalDate(), date.toLocalDate()).toNat())
    }
}

class DateFunctions(private val date: Date) {

    private val localDate by lazy { date.toLocalDate() }

    val isEarlierThan = function(
        command = { other: Date -> localDate < other.toLocalDate() },
        post = { other, result ->
            result iff (date.year < other.year || (date.year == other.year && (date.month < other.month || (date.month == other.month && date.day < other.day))))
        }
    )

    val addYears = function(
        command = { n: nat -> localDate.plusYears(n.toLong()).toDate() },
        pre = { n -> date.year + n <= LastYear },
        post = { n, result -> result == date.functions.addMonths(n * MonthsPerYear) }
    )

    val subtractYears = function(
        command = { n: nat -> localDate.minusYears(n.toLong()).toDate() },
        pre = { n -> date.year > n },
        post = { n, result -> result == date.functions.subtractMonths(n * MonthsPerYear) }
    )

    val addMonths = function(
        command = { n: nat -> localDate.plusMonths(n.toLong()).toDate() },
        pre = { n -> (date.year * MonthsPerYear) + date.month + n <= (LastYear * MonthsPerYear) },
        // TODO -- doesn't hold
//        post = { n, result -> DateUtilities.monthsBetween(date, result) == n }
    )

    val subtractMonths = function(
        command = { n: nat -> localDate.minusMonths(n.toLong()).toDate() },
        pre = { n -> (date.year * MonthsPerYear) + date.month > n },
        // TODO -- doesn't hold
//        post = { n, result -> DateUtilities.monthsBetween(result, date) == n }
    )

    val addDays: (nat) -> Date = function(
        command = { n -> localDate.plusDays(n.toLong()).toDate() },
        post = { n, result -> DateUtilities.daysBetween(date, result) == n }
    )

    val subtractDays: (nat) -> Date = function(
        command = { n -> localDate.minusDays(n.toLong()).toDate() },
        post = { n, result -> DateUtilities.daysBetween(result, date) == n }
    )
}

@PrimitiveInvariant(name = "Year", base = nat::class)
fun yearInRange(year: nat) = year in FirstYear..LastYear

@PrimitiveInvariant(name = "Month", base = nat1::class)
fun monthInRange(month: nat1) = month in 1uL..MonthsPerYear

@PrimitiveInvariant(name = "Day", base = nat1::class)
fun dayInRange(day: nat1) = day in 1uL..DaysPerMonth.rng.max()

object DateUtilities {

    // TODO -- questionable whether this has meaning e.g. https://stackoverflow.com/questions/50890562/java-8-chronounit-months-betweenfromdate-todate-not-working-as-expected/50891079#50891079
    val monthsBetween = function(
        command = { d1: Date, d2: Date -> ChronoUnit.MONTHS.between(d1.toLocalDate(), d2.toLocalDate()).toNat() },
        pre = { d1, d2 -> d1 == d2 || d1.functions.isEarlierThan(d2) },
    )

    val daysBetween = function(
        command = { d1: Date, d2: Date -> ChronoUnit.DAYS.between(d1.toLocalDate(), d2.toLocalDate()).toNat() },
        pre = { d1, d2 -> d1 == d2 || d1.functions.isEarlierThan(d2) },
    )

    val durationBetween = function(
        command = { d1: Date, d2: Date -> DurationUtilities.fromDays(daysBetween(d1, d2)) },
        pre = { d1, d2 -> d1 == d2 || d1.functions.isEarlierThan(d2) },
    )

    val isLeap = function(
        command = { year: Year ->
            val multipleOfFour = year % 4u == 0uL
            val multipleOfOneHundred = year % 100u == 0uL
            val multipleOfFourHundred = year % 400u == 0uL
            multipleOfFour && (multipleOfOneHundred implies multipleOfFourHundred)
        }
    )

    val daysInYear = function(
        command = { year: Year -> seq(1uL..MonthsPerYear) { daysInMonth(year, it) }.sum() }
    )

    val daysInMonth: (Year, Month) -> Day = function(
        command = { year, month -> if (isLeap(year)) DaysPerMonthLeap[month] else DaysPerMonth[month] }
    )

    val earliest = function(
        command = { dates: Set1<Date> -> dates.minOf { it.toLocalDate() }.toDate() },
        post = { dates, result -> result in dates && forall(dates / result) { result.functions.isEarlierThan(it) } }
    )

    val latest = function(
        command = { dates: Set1<Date> -> dates.maxOf { it.toLocalDate() }.toDate() },
        post = { dates, result -> result in dates && forall(dates / result) { it.functions.isEarlierThan(result) } }
    )

    val nextDateWithSameDayAsGivenDate = function(
        command = { date: Date -> nextDateFromGivenYearMonthDayForGivenDay(date.year, date.month, date.day, date.day) }
    )

    val nextDateWithSameDayAsGivenDay = function(
        command = { date: Date, day: Day -> nextDateFromGivenYearMonthDayForGivenDay(date.year, date.month, date.day, day) },
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

internal fun LocalDate.toDate() = mk_Date(this.year.toNat(), this.monthValue.toNat1(), this.dayOfMonth.toNat1())

internal fun Date.toLocalDate() = LocalDate.of(this.year.toInt(), this.month.toInt(), this.day.toInt())