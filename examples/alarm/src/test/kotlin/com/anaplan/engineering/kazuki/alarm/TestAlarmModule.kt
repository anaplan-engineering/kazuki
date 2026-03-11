package com.anaplan.engineering.kazuki.alarm

import com.anaplan.engineering.kazuki.alarm.AlarmModule.periodsExpertIsOnDuty
import com.anaplan.engineering.kazuki.alarm.AlarmModule.expertToPage
import com.anaplan.engineering.kazuki.alarm.AlarmModule_Module.mk_Alarm
import com.anaplan.engineering.kazuki.alarm.AlarmModule_Module.mk_Expert
import com.anaplan.engineering.kazuki.alarm.AlarmModule_Module.mk_Plant
import com.anaplan.engineering.kazuki.alarm.AlarmModule_Module.mk_Schedule
import com.anaplan.engineering.kazuki.core.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertDoesNotThrow


class TestAlarmModule {
    val period1: Period = "Monday day"
    val period2: Period = "Monday night"
    val period3: Period = "Tuesday day"
    val period4: Period = "Tuesday night"
    val period5: Period = "Wednesday day"
    val periods = mk_Set(period1, period2, period3, period4, period5)

    val expertId1: ExpertId = 134
    val expertId2: ExpertId = 145
    val expertId3: ExpertId = 154
    val expertId4: ExpertId = 165
    val expertId5: ExpertId = 169
    val expertId6: ExpertId = 174
    val expertId7: ExpertId = 181
    val expertId8: ExpertId = 190

    val expert1 = mk_Expert(expertId1, mk_Set1(AlarmModule.Qualification.Elec))
    val expert2 = mk_Expert(expertId2, mk_Set1(AlarmModule.Qualification.Mech, AlarmModule.Qualification.Chem))
    val expert3 = mk_Expert(expertId3, mk_Set1(AlarmModule.Qualification.Bio, AlarmModule.Qualification.Chem, AlarmModule.Qualification.Elec))
    val expert4 = mk_Expert(expertId4, mk_Set1(AlarmModule.Qualification.Bio))
    val expert5 = mk_Expert(expertId5, mk_Set1(AlarmModule.Qualification.Chem, AlarmModule.Qualification.Bio))
    val expert6 = mk_Expert(expertId6, mk_Set1(AlarmModule.Qualification.Elec, AlarmModule.Qualification.Chem, AlarmModule.Qualification.Bio, AlarmModule.Qualification.Mech))
    val expert7 = mk_Expert(expertId7, mk_Set1(AlarmModule.Qualification.Elec, AlarmModule.Qualification.Mech))
    val expert8 = mk_Expert(expertId8, mk_Set1(AlarmModule.Qualification.Mech, AlarmModule.Qualification.Bio))
    val experts = mk_Set(expert1, expert2, expert3, expert4, expert5, expert6, expert7, expert8)

    val sch = mk_Schedule (
        mk_(period1, mk_Set1(expert7, expert5, expert1)),
        mk_(period2, mk_Set1(expert6)),
        mk_(period3, mk_Set1(expert1, expert3, expert8)),
        mk_(period4, mk_Set1(expert6)))

    val alarm1 = mk_Alarm("Power supply missing", AlarmModule.Qualification.Elec)
    val alarm2 = mk_Alarm("Tank overflow", AlarmModule.Qualification.Mech)
    val alarm3 = mk_Alarm("CO2 detected", AlarmModule.Qualification.Chem)
    val alarm4 = mk_Alarm("Biological attack", AlarmModule.Qualification.Bio)
    val alarms = mk_Set(alarm1, alarm2, alarm3, alarm4)

    val plant1 = mk_Plant(sch, mk_Set(alarm1, alarm2, alarm3))

    // Tests amended from VDM-SL as it was unclear what they were meant to be testing

    @TestFactory
    fun test1(): Collection<DynamicTest> = set(alarms, periods, experts,
        { a, p, _ -> expertToPage.pre(a, p, plant1) }) { a, p, e ->
        dynamicTest("Conditions run for alarm: '${a.text}' period: $p for expert ${e.expertId}") {
            assertDoesNotThrow {
                expertToPage.post(a, p, plant1, e)
            }
        }
    }

    @TestFactory
    fun test2(): Collection<DynamicTest> = set(experts) {
        dynamicTest("Expert ${it.expertId} is on duty") {
            assertDoesNotThrow { periodsExpertIsOnDuty(it, plant1) }
        }
    }
}
