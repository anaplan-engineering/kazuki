package com.anaplan.engineering.kazuki.alarm

import com.anaplan.engineering.kazuki.alarm.Alarm.expertIsOnDuty
import com.anaplan.engineering.kazuki.alarm.Alarm.expertToPagePre
import com.anaplan.engineering.kazuki.alarm.Alarm.expertToPagePost
import com.anaplan.engineering.kazuki.alarm.Alarm_Module.mk_AlarmI
import com.anaplan.engineering.kazuki.alarm.Alarm_Module.mk_Expert
import com.anaplan.engineering.kazuki.alarm.Alarm_Module.mk_ExpertId
import com.anaplan.engineering.kazuki.alarm.Alarm_Module.mk_Period
import com.anaplan.engineering.kazuki.alarm.Alarm_Module.mk_Plant
import com.anaplan.engineering.kazuki.alarm.Alarm_Module.mk_Schedule
import com.anaplan.engineering.kazuki.core.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertDoesNotThrow


class TestAlarm {
    val period1 = mk_Period("Monday day")
    val period2 = mk_Period("Monday night")
    val period3 = mk_Period("Tuesday day")
    val period4 = mk_Period("Tuesday night")
    val period5 = mk_Period("Wednesday day")
    val periods = mk_Set(period1, period2, period3, period4, period5)

    val expertId1 = mk_ExpertId(134)
    val expertId2 = mk_ExpertId(145)
    val expertId3 = mk_ExpertId(154)
    val expertId4 = mk_ExpertId(165)
    val expertId5 = mk_ExpertId(169)
    val expertId6 = mk_ExpertId(174)
    val expertId7 = mk_ExpertId(181)
    val expertId8 = mk_ExpertId(190)

    val expert1 = mk_Expert(expertId1, mk_Set(Alarm.Qualification.Elec))
    val expert2 = mk_Expert(expertId2, mk_Set(Alarm.Qualification.Mech, Alarm.Qualification.Chem))
    val expert3 = mk_Expert(expertId3, mk_Set(Alarm.Qualification.Bio, Alarm.Qualification.Chem, Alarm.Qualification.Elec))
    val expert4 = mk_Expert(expertId4, mk_Set(Alarm.Qualification.Bio))
    val expert5 = mk_Expert(expertId5, mk_Set(Alarm.Qualification.Chem, Alarm.Qualification.Bio))
    val expert6 = mk_Expert(expertId6, mk_Set(Alarm.Qualification.Elec, Alarm.Qualification.Chem, Alarm.Qualification.Bio, Alarm.Qualification.Mech))
    val expert7 = mk_Expert(expertId7, mk_Set(Alarm.Qualification.Elec, Alarm.Qualification.Mech))
    val expert8 = mk_Expert(expertId8, mk_Set(Alarm.Qualification.Mech, Alarm.Qualification.Bio))
    val experts = mk_Set(expert1, expert2, expert3, expert4, expert5, expert6, expert7, expert8)

    val sch = mk_Schedule (
        mk_(period1, mk_Set(expert7, expert5, expert1)),
        mk_(period2, mk_Set(expert6)),
        mk_(period3, mk_Set(expert1, expert3, expert8)),
        mk_(period4, mk_Set(expert6)))

    val alarm1 = mk_AlarmI("Power supply missing", Alarm.Qualification.Elec)
    val alarm2 = mk_AlarmI("Tank overflow", Alarm.Qualification.Mech)
    val alarm3 = mk_AlarmI("CO2 detected", Alarm.Qualification.Chem)
    val alarm4 = mk_AlarmI("Biological attack", Alarm.Qualification.Bio)
    val alarms = mk_Set(alarm1, alarm2, alarm3, alarm4)

    val plant1 = mk_Plant(sch, mk_Set(alarm1, alarm2, alarm3))

    @TestFactory
    fun test1(): Collection<DynamicTest> = set(alarms, periods, experts, { a, p, _ -> expertToPagePre(a, p, plant1) }) { a, p, e ->
        dynamicTest("Conditions run for ${a.quali} alarm: '${a.alarmText}' period: ${p.period} for expert ${e.expertId.expertId} with qualifications ${e.quali}") {
            assertDoesNotThrow {
                expertToPagePost(a, p, plant1, e)
            }
        }
    }

    @TestFactory
    fun test2(): Collection<DynamicTest> = set(experts) {
        dynamicTest("Expert ${it.expertId.expertId} is on duty") {
            assertDoesNotThrow { expertIsOnDuty(expert1, plant1) }
        }
    }
}
