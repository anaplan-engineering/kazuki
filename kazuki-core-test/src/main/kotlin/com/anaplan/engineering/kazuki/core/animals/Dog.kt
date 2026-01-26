package com.anaplan.engineering.kazuki.core.animals

import com.anaplan.engineering.kazuki.core.FunctionProvider
import com.anaplan.engineering.kazuki.core.Invariant
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.function
import com.anaplan.engineering.kazuki.core.mk_Set
import com.anaplan.engineering.kazuki.core.property

@Module
interface Dog: Animal {

    // Contrast with approach in `Cat`, here the creator does not need to provide the species, but it is still available
    // and enforced
    override val species get() = Species.Dog

    override val isAggressive get() = name == "Spike"

    @Invariant
    fun mustHaveDogName() = name in AcceptableNames

    @FunctionProvider(DogFunctions::class)
    override val functions : DogFunctions

    @FunctionProvider(DogProperties::class)
    override val properties : DogProperties

    companion object {
        private val AcceptableNames = mk_Set("Spike", "Fido")
    }
}

interface Fetchable

class DogProperties(dog: Dog): AnimalProperties(dog) {
    override val says by property { "Woof" }
}

class DogFunctions(dog: Dog): AnimalFunctions(dog) {

    val fetch = function(
        command = { obj: Fetchable -> obj }
    )

}