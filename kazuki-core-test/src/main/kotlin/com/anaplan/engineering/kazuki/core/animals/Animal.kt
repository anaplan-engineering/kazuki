package com.anaplan.engineering.kazuki.core.animals

import com.anaplan.engineering.kazuki.core.FunctionProvider
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.animals.Animal_Module.transform
import com.anaplan.engineering.kazuki.core.function
import com.anaplan.engineering.kazuki.core.property

@Module
interface Animal {

    val name: String
    val species: Species
    val isAggressive: Boolean

    @FunctionProvider(AnimalProperties::class)
    val properties : AnimalProperties

    @FunctionProvider(AnimalFunctions::class)
    val functions : AnimalFunctions
}

open class AnimalProperties(animal: Animal) {
    open val says by property { "Nothing" }
}

open class AnimalFunctions(animal: Animal) {

    val setName = function(
        command = { name: String -> animal.transform(name = name) }
    )

}