package com.anaplan.engineering.kazuki.alarm

import com.anaplan.engineering.kazuki.alarm.Alarm.expertIsOnDuty
import com.anaplan.engineering.kazuki.alarm.Alarm_Module.mk_AlarmI
import com.anaplan.engineering.kazuki.alarm.Alarm_Module.mk_Expert
import com.anaplan.engineering.kazuki.alarm.Alarm_Module.mk_ExpertId
import com.anaplan.engineering.kazuki.alarm.Alarm_Module.mk_Period
import com.anaplan.engineering.kazuki.alarm.Alarm_Module.mk_Plant
import com.anaplan.engineering.kazuki.core.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory


class TestAlarm {
    val period1: Alarm.Period = mk_Period("Monday day")
    val period2: Alarm.Period = mk_Period("Monday night")
    val period3: Alarm.Period = mk_Period("Tuesday day")
    val period4: Alarm.Period = mk_Period("Tuesday night")
    val period5: Alarm.Period = mk_Period("Wednesday day")
    val periods: Set<Alarm.Period> = mk_Set(period1, period2, period3, period4, period5)

    val expertId1: Alarm.ExpertId = mk_ExpertId(134)
    val expertId2: Alarm.ExpertId = mk_ExpertId(145)
    val expertId3: Alarm.ExpertId = mk_ExpertId(154)
    val expertId4: Alarm.ExpertId = mk_ExpertId(165)
    val expertId5: Alarm.ExpertId = mk_ExpertId(169)
    val expertId6: Alarm.ExpertId = mk_ExpertId(174)
    val expertId7: Alarm.ExpertId = mk_ExpertId(181)
    val expertId8: Alarm.ExpertId = mk_ExpertId(190)

    val expert1: Alarm.Expert = mk_Expert(expertId1, mk_Set(Alarm.Qualification.Elec))
    val expert2: Alarm.Expert = mk_Expert(expertId2, mk_Set(Alarm.Qualification.Mech, Alarm.Qualification.Chem))
    val expert3: Alarm.Expert = mk_Expert(expertId3, mk_Set(Alarm.Qualification.Bio, Alarm.Qualification.Chem, Alarm.Qualification.Elec))
    val expert4: Alarm.Expert = mk_Expert(expertId4, mk_Set(Alarm.Qualification.Bio))
    val expert5: Alarm.Expert = mk_Expert(expertId5, mk_Set(Alarm.Qualification.Chem, Alarm.Qualification.Bio))
    val expert6: Alarm.Expert = mk_Expert(expertId6, mk_Set(Alarm.Qualification.Elec, Alarm.Qualification.Chem, Alarm.Qualification.Bio, Alarm.Qualification.Mech))
    val expert7: Alarm.Expert = mk_Expert(expertId7, mk_Set(Alarm.Qualification.Elec, Alarm.Qualification.Mech))
    val expert8: Alarm.Expert = mk_Expert(expertId8, mk_Set(Alarm.Qualification.Mech, Alarm.Qualification.Bio))
    val experts = mk_Set(expert1, expert2, expert3, expert4, expert5, expert6, expert7, expert8)

    val sch : Mapping1<Alarm.Period, Set<Alarm.Expert>> = mk_Mapping1 (
        mk_(period1, mk_Set(expert7, expert5, expert1)),
        mk_(period2, mk_Set(expert6)),
        mk_(period3, mk_Set(expert1, expert3, expert8)),
        mk_(period4, mk_Set(expert6)))

    val alarm1: Alarm.AlarmI = mk_AlarmI("Power supply missing", Alarm.Qualification.Elec)
    val alarm2: Alarm.AlarmI = mk_AlarmI("Tank overflow", Alarm.Qualification.Mech)
    val alarm3: Alarm.AlarmI = mk_AlarmI("CO2 detected", Alarm.Qualification.Chem)
    val alarm4: Alarm.AlarmI = mk_AlarmI("Biological attack", Alarm.Qualification.Bio)
    val alarms: Set<Alarm.AlarmI> = mk_Set(alarm1, alarm2, alarm3, alarm4)

    val plant1: Alarm.Plant = mk_Plant(sch as Alarm.Schedule, mk_Set(alarm1, alarm2, alarm3))

    val run: (Alarm.Expert) -> Set<Alarm.Period> = { expert: Alarm.Expert ->
        expertIsOnDuty(expert, plant1)
    }

    @TestFactory
    fun test2(): Collection<DynamicTest> = set(experts) {
        dynamicTest("Expert ${it.expertId} is on duty") {
            expertIsOnDuty(it, plant1)
        }
    }

}
