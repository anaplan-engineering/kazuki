package com.anaplan.engineering.kazuki.alarm

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.alarm.Alarm_Module.mk_Plant

@Module
object Alarm {

    // Overall plant contains a schedule relating each period to the set of
    // experts on duty in the period; the alarms component records the alarms
    // which can possibly arrise

    interface Plant {
        val schedule: Schedule
        val alarms: Set<AlarmI>

        @Invariant
        fun expertForQualificationAlwaysAvailable() =
            forall(alarms) { alarm ->
                forall(schedule.dom) { period ->
                    QualificationOK(schedule[period], alarm.quali)
                }
            }
    }

    interface Schedule : Mapping1<Period, Set<Expert>> {
        @Invariant
        fun atLeastOneExpertOnDuty() =
            forall(rng) { exs ->
                exs.card > 0uL
            }

        @Invariant
        fun expertIdentifiersUniqueInSet() =
            forall(rng) { exs ->
                dunion(
                    set(exs) { ex1: Expert -> set(exs) { ex2: Expert -> (ex1 != ex2).implies(ex1.expertId != ex2.expertId) } }
                ).fold(true) { ex1, ex2 -> ex1 && ex2 }
            }

    }

    interface Period {val period: String}

    interface Expert {
        val expertId: ExpertId
        val quali: Set<Qualification>

        @Invariant
        fun atLeastOneQualification() =
            quali.card > 0uL
    }

    interface ExpertId {val expertId: integer}

    enum class Qualification {
        Elec,
        Mech,
        Bio,
        Chem
    }

    interface AlarmI {
        val alarmText: String
        val quali: Qualification
    }

    val numberOfExperts = function(
        command = { period: Period, plant: Plant ->
            plant.schedule[period].card
        },
        pre = {period, plant ->
            period in plant.schedule.dom
        }
    )

    val expertIsOnDuty = function(
        command = { expert: Expert, plant: Plant ->
            plant.schedule.dom.filter { period -> expert in plant.schedule[period] }
        }
    )

    val expertToPage = function(
        command = { alarm: AlarmI, period: Period, plant: Plant ->
            (plant.schedule[period]. filter {expert -> alarm.quali in expert.quali}).first()
        },
        pre = { alarm, period, plant ->
            expertToPagePre(alarm, period, plant)
        },
        post = { alarm, period, plant, result ->
            expertToPagePost(alarm, period, plant, result)
        }
    )

    // Exposed for testing purposes
    val expertToPagePre = { alarm: AlarmI, period: Period, plant: Plant ->
        period in plant.schedule.dom && alarm in plant.alarms
    }
    val expertToPagePost = { alarm: AlarmI, period: Period, plant: Plant, result: Expert ->
        result in plant.schedule[period] && alarm.quali in result.quali
    }

    val QualificationOK = function(
        command = { experts: Set<Expert>, reqQuali : Qualification ->
            exists(experts) { ex -> reqQuali in ex.quali }
        }
    )
}
