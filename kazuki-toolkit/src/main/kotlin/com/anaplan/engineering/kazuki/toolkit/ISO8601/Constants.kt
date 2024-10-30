package com.anaplan.engineering.kazuki.toolkit.ISO8601

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Date_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.ISO8601.Time_Module.mk_Time

enum class PlusOrMinus {
    Plus,
    Minus,
    None
}

enum class DayOfWeek(val dayNumber: int) {
    Monday(1),
    Tuesday(2),
    Wednesday(3),
    Thursday(4),
    Friday(5),
    Saturday(6),
    Sunday(0)
}

const val MillisPerSecond: nat = 1000
const val SecondsPerMinute: nat = 60
const val MinutesPerHour: nat = 60
const val HoursPerDay: nat = 24

val DaysPerMonth: Mapping<Month, Day> = mk_Mapping(
    mk_(1, 31), mk_(2, 28), mk_(3, 31),
    mk_(4, 30), mk_(5, 31), mk_(6, 30),
    mk_(7, 31), mk_(8, 31), mk_(9, 30),
    mk_(10, 31), mk_(11, 30), mk_(12, 31)
)
val DaysPerMonthLeap: Mapping<Month, Day> = DaysPerMonth * mk_(2, 29)

val MonthsPerYear: nat = DaysPerMonth.dom.card
val MaxDaysPerMonth: nat = DaysPerMonth.rng.max()

const val FirstYear: nat = 0
const val LastYear: nat = 9999

val FirstDate: Date = mk_Date(FirstYear, 1, 1)

val LastDate: Date = mk_Date(LastYear, 12, 31)

val FirstTime: Time = mk_Time(0, 0, 0, 0)

val NoDuration: Duration = Duration.fromMillis(0)

val OneMillisecondDuration: Duration = Duration.fromMillis(1)

val OneSecondDuration: Duration = Duration.fromSeconds(1)

val OneMinuteDuration: Duration = Duration.fromMinutes(1)

val OneHourDuration: Duration = Duration.fromHours(1)

val OneDayDuration: Duration = Duration.fromDays(1)
