package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.property
import com.anaplan.engineering.kazuki.syntax.examples.Foo_Module.mk_Foo

/**
 * Exemplifies the use of mk_* constructor within the declaring module
 */
@Module
interface Foo<T: Number> {

    val value: T

    class Utilities {

        val arbitrary by property { mk_Foo(42) }
    }
}