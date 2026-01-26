package com.anaplan.engineering.kazuki.core.test.typing

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.animals.*
import com.anaplan.engineering.kazuki.core.animals.Animal_Module.as_Animal
import com.anaplan.engineering.kazuki.core.animals.Animal_Module.is_Animal
import com.anaplan.engineering.kazuki.core.animals.Animal_Module.mk_Animal
import com.anaplan.engineering.kazuki.core.animals.Animal_Module.to_Animal
import com.anaplan.engineering.kazuki.core.animals.Cat_Module.as_Cat
import com.anaplan.engineering.kazuki.core.animals.Cat_Module.is_Cat
import com.anaplan.engineering.kazuki.core.animals.Cat_Module.mk_Cat
import com.anaplan.engineering.kazuki.core.animals.Cat_Module.to_Cat
import com.anaplan.engineering.kazuki.core.animals.Dog_Module.as_Dog
import com.anaplan.engineering.kazuki.core.animals.Dog_Module.is_Dog
import com.anaplan.engineering.kazuki.core.animals.Dog_Module.mk_Dog
import com.anaplan.engineering.kazuki.core.animals.Dog_Module.to_Dog
import com.anaplan.engineering.kazuki.core.test.*
import kotlin.test.Test
import kotlin.test.assertEquals


class TestRecordTyping {

    @Test
    fun staticIs() {
        // Something created as a dog is statically both a dog and an animal, but not a cat
        val mkDog = mk_Dog("Fido")
        assertEquals(true, mkDog is Dog)
        assertEquals(true, mkDog is Animal)
        assertEquals(false, mkDog is Cat)

        // Something created as an animal is statically an animal but the Kotlin compiler cannot statically confirm
        // whether it is a dog or a cat
        val mkAnimal = mk_Animal("Fido", Species.Dog, false)
        assertEquals(false, mkAnimal is Dog)
        assertEquals(true, mkAnimal is Animal)
        assertEquals(false, mkAnimal is Cat)

        // Something created as a generic tuple cannot be statically confirmed to be either a dog, cat or animal
        val tuple = mk_("Fido", Species.Dog)
        assertEquals(false, tuple is Dog)
        assertEquals(false, tuple is Animal)
        assertEquals(false, tuple is Cat)

        // Something created as a cat is statically both a cat and an animal, but not a dog
        val mkCat = mk_Cat("Nibbles", Species.Cat, false)
        assertEquals(false, mkCat is Dog)
        assertEquals(true, mkCat is Animal)
        assertEquals(true, mkCat is Cat)
    }

    // Note: test confirms there is no difference in behaviour between explicit and generic is_
    @Test
    fun dynamicIs_explicit() {
        // Something created as a dog is dynamically both a dog and an animal, but not a cat
        val mkDog = mk_Dog("Fido")
        assertEquals(true, is_Dog(mkDog))
        assertEquals(true, is_Animal(mkDog))
        assertEquals(false, is_Cat(mkDog))

        // Something created as an animal can by dynamically determined to be a dog or cat, by structure and the
        // satisfaction of invariants
        val mkAnimal = mk_Animal("Fido", Species.Dog, isAggressive = false)
        assertEquals(true, is_Dog(mkAnimal))
        assertEquals(true, is_Animal(mkAnimal))
        assertEquals(false, is_Cat(mkAnimal))

        // Note that, invariant implied by derived property is enforced. E.g. Dog's called Spike are aggresive, but
        // are not otherwise.
        assertEquals(false, is_Dog(mk_Animal("Fido", Species.Dog, isAggressive = true)))
        assertEquals(true, is_Dog(mk_Animal("Spike", Species.Dog, isAggressive = true)))
        assertEquals(false, is_Dog(mk_Animal("Spike", Species.Dog, isAggressive = false)))

        // Something created as a generic tuple can by dynamically determined to be a dog or cat, by structure and the
        // satisfaction of invariants
        val tuple = mk_("Fido", Species.Dog, false)
        assertEquals(true, is_Dog(tuple))
        assertEquals(true, is_Animal(tuple))
        assertEquals(false, is_Cat(tuple))

        // Something created as a cat is statically both a cat and an animal, but not a dog
        val mkCat = mk_Cat("Nibbles", Species.Cat, isAggressive = false)
        assertEquals(false, is_Dog(mkCat))
        assertEquals(true, is_Animal(mkCat))
        assertEquals(true, is_Cat(mkCat))
    }

