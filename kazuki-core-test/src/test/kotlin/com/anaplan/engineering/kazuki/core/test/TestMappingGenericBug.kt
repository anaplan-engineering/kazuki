package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.Mapping
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.first
import com.anaplan.engineering.kazuki.core.last
import com.anaplan.engineering.kazuki.core.mk_
import com.anaplan.engineering.kazuki.core.nat1
import com.anaplan.engineering.kazuki.core.test.ClassWithGeneric_Module.mk_ClassWithGeneric
import com.anaplan.engineering.kazuki.core.test.MappingWithClassWithBoundGeneric_Module.mk_MappingWithClassWithBoundGeneric
import com.anaplan.engineering.kazuki.core.test.MappingWithClassWithGenericExtTypeAlias_Module.mk_MappingWithClassWithGenericExtTypeAlias
import com.anaplan.engineering.kazuki.core.test.MappingWithClassWithGenericExt_Module.mk_MappingWithClassWithGenericExt
import com.anaplan.engineering.kazuki.core.test.MappingWithClassWithOutGeneric_Module.mk_MappingWithClassWithOutGeneric
import kotlin.test.Test
import kotlin.test.assertEquals


@Module
interface ClassWithGeneric<T> {
    val t: T
}

@Module
interface MappingWithClassWithGeneric<U> : Mapping<ClassWithGeneric<U>, Int>

@Module
interface MappingWithClassWithOutGeneric<out V> : Mapping<ClassWithGeneric<Int>, V>

@Module
interface MappingWithClassWithBoundGeneric<V: Int> : Mapping<ClassWithGeneric<Int>, V>

@Module
interface MappingWithClassWithGenericExt: MappingWithClassWithGeneric<Int>

@Module
interface MappingWithClassWithGenericExtTypeAlias: MappingWithClassWithGeneric<nat1>

class TestMappingGenericBug {

    @Test
    fun creation() {
        val mapping = mk_MappingWithClassWithGenericExt(mk_(mk_ClassWithGeneric(3), 3))
        assertEquals(3, mapping.first()._1.t)
        assertEquals(3, mapping.first()._2)
    }

    @Test
    fun creationWithTypeAlias() {
        val mapping = mk_MappingWithClassWithGenericExtTypeAlias(mk_(mk_ClassWithGeneric(3uL), 3))
        assertEquals(3uL, mapping.first()._1.t)
        assertEquals(3, mapping.first()._2)
    }

    @Test
    fun creationWithOutVariance() {
        val mapping = mk_MappingWithClassWithOutGeneric(mk_(mk_ClassWithGeneric(3), 3))
        assertEquals(3, mapping.first()._1.t)
        assertEquals(3, mapping.first()._2)
    }

    @Test
    fun creationWithBounds() {
        val mapping = mk_MappingWithClassWithBoundGeneric(mk_(mk_ClassWithGeneric(3), 3))
        assertEquals(3, mapping.first()._1.t)
        assertEquals(3, mapping.first()._2)
    }

}