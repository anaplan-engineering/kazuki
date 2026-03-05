package com.anaplan.engineering.kazuki.core.test.transform

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.animals.*
import com.anaplan.engineering.kazuki.core.animals.Animal_Module.mk_Animal
import com.anaplan.engineering.kazuki.core.animals.Animal_Module.transform
import com.anaplan.engineering.kazuki.core.animals.Dog_Module.conditionalTransform
import com.anaplan.engineering.kazuki.core.animals.Dog_Module.mk_Dog
import com.anaplan.engineering.kazuki.core.animals.Dog_Module.transform
import com.anaplan.engineering.kazuki.core.internal.*
import com.anaplan.engineering.kazuki.core.test.*
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestRecordTransform {

    @Test
    fun canTransformGenericTuple() {
        assertEquals(
            mk_("Fido", Species.Cat, false),
            mk_("Fido", Species.Dog, false).transform(_2 = Species.Cat)
        )
    }

    @Test
    fun cantTransformWhenWillBreakInvariant() {
        causesPreconditionFailure {
            mk_Dog("Spike").transform(name = "Jake")
        }

        // But this is okay if the object is typed as an animal not a dog
        mk_Animal("Spike", Species.Dog, isAggressive = true).transform(name = "Fido")
    }

    @Test
    fun transformedObjectRetainsType() {
        val fido = mk_Dog("Spike").functions.setName("Fido")
        assertTrue(fido is Dog)
        assertEquals("Fido", fido.name)
        assertEquals(false, fido.isAggressive)
        assertEquals("Woof", fido.properties.says)

        val spike = fido.transform(name = "Spike")
        assertTrue(spike is Dog)
        assertEquals("Spike", spike.name)
        assertEquals(true, spike.isAggressive)
        assertEquals("Woof", spike.properties.says)
    }

    @Test
    fun conditionalTransform_failure() {
        val result = mk_Dog("Fido").conditionalTransform(
            name = "Invalid",
            onSuccess = { new -> success(new) },
            onFailure = { old -> failure(object : RuntimeException("Illegal name for $old"){}) }
        )
        val dog = result.getOrNull()
        assertTrue(result.isFailure)
        assertNull(dog)
    }

    @Test
    fun conditionalTransform_success() {
        val result = mk_Dog("Fido").conditionalTransform(
            name = "Spike",
            onSuccess = { new -> success(new) },
            onFailure = { old -> failure(object : RuntimeException("Illegal name for $old"){}) }
        )
        val dog = result.getOrNull()
        assertTrue(result.isSuccess)
        assertEquals(mk_Dog("Spike"), dog)
    }

}