    // Note: test confirms there is no difference in behaviour between explicit and generic is_
    @Test
    fun dynamicIs_generic() {
        // Something created as a dog is dynamically both a dog and an animal, but not a cat
        val mkDog = mk_Dog("Fido")
        assertEquals(true, is_<Dog>(mkDog))
        assertEquals(true, is_<Animal>(mkDog))
        assertEquals(false, is_<Cat>(mkDog))

        // Something created as an animal can by dynamically determined to be a dog or cat, by structure and the
        // satisfaction of invariants
        val mkAnimal = mk_Animal("Fido", Species.Dog, isAggressive = false)
        assertEquals(true, is_<Dog>(mkAnimal))
        assertEquals(true, is_<Animal>(mkAnimal))
        assertEquals(false, is_<Cat>(mkAnimal))

        // Note that, invariant implied by derived property is enforced. E.g. Dog's called Spike are aggresive, but
        // are not otherwise.
        assertEquals(false, is_<Dog>(mk_Animal("Fido", Species.Dog, isAggressive = true)))
        assertEquals(true, is_<Dog>(mk_Animal("Spike", Species.Dog, isAggressive = true)))
        assertEquals(false, is_<Dog>(mk_Animal("Spike", Species.Dog, isAggressive = false)))

        // Something created as a generic tuple can by dynamically determined to be a dog or cat, by structure and the
        // satisfaction of invariants
        val tuple = mk_("Fido", Species.Dog, false)
        assertEquals(true, is_<Dog>(tuple))
        assertEquals(true, is_<Animal>(tuple))
        assertEquals(false, is_<Cat>(tuple))

        // Something created as a cat is statically both a cat and an animal, but not a dog
        val mkCat = mk_Cat("Nibbles", Species.Cat, isAggressive = false)
        assertEquals(false, is_<Dog>(mkCat))
        assertEquals(true, is_<Animal>(mkCat))
        assertEquals(true, is_<Cat>(mkCat))
    }

    @Test
    fun staticAs() {
        // Static `as` can only be used to treat x as Y when `x is Y`
        // Static `as` does not change the underlying object so properties/functions derived from the type on creation
        // Kotlin compiler prevents the misuse of static `as` statically

        // Something created as a dog retains dog properties even after being treated as Animal
        val mkDog = mk_Dog("Fido")
        assertEquals("Woof", (mkDog as Dog).properties.says)
        assertEquals("Woof", (mkDog as Animal).properties.says)

        // Something created as an animal cannot be treated as specialization with static `as`
        val mkAnimal = mk_Animal("Fido", Species.Dog, isAggressive = false)
        assertEquals("Nothing", (mkAnimal as Animal).properties.says)

        // Something created as a generic tuple cannot be treated as an animal with static `as`

        // Something created as a cat retains dog properties even after being treated as Animal
        val mkCat = mk_Cat("Nibbles", Species.Cat, isAggressive = false)
        assertEquals("Miaow", (mkCat as Cat).properties.says)
        assertEquals("Miaow", (mkCat as Animal).properties.says)
    }

