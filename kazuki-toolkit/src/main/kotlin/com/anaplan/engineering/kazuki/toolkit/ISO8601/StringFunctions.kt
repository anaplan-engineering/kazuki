package com.anaplan.engineering.kazuki.toolkit.ISO8601

import com.anaplan.engineering.kazuki.core.bool
import com.anaplan.engineering.kazuki.core.function
import com.anaplan.engineering.kazuki.core.implies
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Date_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Dtg_Module.mk_Dtg
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Time_Module.mk_Time

val stringToDate: (String) -> Date = function(
    command = { string ->
        val year = string.substring(0, 4).toInt()
        val month = string.substring(5, 7).toInt()
        val day = string.substring(8, 10).toInt()

        mk_Date(year, month, day)
    }, pre = { string -> isStringIsoDate(string) })

val stringToDtg: (String) -> Dtg = function(
    command = { string ->
        val year = string.substring(0, 4).toInt()
        val month = string.substring(5, 7).toInt()
        val day = string.substring(8, 10).toInt()

        val hour = string.substring(11, 13).toInt()
        val minute = string.substring(14, 16).toInt()
        val second = string.substring(17, 19).toInt()
        val millisecond = if (string.length == 19) 0 else string.substring(20, 23).toInt()

        mk_Dtg(mk_Date(year, month, day), mk_Time(hour, minute, second, millisecond))
    }, pre = { string -> isStringIsoDtg(string) })

val isStringIsoDate: (String) -> bool = function(
    command = { string ->
        isoDateFormattedCorrectly(string) && isoDateNumbersValid(string)
    }
)

val isStringIsoDtg: (String) -> bool = function(
    command = { string ->
        isoDtgFormattedCorrectly(string) && isoDtgTimeNumbersValid(string) &&
                (isoDtgContainsMilliseconds(string)) implies {
            isoDtgMillisecondFormattedCorrectly(string) && isoMillisecondValid(string)
        }
    })

private val isoDateFormattedCorrectly: (String) -> bool = function(
    command = { string ->
        val correctIsoLength = string.length == 10
        val isoSeparatorsPresent by lazy { string.elementAt(4) == '-' && string.elementAt(7) == '-' }
        correctIsoLength && isoSeparatorsPresent
    })

private val isoDtgFormattedCorrectly: (String) -> bool = function(
    command = { string ->
        val correctIsoLength = string.length == 19 || string.length == 23
        val isoSeparatorsPresent by lazy {
            string.elementAt(4) == '-' && string.elementAt(7) == '-' && string.elementAt(10) == 'T' &&
                    string.elementAt(13) == ':' && string.elementAt(16) == ':'
        }
        correctIsoLength && isoSeparatorsPresent
    })

private val isoDateNumbersValid: (String) -> bool = function(
    command = { string ->

        val year = string.substring(0, 4).toIntOrNull()
        val month = string.substring(5, 7).toIntOrNull()
        val day = string.substring(8, 10).toIntOrNull()

        val isoDateNumbersNonNull = (year != null && month != null && day != null)

        val isoDateNumbersValid by lazy {
            val yearValid = year in FirstYear..LastYear
            val monthValid = month in 1..MonthsPerYear
            val dayValid by lazy { day in 1..daysInMonth(year!!, month!!) }
            yearValid && monthValid && dayValid
        }

        isoDateNumbersNonNull && isoDateNumbersValid
    })

private val isoDtgTimeNumbersValid: (String) -> bool = function(
    command = { string ->

        val dateValid = isoDateNumbersValid(string)

        val hour = string.substring(11, 13).toIntOrNull()
        val minute = string.substring(14, 16).toIntOrNull()
        val second = string.substring(17, 19).toIntOrNull()
        val isoTimeNonNull = hour != null && minute != null && second != null

        val timeValid by lazy {
            val hourValid = hour in 0 until HoursPerDay
            val minuteValid = minute in 0 until MinutesPerHour
            val secondValid = second in 0 until SecondsPerMinute
            isoTimeNonNull && hourValid && minuteValid && secondValid
        }

        dateValid && timeValid
    },
)

private val isoDtgContainsMilliseconds: (String) -> bool = function(
    command = { string -> string.length == 23 }
)

private val isoDtgMillisecondFormattedCorrectly: (String) -> bool = function(
    command = { string -> string.elementAt(19) == '.' }
)

private val isoMillisecondValid: (String) -> bool = function(
    command = { string ->
        val millisecond = string.substring(20, 23).toIntOrNull()
        millisecond != null && millisecond in 0 until MillisPerSecond
    }
)
