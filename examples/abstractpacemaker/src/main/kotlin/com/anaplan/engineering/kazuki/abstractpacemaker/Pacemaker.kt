package com.anaplan.engineering.kazuki.abstractpacemaker

import com.anaplan.engineering.kazuki.abstractpacemaker.Trace_Module.as_Trace
import com.anaplan.engineering.kazuki.abstractpacemaker.Trace_Module.mk_Trace
import com.anaplan.engineering.kazuki.core.*

@Module
interface Pacemaker {
    val aperiod: nat
    val vdelay: nat

    @FunctionProvider(PacemakerProperties::class)
    val properties: PacemakerProperties

    @FunctionProvider(PacemakerFunctions::class)
    val functions: PacemakerFunctions
}

class PacemakerProperties(pacemaker: Pacemaker) {

    val idealHeart by property {
        as_Trace(seq(1uL..100uL) { i ->
            if (i % pacemaker.aperiod == 1uL) {
                Event.A
            } else if (i % pacemaker.aperiod == pacemaker.vdelay + 1uL) {
                Event.V
            } else {
                Nil
            }
        })
    }
}

class PacemakerFunctions(pacemaker: Pacemaker) {

    val pace = function(
        command = { tr: Trace ->
            mk_Trace(Nil) cat seq(tr.tail().tuples) { (i, e) ->
                if (i % pacemaker.aperiod == pacemaker.vdelay + 1uL && e != Event.V) {
                    Event.V
                } else {
                    Nil
                }
            }
        }
    )
}


