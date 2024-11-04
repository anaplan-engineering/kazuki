package com.anaplan.engineering.kazuki.core

fun <I> identity() = function(command = { i: I -> i })

fun <I> alwaysTrue() = function(command = { _: I -> true })