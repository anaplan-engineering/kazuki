package com.anaplan.engineering.kazuki.toolkit.iso8601

import com.anaplan.engineering.kazuki.core.FunctionProvider
import com.anaplan.engineering.kazuki.core.Invariant
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.PrettyPrintable
import com.anaplan.engineering.kazuki.toolkit.iso8601.Dtg_Module.mk_Dtg


@Module
interface DtgInZone : PrettyPrintable {
    val date: Date
    val time: TimeInZone

    @Invariant
    fun dtgBeforeFirstDate() =
        !(date == FirstDate && time.properties.normalisedTime.plusOrMinusADay == PlusOrMinus.Plus)

    @Invariant
    fun dtgAfterLastDate() =
        !(date == LastDate && time.properties.normalisedTime.plusOrMinusADay == PlusOrMinus.Minus)

    override fun pretty() = properties.formatted


    @FunctionProvider(DtgInZoneProperties::class)
    val properties: DtgInZoneProperties

}

class DtgInZoneProperties(private val dtgInZone: DtgInZone) {

    val normalised by lazy {
        val normalisedTime = dtgInZone.time.properties.normalisedTime
        val baseDtg = mk_Dtg(dtgInZone.date, normalisedTime.time)
        when (normalisedTime.plusOrMinusADay) {
            PlusOrMinus.Plus -> baseDtg.functions.subtractDuration(OneDayDuration)
            PlusOrMinus.Minus -> baseDtg.functions.addDuration(OneDayDuration)
            PlusOrMinus.None -> baseDtg
        }
    }

    val formatted by lazy { dtgInZone.date.properties.formatted + "T" + dtgInZone.time.properties.formatted }
}