package com.anaplan.engineering.kazuki.toolkit.iso8601

import com.anaplan.engineering.kazuki.core.bool
import com.anaplan.engineering.kazuki.core.function
import com.anaplan.engineering.kazuki.core.implies
import com.anaplan.engineering.kazuki.core.nat
import com.anaplan.engineering.kazuki.core.nat1
import com.anaplan.engineering.kazuki.core.toNat
import com.anaplan.engineering.kazuki.core.toNat1
import com.anaplan.engineering.kazuki.toolkit.iso8601.DateUtilities.daysInMonth
import com.anaplan.engineering.kazuki.toolkit.iso8601.Date_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.iso8601.Dtg_Module.mk_Dtg
import com.anaplan.engineering.kazuki.toolkit.iso8601.Time_Module.mk_Time

object DateFormattingUtilities {
    
    private fun String.toNat1() = toInt().toNat1()
    private fun String.toNat() = toInt().toNat()
    private fun String.toNat1OrNull(): nat1? {
        val int = toIntOrNull()
        return if (int != null && int > 0) {
            int.toNat1()
        } else {
            null
        }
    }
    private fun String.toNatOrNull(): nat? {
        val int = toIntOrNull()
        return if (int != null && int >= 0) {
            int.toNat()
        } else {
            null
        }
    }

    val stringToDate: (String) -> Date = function(
        command = { string ->
            val year = string.substring(0, 4).toNat()
            val month = string.substring(5, 7).toNat1()
            val day = string.substring(8, 10).toNat1()

            mk_Date(year, month, day)
        }, pre = { string -> isStringIsoDate(string) })

    val stringToDtg: (String) -> Dtg = function(
        command = { string ->
            val year = string.substring(0, 4).toNat()
            val month = string.substring(5, 7).toNat1()
            val day = string.substring(8, 10).toNat1()

            val hour = string.substring(11, 13).toNat1()
            val minute = string.substring(14, 16).toNat1()
            val second = string.substring(17, 19).toNat()
            val millisecond = if (string.length == 19) 0u else string.substring(20, 23).toNat()

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
            val year = string.substring(0, 4).toNatOrNull()
            val month = string.substring(5, 7).toNat1OrNull()
            val day = string.substring(8, 10).toNat1OrNull()

            val isoDateNumbersNonNull = (year != null && month != null && day != null)

            val isoDateNumbersValid by lazy {
                val yearValid = year in FirstYear..LastYear
                val monthValid = month in 1uL..MonthsPerYear
                val dayValid by lazy { day in 1uL..daysInMonth(year!!, month!!) }
                yearValid && monthValid && dayValid
            }

            isoDateNumbersNonNull && isoDateNumbersValid
        })

    private val isoDtgTimeNumbersValid: (String) -> bool = function(
        command = { string ->

            val dateValid = isoDateNumbersValid(string)

            val hour = string.substring(11, 13).toNatOrNull()
            val minute = string.substring(14, 16).toNatOrNull()
            val second = string.substring(17, 19).toNatOrNull()
            val isoTimeNonNull = hour != null && minute != null && second != null

            val timeValid by lazy {
                val hourValid = hour in 0uL until HoursPerDay
                val minuteValid = minute in 0uL until MinutesPerHour
                val secondValid = second in 0uL until SecondsPerMinute
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
            val millisecond = string.substring(20, 23).toNatOrNull()
            millisecond != null && millisecond in 0uL until MillisPerSecond
        }
    )
}