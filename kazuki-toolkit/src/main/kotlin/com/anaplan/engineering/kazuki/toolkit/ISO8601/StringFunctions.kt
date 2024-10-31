package com.anaplan.engineering.kazuki.toolkit.ISO8601

import com.anaplan.engineering.kazuki.core.bool
import com.anaplan.engineering.kazuki.core.function
import com.anaplan.engineering.kazuki.core.implies
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Date_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Dtg_Module.mk_Dtg
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Time_Module.mk_Time

val strToDate: (String) -> Date = function(
    command = { string ->
        val year = string.substring(0, 4).toInt()
        val month = string.substring(5, 7).toInt()
        val day = string.substring(8, 10).toInt()

        mk_Date(year, month, day)
    },
    pre = { string -> isStringIsoDate(string) }
)

val strToDtg: (String) -> Dtg = function(
    command = { string ->
        val year = string.substring(0, 4).toInt()
        val month = string.substring(5, 7).toInt()
        val day = string.substring(8, 10).toInt()

        val hour = string.substring(11, 13).toInt()
        val minute = string.substring(14, 16).toInt()
        val second = string.substring(17, 19).toInt()
        val millisecond = if (string.length == 19) 0 else string.substring(20, 23).toInt()

        mk_Dtg(mk_Date(year, month, day), mk_Time(hour, minute, second, millisecond))
    },
    pre = { string -> isStringIsoDtg(string) }
)

val isStringIsoDate: (String) -> bool = function(
    command = { string ->
        isoDateFormattedCorrectly(string) && isoDateNumbersNonNull(string) && isoDateNumbersValid(string)
    }
)

val isStringIsoDtg: (String) -> bool = function(
    command = { string ->
        isoDtgFormattedCorrectly(string) && isoDtgNumbersNonNull(string) && isoDtgTimeNumbersValid(string) &&
                (isoDtgContainsMilliseconds(string)) implies {
            isoDtgMillisecondFormattedCorrectly(string) &&
                    isDtgMillisecondNumbersNotNull(string) &&
                    isoMillisecondValid(string)
        }
    }
)

private val isoDateFormattedCorrectly: (String) -> bool = function(
    command = { string ->
        val correctIsoLength = string.length == 10
        val isoSeparatorsPresent = if (correctIsoLength) {
            string.elementAt(4) == '-' && string.elementAt(7) == '-'
        } else false
        correctIsoLength && isoSeparatorsPresent
    }
)
private val isoDtgFormattedCorrectly: (String) -> bool = function(
    command = { string ->
        val correctIsoLength = string.length == 19 || string.length == 23
        val isoSeparatorsPresent = if (correctIsoLength) {
            string.elementAt(4) == '-' && string.elementAt(7) == '-' &&
                    string.elementAt(10) == 'T' && string.elementAt(13) == ':' && string.elementAt(16) == ':'
        } else false
        correctIsoLength && isoSeparatorsPresent
    }
)
private val isoDateNumbersNonNull: (String) -> bool = function(
    command = { string ->
        string.substring(0, 4).toIntOrNull() != null &&
                string.substring(5, 7).toIntOrNull() != null &&
                string.substring(8, 10).toIntOrNull() != null
    }
)
private val isoDtgNumbersNonNull: (String) -> bool = function(
    command = { string ->
        isoDateNumbersNonNull(string) && string.substring(11, 13).toIntOrNull() != null &&
                string.substring(14, 16).toIntOrNull() != null && string.substring(17, 19).toIntOrNull() != null
    }
)
private val isoDateNumbersValid: (String) -> bool = function(
    command = { string ->

        val year = string.substring(0, 4).toInt()
        val month = string.substring(5, 7).toInt()
        val day = string.substring(8, 10).toInt()

        val yearValid = year in FirstYear..LastYear
        val monthValid = month in 1..MonthsPerYear
        val dayValid = (yearValid && monthValid) implies { day in 1..daysInMonth(year, month) }

        yearValid && monthValid && dayValid
    }
)
private val isoDtgTimeNumbersValid: (String) -> bool = function(
    command = { string ->

        val hour = string.substring(11, 13).toInt()
        val hourValid = hour in 0 until HoursPerDay
        val minute = string.substring(14, 16).toInt()
        val minuteValid = minute in 0 until MinutesPerHour
        val second = string.substring(17, 19).toInt()
        val secondValid = second in 0 until SecondsPerMinute

        isoDateNumbersValid(string) && hourValid && minuteValid && secondValid
    },
)
private val isoDtgContainsMilliseconds: (String) -> bool = function(
    command = { string -> string.length == 23 }
)
private val isoDtgMillisecondFormattedCorrectly: (String) -> bool = function(
    command = { string -> string.elementAt(19) == '.' }
)
private val isDtgMillisecondNumbersNotNull: (String) -> bool = function(
    command = { string -> string.substring(20, 23).toIntOrNull() != null }
)
private val isoMillisecondValid: (String) -> bool = function(
    command = { string ->
        val millisecond = string.substring(20, 23).toInt()
        millisecond in 0 until MillisPerSecond
    }
)
