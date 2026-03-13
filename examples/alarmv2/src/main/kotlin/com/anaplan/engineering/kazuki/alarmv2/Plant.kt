package com.anaplan.engineering.kazuki.alarmv2

import com.anaplan.engineering.kazuki.core.*

@Module
interface Plant {
    val schedule: Schedule
    val alarms: Set<Alarm>

    @Invariant
    fun expertForAlarmAlwaysAvailable() =
        forall(alarms) { alarm ->
            forall(schedule.dom) { period ->
                exists(schedule[period]) { expert -> alarm.qualification in expert.qualifications }
            }
        }

    @FunctionProvider(PlantFunctions::class)
    val functions: PlantFunctions
}

class PlantFunctions(plant: Plant) {
    val numberOfExperts = function (
        command = { period: Period ->
            plant.schedule[period].card
        },
        pre = {period ->
            period in plant.schedule.dom
        }
    )

    val periodsExpertIsOnDuty = function(
        command = { expert: Expert ->
            set(plant.schedule.dom, { period -> expert in plant.schedule[period] }) { it }
        }
    )

    val expertToPage = function(
        command = { alarm: Alarm, period: Period ->
            set(plant.schedule[period], { expert -> alarm.qualification in expert.qualifications} ) { it }.arbitrary()
        },
        pre = { alarm, period ->
            period in plant.schedule.dom && alarm in plant.alarms
        },
        post = { alarm, period, result ->
            result in plant.schedule[period] && alarm.qualification in result.qualifications
        }
    )
}