    // Note: test confirms there as no difference in behaviour between explicit and generic as_
    @Test
    fun dynamicAs_explicit() {
        // Dynamic `as` can only be used to treat x as Y when `is_Y(x)`
        // When dynamic `as` is generalising, it does not change the underlying object so properties/functions derived
        // from the type on creation
        // When dynamic `as` is specialising, the underlying object it acquires the properties/functions of the
        // specialized type
        // Kazuki prevents use of dynamic `as` dynamically through addition of pre-conditions

        // Something created as a dog retains dog properties even after being generalized as an Animal
        val mkDog = mk_Dog("Fido")
        assertEquals("Woof", as_Dog(mkDog).properties.says)
        assertEquals("Woof", as_Animal(mkDog).properties.says)
        causesPreconditionFailure { as_Cat(mkDog) }

        // Something created as an animal gets dog properties/functions when specialised as a dog
        val mkAnimal = mk_Animal("Fido", Species.Dog, isAggressive = false)
        assertEquals("Woof", as_Dog(mkAnimal).properties.says)
        assertEquals("Nothing", as_Animal(mkAnimal).properties.says)
        causesPreconditionFailure { as_Cat(mkAnimal) }
        causesPreconditionFailure { as_Dog(mk_Animal("Fido", Species.Dog, isAggressive = true)) }

        // Something created as a generic tuple gets dog properties/functions when specialised as a dog and
        // gets animal properties/functions when specialised as an animal
        val tuple = mk_("Fido", Species.Dog, false)
        assertEquals("Woof", as_Dog(tuple).properties.says)
        assertEquals("Nothing", as_Animal(tuple).properties.says)
        causesPreconditionFailure { as_Cat(tuple) }
        causesPreconditionFailure { as_Dog(mk_("Fido", Species.Dog, true)) }

        // Something created as a cat retains cat properties even after being generalized as an Animal
        val mkCat = mk_Cat("Nibbles", Species.Cat, isAggressive = false)
        causesPreconditionFailure { as_Dog(mkCat) }
        assertEquals("Miaow", as_Animal(mkCat).properties.says)
        assertEquals("Miaow", as_Cat(mkCat).properties.says)
    }

    // Note: test confirms there as no difference in behaviour between explicit and generic as_
    @Test
    fun dynamicAs_generic() {
        // Dynamic `as` can only be used to treat x as Y when `is_Y(x)`
        // When dynamic `as` is generalising, it does not change the underlying object so properties/functions derived
        // from the type on creation
        // When dynamic `as` is specialising, the underlying object it acquires the properties/functions of the
        // specialized type
        // Kazuki prevents use of dynamic `as` dynamically through addition of pre-conditions

        // Something created as a dog retains dog properties even after being generalized as an Animal
        val mkDog = mk_Dog("Fido")
        assertEquals("Woof", as_<Dog>(mkDog).properties.says)
        assertEquals("Woof", as_<Animal>(mkDog).properties.says)
        causesPreconditionFailure { as_<Cat>(mkDog) }

        // Something created as an animal gets dog properties/functions when specialised as a dog
        val mkAnimal = mk_Animal("Fido", Species.Dog, isAggressive = false)
        assertEquals("Woof", as_<Dog>(mkAnimal).properties.says)
        assertEquals("Nothing", as_<Animal>(mkAnimal).properties.says)
        causesPreconditionFailure { as_<Cat>(mkAnimal) }
        causesPreconditionFailure { as_<Dog>(mk_Animal("Fido", Species.Dog, isAggressive = true)) }

        // Something created as a generic tuple gets dog properties/functions when specialised as a dog and
        // gets animal properties/functions when specialised as an animal
        val tuple = mk_("Fido", Species.Dog, false)
        assertEquals("Woof", as_<Dog>(tuple).properties.says)
        assertEquals("Nothing", as_<Animal>(tuple).properties.says)
        causesPreconditionFailure { as_<Cat>(tuple) }
        causesPreconditionFailure { as_<Dog>(mk_("Fido", Species.Dog, true)) }

        // Something created as a cat retains cat properties even after being generalized as an Animal
        val mkCat = mk_Cat("Nibbles", Species.Cat, isAggressive = false)
        causesPreconditionFailure { as_<Dog>(mkCat) }
        assertEquals("Miaow", as_<Animal>(mkCat).properties.says)
        assertEquals("Miaow", as_<Cat>(mkCat).properties.says)
    }

    // No concept of static `to`

