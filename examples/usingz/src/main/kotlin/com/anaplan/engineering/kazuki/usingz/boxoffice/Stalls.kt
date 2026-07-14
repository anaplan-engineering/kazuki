package com.anaplan.engineering.kazuki.usingz.boxoffice

import com.anaplan.engineering.kazuki.core.Module

@Module
interface Stalls {
    val seating: Set<Seat>
}