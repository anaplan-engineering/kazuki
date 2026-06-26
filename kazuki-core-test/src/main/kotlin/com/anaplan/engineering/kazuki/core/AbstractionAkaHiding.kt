package com.anaplan.engineering.kazuki.core

import com.anaplan.engineering.kazuki.core.ConcreteAbs_Abstraction.xiConcreteAbs
import com.anaplan.engineering.kazuki.core.Concrete_Module.transform

@Module
interface Concrete {
    val a: integer
    val b: integer
    val c: integer

    @Invariant
    fun bgtra() = b > a

    @Invariant
    fun agtrc() = a > c

    @FunctionProvider(ConcreteFunctions::class)
    val functions: ConcreteFunctions

}

class ConcreteFunctions(val concrete: Concrete) {

    val addToA = function(
        command = { n: integer -> concrete.transform(a = concrete.a + n) },
        post = { n, result -> concrete.xiConcreteAbs(result) && result.a == concrete.a + n }
    )
}

@Abstraction(of = Concrete::class)
interface ConcreteAbs {
    val b: integer
    val c: integer
}


