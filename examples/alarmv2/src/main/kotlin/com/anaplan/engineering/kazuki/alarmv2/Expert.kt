package com.anaplan.engineering.kazuki.alarmv2

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.integer

typealias ExpertId = integer

@Module
interface Expert {
    val expertId: ExpertId
    val qualifications: Set1<Qualification>
}