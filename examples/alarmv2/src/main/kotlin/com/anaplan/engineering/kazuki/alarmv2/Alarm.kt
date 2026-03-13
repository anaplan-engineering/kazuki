package com.anaplan.engineering.kazuki.alarmv2

import com.anaplan.engineering.kazuki.core.*

@Module
interface Alarm {
    val text: Sequence<Char>
    val qualification: Qualification
}

internal fun String.toAlarmText() = as_Seq(toList())

