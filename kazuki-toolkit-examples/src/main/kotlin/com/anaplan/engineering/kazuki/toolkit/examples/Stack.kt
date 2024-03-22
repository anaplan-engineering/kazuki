package com.anaplan.engineering.kazuki.toolkit.examples

import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.OrderedSet
import com.anaplan.engineering.kazuki.toolkit.examples.Stack_Module.mk_Stack

@Module
interface Stack<T>: OrderedSet<T> {
    
    class Functions<T> {
        
        val push = function(
            command = { s: Stack<T>, t: T -> mk_Stack(listOf(t) + s) },
            pre = { s: Stack<T>, t: T -> t !in s },
            post = { s: Stack<T>, t: T, result: Stack<T> ->
                   result.isNotEmpty() && result.first() == t && result.drop(1) == s
            }
        )
        
        val pop = function(
            command = { s: Stack<T> -> mk_(s.first(), s.drop(1)) },
            pre = { s: Stack<T> -> s.len > 0 },
            post = { s: Stack<T>, result: Tuple2<T, Stack<T>> -> push(result._2, result._1) == s }
        )
    }
}