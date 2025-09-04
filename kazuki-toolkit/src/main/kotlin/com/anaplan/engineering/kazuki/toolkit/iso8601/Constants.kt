package com.anaplan.engineering.kazuki.toolkit.iso8601

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.iso8601.Date_Module.mk_Date
import com.anaplan.engineering.kazuki.toolkit.iso8601.Dtg_Module.mk_Dtg
import com.anaplan.engineering.kazuki.toolkit.iso8601.Duration_Module.mk_Duration
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

const val NanosPerMilli = 1_000_000uL
const val MillisPerSecond = 1000uL
const val SecondsPerMinute = 60uL
const val MinutesPerHour = 60uL
const val HoursPerDay = 24uL

const val FirstYear = 0uL
const val LastYear = 9999uL

val DaysPerMonth by lazy {
    mk_Mapping(
        mk_(1uL, 31uL), mk_(2uL, 28uL), mk_(3uL, 31uL),
        mk_(4uL, 30uL), mk_(5uL, 31uL), mk_(6uL, 30uL),
        mk_(7uL, 31uL), mk_(8uL, 31uL), mk_(9uL, 30uL),
        mk_(10uL, 31uL), mk_(11uL, 30uL), mk_(12uL, 31uL)
    )
}

val DaysPerMonthLeap by lazy {
    DaysPerMonth * mk_(2uL, 29uL)
}

val MonthsPerYear by lazy { DaysPerMonth.card }

val FirstDate by lazy { mk_Date(FirstYear, 1uL, 1uL) }
val LastDate by lazy { mk_Date(LastYear, 12uL, 31uL) }

val FirstTime by lazy { mk_Time(0u, 0u, 0u, 0u) }
val LastTime by lazy { mk_Time(HoursPerDay - 1u, MinutesPerHour - 1u, SecondsPerMinute - 1u, MillisPerSecond - 1u) }

val FirstDtg by lazy { mk_Dtg(FirstDate, FirstTime) }
val LastDtg by lazy { mk_Dtg(LastDate, LastTime) }

val NoDuration by lazy { mk_Duration(0uL) }
val OneMillisecondDuration by lazy { mk_Duration(1uL) }
val OneSecondDuration by lazy { OneMillisecondDuration.functions.multiply(MillisPerSecond) }
val OneMinuteDuration by lazy { OneSecondDuration.functions.multiply(SecondsPerMinute) }
val OneHourDuration by lazy { OneMinuteDuration.functions.multiply(MinutesPerHour) }
val OneDayDuration by lazy { OneHourDuration.functions.multiply(HoursPerDay) }
