package com.anaplan.engineering.kazuki.toolkit.ISO8601

import com.anaplan.engineering.kazuki.core.bool
import com.anaplan.engineering.kazuki.core.function
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
    pre = { string -> isDate(string) }
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
    pre = { string -> isDtg(string) }
)

val isDate: (String) -> bool = function(
    command = { string ->
        !(dateFormattedWrong(string) || dateNumberNull(string) || dateOutOfRange(string))
    }
)

val isDtg: (String) -> bool = function(
    command = { string ->
        if (dtgFormattedWrong(string) || dtgNumberNull(string) || dateOutOfRange(string) || timeOutOfRange(string)) {
            false
        } else if (string.length == 23) { // So if it contains a millisecond value
            !(millisecondFormattedWrong(string) || millisecondNumberNull(string) || millisecondOutOfRange(string))
        } else {
            true
        }
    }
)

private val dateFormattedWrong: (String) -> bool = function(
    command = { string -> string.length != 10 || string.elementAt(4) != '-' || string.elementAt(7) != '-' }
)
private val dtgFormattedWrong: (String) -> bool = function(
    command = { string ->
        if (string.length != 19 && string.length != 23) {
            true
        } else if (string.elementAt(4) != '-' || string.elementAt(7) != '-' ||
            string.elementAt(10) != 'T' || string.elementAt(13) != ':' || string.elementAt(16) != ':'
        ) {
            true
        } else {
            false
        }
    }
)
private val millisecondFormattedWrong: (String) -> bool = function(
    command = { string -> string.elementAt(19) != '.' }
)
private val dateNumberNull: (String) -> bool = function(
    command = { string ->
        string.substring(0, 4).toIntOrNull() == null ||
                string.substring(5, 7).toIntOrNull() == null ||
                string.substring(8, 10).toIntOrNull() == null
    }
)
private val dtgNumberNull: (String) -> bool = function(
    command = { string ->
        string.substring(0, 4).toIntOrNull() == null || string.substring(5, 7).toIntOrNull() == null ||
                string.substring(8, 10).toIntOrNull() == null || string.substring(11, 13).toIntOrNull() == null ||
                string.substring(14, 16).toIntOrNull() == null || string.substring(17, 19).toIntOrNull() == null
    }
)
private val millisecondNumberNull: (String) -> bool = function(
    command = { string -> string.substring(20, 23).toIntOrNull() == null }
)
private val dateOutOfRange: (String) -> bool = function(
    command = { string ->
        val year = string.substring(0, 4).toInt()
        val month = string.substring(5, 7).toInt()
        val day = string.substring(8, 10).toInt()
        if (year < FirstYear || year > LastYear) {
            true
        } else if (month < 1 || month > MonthsPerYear) {
            true
        } else if (day < 1 || day > daysInMonth(year, month)) {
            true
        } else {
            false
        }
    }
)
private val timeOutOfRange: (String) -> bool = function(
    command = { string ->
        val hour = string.substring(11, 13).toInt()
        val minute = string.substring(14, 16).toInt()
        val second = string.substring(17, 19).toInt()
        if (hour < 0 || hour > HoursPerDay) {
            true
        } else if (minute < 0 || minute > MinutesPerHour) {
            true
        } else if (second < 0 || second > SecondsPerMinute) {
            true
        } else {
            false
        }
    }
)
private val millisecondOutOfRange: (String) -> bool = function(
    command = { string ->
        val millisecond = string.substring(20, 23).toInt()
        millisecond < 0 || millisecond > MillisPerSecond
    }
)