    // Note: test confirms there as no difference in behaviour between explicit and generic to_
    @Test
    fun dynamicTo_explicit() {
        // Dynamic `to` can only be used to convert x to Y when `is_Y(x)`
        // When using dynamic `to`, the underlying object always acquires the properties/functions of the
        // target type
        // Kazuki prevents use of dynamic `to` dynamically through addition of pre-conditions

        // Something created as a dog loses dog properties even after being converted to an Animal
        val mkDog = mk_Dog("Fido")
        assertEquals("Woof", to_Dog(mkDog).properties.says)
        assertEquals("Nothing", to_Animal(mkDog).properties.says)
        causesPreconditionFailure { to_Cat(mkDog) }

        // Something created as an animal gets dog properties/functions when converted to a dog
        val mkAnimal = mk_Animal("Fido", Species.Dog, isAggressive = false)
        assertEquals("Woof", to_Dog(mkAnimal).properties.says)
        assertEquals("Nothing", to_Animal(mkAnimal).properties.says)
        causesPreconditionFailure { to_Cat(mkAnimal) }
        causesPreconditionFailure { to_Dog(mk_Animal("Fido", Species.Dog, isAggressive = true)) }

        // Something created as a generic tuple gets dog properties/functions when converted to a dog and
        // gets animal properties/functions when converted to an animal
        val tuple = mk_("Fido", Species.Dog, false)
        assertEquals("Woof", to_Dog(tuple).properties.says)
        assertEquals("Nothing", to_Animal(tuple).properties.says)
        causesPreconditionFailure { to_Cat(tuple) }
        causesPreconditionFailure { to_Dog(mk_("Fido", Species.Dog, true)) }

        // Something created as a cat loses cat properties after being converted to an Animal
        val mkCat = mk_Cat("Nibbles", Species.Cat, isAggressive = false)
        causesPreconditionFailure { to_Dog(mkCat) }
        assertEquals("Nothing", to_Animal(mkCat).properties.says)
        assertEquals("Miaow", to_Cat(mkCat).properties.says)
    }

    // Note: test confirms there as no difference in behaviour between explicit and generic to_
    @Test
    fun dynamicTo_generic() {
        // Dynamic `to` can only be used to convert x to Y when `is_Y(x)`
        // When using dynamic `to`, the underlying object always acquires the properties/functions of the
        // target type
        // Kazuki prevents use of dynamic `to` dynamically through addition of pre-conditions

        // Something created as a dog loses dog properties even after being converted to an Animal
        val mkDog = mk_Dog("Fido")
        assertEquals("Woof", to_<Dog>(mkDog).properties.says)
        assertEquals("Nothing", to_<Animal>(mkDog).properties.says)
        causesPreconditionFailure { to_<Cat>(mkDog) }

        // Something created as an animal gets dog properties/functions when converted to a dog
        val mkAnimal = mk_Animal("Fido", Species.Dog, isAggressive = false)
        assertEquals("Woof", to_<Dog>(mkAnimal).properties.says)
        assertEquals("Nothing", to_<Animal>(mkAnimal).properties.says)
        causesPreconditionFailure { to_<Cat>(mkAnimal) }
        causesPreconditionFailure { to_<Dog>(mk_Animal("Fido", Species.Dog, isAggressive = true)) }

        // Something created as a generic tuple gets dog properties/functions when converted to a dog and
        // gets animal properties/functions when converted to an animal
        val tuple = mk_("Fido", Species.Dog, false)
        assertEquals("Woof", to_<Dog>(tuple).properties.says)
        assertEquals("Nothing", to_<Animal>(tuple).properties.says)
        causesPreconditionFailure { to_<Cat>(tuple) }
        causesPreconditionFailure { to_<Dog>(mk_("Fido", Species.Dog, true)) }

        // Something created as a cat loses cat properties after being converted to an Animal
        val mkCat = mk_Cat("Nibbles", Species.Cat, isAggressive = false)
        causesPreconditionFailure { to_<Dog>(mkCat) }
        assertEquals("Nothing", to_<Animal>(mkCat).properties.says)
        assertEquals("Miaow", to_<Cat>(mkCat).properties.says)
    }


}