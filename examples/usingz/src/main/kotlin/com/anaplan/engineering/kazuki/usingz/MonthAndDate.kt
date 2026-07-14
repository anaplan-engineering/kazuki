package com.anaplan.engineering.kazuki.usingz

import com.anaplan.engineering.kazuki.core.*

enum class Month { jan, feb, mar, apr, may, jun, jul, aug, sep, oct, nov, dec }

@Module
interface DateNormalised {
    val month: Month
    val day: integer

    @Invariant
    fun validDay() =
        when (month) {
            Month.feb -> day in (1..29)
            Month.sep, Month.apr, Month.jun, Month.nov, -> day in (1..30)
            else -> day in (1..31)
        }

}