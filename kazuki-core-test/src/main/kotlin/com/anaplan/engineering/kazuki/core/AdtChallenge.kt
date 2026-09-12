package com.anaplan.engineering.kazuki.core

/**
 * Abstract Data Type (ADT) encapsulates functionality through a public API where
 * implementation is kept internal and inaccessible, such that you can change the
 * implementation without causing any ADT dependencies to break. This is common in
 * SPARK/Ada or VDM-SL with modules where ADT type is opaquely exported (e.g. VDM non-struct export).
 *
 * The `@ImplementedBy` Kazuki extension works in tandem with `@Module(makeable=false)`:
 * only unmakeable modules with linked `internal` implemented by inheritance realises
 * the intended ADT behaviour.
 *
 * Below we have various cases where Kazuki fails with different explanations regarding
 * the invalid ADT setup. Concretely, a valid ADT must:
 *
 * 1. Be an interface annotated with `@Module(makeable=false) @ImplementedBy(Impl::class) interface ADT`
 * 2. Have no fields beyond `@FunctionProvider` annotated ones for its exported APIs
 * 3. Have its implementation as a `@Module internal interface Impl : ADT` that must have non-function provider fields
 *
 * Notes:
 * - ADTs are instantiated via `mk_ADT`, which delegates construction to its implementation (i.e. internal `mk_Impl`)
 * - `@Module object X` cannot have an `@ImplementedBy(Impl::cls)` annotation.
 *
 * Cases created manually:
 * - P1; F1-F4, F7 (was M3, similar to M2), F9 (was M4), F13 (was M5); M1, M2
 * Cases suggested by Cursor that survived:
 * - F5-6, F8, F10-12 (already catered for, but with poor error messages)
 *
 * TODO add a construction annotation for `mk_ADT`, such that it tells what to expect? leave for now
 * TODO might ADTs have multiple implementations? This will complicate the `mk_ADT`, leave for now
 */

// Case P1: unmakeable empty fields ADT with implementation that is internal and non-empty, MUST PASS
//          why? usual use case
@Module(makeable = false)
@ImplementedBy(Bar::class)
interface Foo

@Module
internal interface Bar : Foo {
    val bar: nat
}

// Case P2: parametric ADT, MUST PASS
//          why? Stack<T>-style opaque container
@Module(makeable = false)
@ImplementedBy(BoxImpl::class)
interface Box<T>

@Module
internal interface BoxImpl<T> : Box<T> {
    val payload: T
}

//// Case F1: unmakeable empty fields ADT with no implementation, MUST FAIL
////          why? ADT only allowed if internal @ImplementedBy is present
//@Module(makeable = false)
//interface FooNoImplementation

//// Case F2: unmakeable empty fields ADT with non-internal implementation, MUST FAIL
////          why? ADT @ImplementedBy must be internal
//@Module(makeable = false)
//@ImplementedBy(BarNotInternal::class)
//interface FooBarNotInternal
//
//@Module
//interface BarNotInternal : FooBarNotInternal {
//    val bar: nat
//}

//// Case F3: unmakeable ADT with leaky fields and good (non-empty fields) implementation, MUST FAIL
////          why? ADT has internal implementation but leaks information
//@Module(makeable = false)
//@ImplementedBy(BarLeakyFoo::class)
//interface LeakyFoo {
//    val foo: nat
//}
//
//@Module
//internal interface BarLeakyFoo : LeakyFoo {
//    val bar: nat
//}

//// Case F4: unmakeable ADT with leaky fields and bad (empty fields) implementation, MUST FAIL
////          why? ADT has internal implementation without representation (empty fields), also with leaky fields
//@Module(makeable = false)
//@ImplementedBy(EmptyBarLeakyFoo::class)
//interface LeakyFooAgain {
//    val foo: nat
//}
//@Module
//internal interface EmptyBarLeakyFoo : LeakyFooAgain

//// Case F5: object module with @ImplementedBy, MUST FAIL
////          why? objects are singletons; ADT construction model does not apply
//@Module
//@ImplementedBy(BarObj::class)
//object FooObj
//
//@Module
//internal interface BarObj {
//    val bar: nat
//}

