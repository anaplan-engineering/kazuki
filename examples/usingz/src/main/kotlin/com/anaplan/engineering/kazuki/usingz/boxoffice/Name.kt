package com.anaplan.engineering.kazuki.usingz.boxoffice

import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.Sequence
import com.anaplan.engineering.kazuki.usingz.boxoffice.Name_Module.mk_Name

@Module
interface Name : Sequence<Char>

fun String.toName() = mk_Name(*toCharArray())