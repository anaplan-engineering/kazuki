package com.anaplan.engineering.kazuki.alarm

import com.anaplan.engineering.kazuki.core.*

typealias Period = String
typealias ExpertId = integer

@Module
object AlarmModule {

    // Overall plant contains a schedule relating each period to the set of
    // experts on duty in the period; the alarms component records the alarms
    // which can arise

    interface Plant {
        val schedule: Schedule
        val alarms: Set<Alarm>

        @Invariant
        fun expertForAlarmAlwaysAvailable() =
            forall(alarms) { alarm ->
                forall(schedule.dom) { period ->
                    qualificationOK(schedule[period], alarm.qualification)
                }
            }
    }

    interface Schedule : Mapping<Period, Set1<Expert>> {
        @Invariant
        fun expertIdentifiersUniqueInSet() =
            forall(rng) { exs ->
                forall(exs, exs) {  ex1, ex2 ->
                    (ex1 != ex2).implies(ex1.expertId != ex2.expertId)
                }
            }
    }

    interface Expert {
        val expertId: ExpertId
        val qualifications: Set1<Qualification>

    }

    enum class Qualification {
        Elec,
        Mech,
        Bio,
        Chem
    }

    interface Alarm {
        val text: String
        val qualification: Qualification
    }

    val numberOfExperts = function(
        command = { period: Period, plant: Plant ->
            plant.schedule[period].card
        },
        pre = {period, plant ->
            period in plant.schedule.dom
        }
    )

    val periodsExpertIsOnDuty = function(
        command = { expert: Expert, plant: Plant ->
            set(plant.schedule.dom, { period -> expert in plant.schedule[period] }) { it }
        }
    )

    val expertToPage = function(
        command = { alarm: Alarm, period: Period, plant: Plant ->
            set(plant.schedule[period], { expert -> alarm.qualification in expert.qualifications} ) { it }.arbitrary()
        },
        pre = { alarm, period, plant ->
            period in plant.schedule.dom && alarm in plant.alarms
        },
        post = { alarm, period, plant, result ->
            result in plant.schedule[period] && alarm.qualification in result.qualifications
        }
    )

    val qualificationOK = function(
        command = { experts: Set<Expert>, reqQualification : Qualification ->
            exists(experts) { ex -> reqQualification in ex.qualifications }
        }
    )
}
