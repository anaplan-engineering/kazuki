package com.anaplan.engineering.kazuki.abstractpacemaker

import com.anaplan.engineering.kazuki.abstractpacemaker.Trace_Module.as_Trace
import com.anaplan.engineering.kazuki.core.*

@Module
interface Trace : Sequence<Traceable> {

    @FunctionProvider(TraceFunctions::class)
    val functions: TraceFunctions

}

class TraceFunctions(trace: Trace) {

    val periodic = function(
        command = { e: Event, p: nat1 ->
            forall(trace.inds) { t ->
                (e == trace[t]) implies {
                    (t + p <= trace.len) implies {
                        trace[t + p] == e &&
                                forall(t + 1uL..t + p - 1uL) { trace[it] != e }
                    } &&
                    (t + p > trace.len) implies {
                        forall(t + 1uL..trace.len) { trace[it] != e }
                    }
                }
            }
        }
    )
}
