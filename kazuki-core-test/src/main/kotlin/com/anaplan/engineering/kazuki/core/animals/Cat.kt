package com.anaplan.engineering.kazuki.core.animals

import com.anaplan.engineering.kazuki.core.FunctionProvider
import com.anaplan.engineering.kazuki.core.Invariant
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.function
import com.anaplan.engineering.kazuki.core.mk_Set
import com.anaplan.engineering.kazuki.core.property

@Module
interface Cat: Animal {

    // Contrast with approach in `Dog`, here the creator must provide the species explicitly
    @Invariant
    fun isCat() = species == Species.Cat

    @FunctionProvider(CatFunctions::class)
    override val functions : CatFunctions

    @FunctionProvider(CatProperties::class)
    override val properties: CatProperties
}


class CatProperties(cat: Cat): AnimalProperties(cat) {
    override val says by property { "Miaow" }
}

class CatFunctions(cat: Cat): AnimalFunctions(cat) {

    val catAction = function(
        command = { obj: Int -> obj }
    )

}