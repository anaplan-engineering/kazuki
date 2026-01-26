package com.anaplan.engineering.kazuki.ksp

import com.squareup.kotlinpoet.MemberName

internal object InbuiltNames {
    internal const val corePackage = "com.anaplan.engineering.kazuki.core"
    internal const val coreInternalPackage = "com.anaplan.engineering.kazuki.core.internal"

    internal const val prettyOrDefault = "prettyOrDefault"

    internal const val transform = "transform"

    internal val mkSet = MemberName(corePackage, "mk_Set")
    internal val mkSet1 = MemberName(corePackage, "mk_Set1")
    internal val asSet = MemberName(corePackage, "as_Set")
    internal val set = MemberName(corePackage, "set")
    internal val set1 = MemberName(corePackage, "set1")
    internal val asSet1 = MemberName(corePackage, "as_Set1")
    internal val mkSeq = MemberName(corePackage, "mk_Seq")
    internal val mkSeq1 = MemberName(corePackage, "mk_Seq1")
    internal val forall = MemberName(corePackage, "forall")
    internal val pre = MemberName(corePackage, "pre")

    internal val toNat = MemberName(corePackage, "toNat")
    internal val toNat1 = MemberName(corePackage, "toNat1")
    internal val toInteger = MemberName(corePackage, "toInteger")
    internal val safeToInt = MemberName(corePackage, "safeToInt")


}