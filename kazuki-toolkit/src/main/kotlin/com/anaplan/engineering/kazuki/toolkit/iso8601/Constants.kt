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

val DaysPerMonth by property {
    mk_Mapping(
        mk_(1uL, 31uL), mk_(2uL, 28uL), mk_(3uL, 31uL),
        mk_(4uL, 30uL), mk_(5uL, 31uL), mk_(6uL, 30uL),
        mk_(7uL, 31uL), mk_(8uL, 31uL), mk_(9uL, 30uL),
        mk_(10uL, 31uL), mk_(11uL, 30uL), mk_(12uL, 31uL)
    )
}

val DaysPerMonthLeap by property {
    DaysPerMonth * mk_(2uL, 29uL)
}

val MonthsPerYear by property { DaysPerMonth.card }

val FirstDate by property { mk_Date(FirstYear, 1uL, 1uL) }
val LastDate by property { mk_Date(LastYear, 12uL, 31uL) }

val FirstTime by property { mk_Time(0u, 0u, 0u, 0u) }
val LastTime by property { mk_Time(HoursPerDay - 1u, MinutesPerHour - 1u, SecondsPerMinute - 1u, MillisPerSecond - 1u) }

val FirstDtg by property { mk_Dtg(FirstDate, FirstTime) }
val LastDtg by property { mk_Dtg(LastDate, LastTime) }

val NoDuration by property { mk_Duration(0uL) }
val OneMillisecondDuration by property { mk_Duration(1uL) }
val OneSecondDuration by property { OneMillisecondDuration.functions.multiply(MillisPerSecond) }
val OneMinuteDuration by property { OneSecondDuration.functions.multiply(SecondsPerMinute) }
val OneHourDuration by property { OneMinuteDuration.functions.multiply(MinutesPerHour) }
val OneDayDuration by property { OneHourDuration.functions.multiply(HoursPerDay) }