//// Case F6: @ImplementedBy target is not a @Module interface, [MUST FAIL, already failing, needs better error msg]
////          why? Kazuki cannot generate mk_/tuple for non-modules
//@Module(makeable = false)
//@ImplementedBy(PlainBar::class)
//interface FooPlain
//
//internal interface PlainBar : FooPlain {
//    val bar: nat
//}

//// Case F7: @ImplementedBy target does not extend the abstract ADT, [MUST FAIL, already failing, needs better error msg]
////          why? mk_ADT would delegate to unrelated module; currently may crash KSP
//@Module(makeable = false)
//@ImplementedBy(WrongBar::class)
//interface UnextendedFoo
//
//@Module(makeable = false)
//interface WrongOther {
//    val x: nat
//}
//
//@Module
//internal interface WrongBar : WrongOther {
//    val bar: nat
//}

//// Case F8: abstract ADT declares @ComparableProperty / fixed record field, [MUST FAIL, already fail, needs better error msg]
////          why? fixed fields are part of tuple representation and leak structure
//@Module(makeable = false)
//@ImplementedBy(FixedImpl::class)
//interface FixedADT {
//    @ComparableProperty
//    val id: nat
//}
//
//@Module
//internal interface FixedImpl : FixedADT {
//    val secret: nat
//}

//// Case F9: @ImplementedBy on makeable module, [MUST FAIL, already fails, needs better error msg]
////          why? public mk_/transform on abstract defeats encapsulation
//@Module
//@ImplementedBy(BarMakeableFoo::class)
//interface FooMakeable
//
//@Module
//internal interface BarMakeableFoo : FooMakeable {
//    val bar: nat
//}

//// Case F10: @ImplementedBy with internal abstract + public concrete, [MUST FAIL, already fails, needs better error msg]
////          why? public mk_Concrete bypasses internal abstract ADT barrier (inverse of F2)
//@Module(makeable = false)
//@ImplementedBy(PublicImpl::class)
//internal interface InternalADT
//
//@Module
//interface PublicImpl : InternalADT {
//    val bar: nat
//}

//// Case F11: ADT abstract is internal, MUST FAIL
////          why? ADT abstract must be public; internal abstract breaks intended opaque export surface
//@Module(makeable = false)
//@ImplementedBy(LocalImpl::class)
//internal interface LocalADT
//
//@Module
//internal interface LocalImpl : LocalADT {
//    val state: nat
//}

//// Case F12: @ImplementedBy on sequence/set/map module (non-record), MUST FAIL
////          why? ImplementedBy wiring currently lives in RecordType only
//@Module(makeable = false)
//@ImplementedBy(SeqImpl::class)
//interface OpaqueSeq<T> : Sequence<T>
//
//@Module
//internal interface SeqImpl<T> : OpaqueSeq<T>

//// Case F13: @ImplementedBy points at intermediate abstract module, not internal concrete, [MUST FAIL, already failing, needs better error msg]
////          why? mk_ chain must target the internal concrete record directly, not a layered abstract module
//@Module(makeable = false)
//@ImplementedBy(Middle::class)
//interface ADT12
//
//@Module(makeable = false)
//interface Middle : ADT12 {
//    val mid: nat
//}
//
//@Module
//internal interface MiddleImpl : Middle {
//    val rep: nat
//}

//// Case M1: unmakeable empty fields ADT and bad (empty fields) implementation, MIGHT FAIL? [or MUST FAIL? ALREADY FAILS]
////          why? empty ADT has internal implementation without representation (empty fields); harmless but useless?
//@Module(makeable = false)
//@ImplementedBy(EmptyBar::class)
//interface EmptyFoo
//
//@Module
//internal interface EmptyBar : EmptyFoo

// Case M2: unmakeable empty fields ADT with mixed interface inheritance, MIGHT FAIL? [or MUST FAIL? WORKS WITH MIXED mk_]
@Module(makeable = false)
@ImplementedBy(MixedBar::class)
interface MixedFoo

@Module(makeable = false)
interface Other {
    val x: nat
}

@Module
internal interface MixedBar : Other, MixedFoo {
    val bar: nat
}