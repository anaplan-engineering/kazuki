package com.anaplan.engineering.kazuki.core.internal

import com.anaplan.engineering.kazuki.core.PrettyPrintable

internal fun _prettyOrDefault(obj: Any?) =
    if (obj is PrettyPrintable) {
        obj.pretty()
    } else {
        obj.toString()
    }