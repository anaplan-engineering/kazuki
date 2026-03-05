package com.anaplan.engineering.kazuki.abstractpacemaker

import com.anaplan.engineering.kazuki.abstractpacemaker.Trace_Module.as_Trace
import com.anaplan.engineering.kazuki.core.*

sealed interface Traceable

enum class Event: Traceable {
    A,
    V,
}

object Nil: Traceable