package com.anaplan.engineering.kazuki.alarmv2

import com.anaplan.engineering.kazuki.alarmv2.Expert_Module.mk_Expert
import com.anaplan.engineering.kazuki.alarmv2.Plant_Module.mk_Plant
import com.anaplan.engineering.kazuki.alarmv2.Schedule_Module.mk_Schedule
import com.anaplan.engineering.kazuki.alarmv2.Alarm_Module.mk_Alarm
import com.anaplan.engineering.kazuki.core.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertDoesNotThrow


class TestAlarm {
    val period1: Period = "Monday day".toPeriod()
    val period2: Period = "Monday night".toPeriod()
    val period3: Period = "Tuesday day".toPeriod()
    val period4: Period = "Tuesday night".toPeriod()
    val period5: Period = "Wednesday day".toPeriod()
    val periods = mk_Set(period1, period2, period3, period4, period5)

    val expertId1: ExpertId = 134uL
    val expertId2: ExpertId = 145uL
    val expertId3: ExpertId = 154uL
    val expertId4: ExpertId = 165uL
    val expertId5: ExpertId = 169uL
    val expertId6: ExpertId = 174uL
    val expertId7: ExpertId = 181uL
    val expertId8: ExpertId = 190uL

    val expert1 = mk_Expert(expertId1, mk_Set1(Qualification.Elec))
    val expert2 = mk_Expert(expertId2, mk_Set1(Qualification.Mech, Qualification.Chem))
    val expert3 = mk_Expert(expertId3, mk_Set1(Qualification.Bio, Qualification.Chem, Qualification.Elec))
    val expert4 = mk_Expert(expertId4, mk_Set1(Qualification.Bio))
    val expert5 = mk_Expert(expertId5, mk_Set1(Qualification.Chem, Qualification.Bio))
    val expert6 = mk_Expert(
        expertId6,
        mk_Set1(Qualification.Elec, Qualification.Chem, Qualification.Bio, Qualification.Mech)
    )
    val expert7 = mk_Expert(expertId7, mk_Set1(Qualification.Elec, Qualification.Mech))
    val expert8 = mk_Expert(expertId8, mk_Set1(Qualification.Mech, Qualification.Bio))
    val experts = mk_Set(expert1, expert2, expert3, expert4, expert5, expert6, expert7, expert8)

    val sch = mk_Schedule (
        mk_(period1, mk_Set1(expert7, expert5, expert1)),
        mk_(period2, mk_Set1(expert6)),
        mk_(period3, mk_Set1(expert1, expert3, expert8)),
        mk_(period4, mk_Set1(expert6)))

    val alarm1 = mk_Alarm("Power supply missing".toAlarmText(), Qualification.Elec)
    val alarm2 = mk_Alarm("Tank overflow".toAlarmText(), Qualification.Mech)
    val alarm3 = mk_Alarm("CO2 detected".toAlarmText(), Qualification.Chem)
    val alarm4 = mk_Alarm("Biological attack".toAlarmText(), Qualification.Bio)
    val alarms = mk_Set(alarm1, alarm2, alarm3, alarm4)

    val plant1 = mk_Plant(sch, mk_Set(alarm1, alarm2, alarm3))

    @TestFactory
    fun test1(): Collection<DynamicTest> = set(alarms, periods, experts,
        { a, p, _ -> plant1.functions.expertToPage.pre(a, p) }) { a, p, e ->
        dynamicTest("Conditions run for alarm: '${a.text.joinToString("")}' period: ${p.joinToString("")} for expert ${e.expertId}") {
            assertDoesNotThrow {
                plant1.functions.numberOfExperts(p)
                plant1.functions.expertToPage.post(a, p, e)
            }
        }
    }

    @TestFactory
    fun test2(): Collection<DynamicTest> = set(experts) {
        dynamicTest("Expert ${it.expertId} is on duty") {
            assertDoesNotThrow { plant1.functions.periodsExpertIsOnDuty(it) }
        }
    }
}
