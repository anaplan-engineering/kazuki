package com.anaplan.engineering.kazuki.toolkit.iso8601

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.iso8601.Date_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.iso8601.Dtg_Module.mk_Dtg
import com.anaplan.engineering.kazuki.toolkit.iso8601.Time_Module.mk_Time

enum class PlusOrMinus {
    Plus,
    Minus,
    None
}

typealias OffsetDirection = PlusOrMinus


enum class DayOfWeek { // Date.functions.toDayOfWeek depends on this ordering - don't change it!
    Sunday,
    Monday,
    Tuesday,
    Wednesday,
    Thursday,
    Friday,
    Saturday,
}

const val MillisPerSecond: nat = 1000u
const val SecondsPerMinute: nat = 60u
const val MinutesPerHour: nat = 60u
const val HoursPerDay: nat = 24u

val DaysPerMonth: Mapping<Month, Day> = mk_Mapping(
    mk_(1u, 31u), mk_(2u, 28u), mk_(3u, 31u),
    mk_(4u, 30u), mk_(5u, 31u), mk_(6u, 30u),
    mk_(7u, 31u), mk_(8u, 31u), mk_(9u, 30u),
    mk_(10u, 31u), mk_(11u, 30u), mk_(12u, 31u)
)
val DaysPerMonthLeap: Mapping<Month, Day> = DaysPerMonth * mk_(2u, 29u)

val MonthsPerYear: nat = DaysPerMonth.dom.card

const val FirstYear: nat = 0u
const val LastYear: nat = 9999u

val FirstDate: Date = mk_Date(FirstYear, 1u, 1u)
val LastDate: Date = mk_Date(LastYear, 12u, 31u)

val FirstTime: Time = mk_Time(0u, 0u, 0u, 0u)
val LastTime: Time = mk_Time(HoursPerDay - 1u, MinutesPerHour - 1u, SecondsPerMinute - 1u, MillisPerSecond - 1u)

val FirstDtg: Dtg = mk_Dtg(FirstDate, FirstTime)
val LastDtg: Dtg = mk_Dtg(LastDate, LastTime)

val NoDuration: Duration = Duration.fromMillis(0u)
val OneMillisecondDuration: Duration = Duration.fromMillis(1u)
val OneSecondDuration: Duration = Duration.fromSeconds(1u)
val OneMinuteDuration: Duration = Duration.fromMinutes(1u)
val OneHourDuration: Duration = Duration.fromHours(1u)
val OneDayDuration: Duration = Duration.fromDays(1u)
