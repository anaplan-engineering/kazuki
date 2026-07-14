package com.anaplan.engineering.kazuki.usingz.boxoffice

import com.anaplan.engineering.kazuki.core.InjectiveMapping
import com.anaplan.engineering.kazuki.core.Invariant
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.subset

// Example 11.23
@Module
interface System<Items, Client> {
    val seating: Set<Items>
    val sold: InjectiveMapping<Items, Client>

    @Invariant
    fun canOnlyOnlySellValidSeats() = sold.dom subset seating

}