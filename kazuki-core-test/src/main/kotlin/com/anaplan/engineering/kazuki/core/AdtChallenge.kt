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

// Case F1: unmakeable empty fields ADT with no implementation, MUST FAIL
//          why? ADT only allowed if internal @ImplementedBy is present
@Module(makeable = false)
interface FooNoImplementation

// Case F2: unmakeable empty fields ADT with non-internal implementation, MUST FAIL
//          why? ADT @ImplementedBy must be internal
@Module(makeable = false)
@ImplementedBy(BarNotInternal::class)
interface FooBarNotInternal

@Module
interface BarNotInternal : FooBarNotInternal {
    val bar: nat
}

// Case F3: unmakeable ADT with leaky fields and good (non-empty fields) implementation, MUST FAIL
//          why? ADT has internal implementation but leaks information
@Module(makeable = false)
@ImplementedBy(BarLeakyFoo::class)
interface LeakyFoo {
    val foo: nat
}

@Module
internal interface BarLeakyFoo : LeakyFoo {
    val bar: nat
}

// Case F4: unmakeable ADT with leaky fields and bad (empty fields) implementation, MUST FAIL
//          why? ADT has internal implementation without representation (empty fields), also with leaky fields
@Module(makeable = false)
@ImplementedBy(EmptyBarLeakyFoo::class)
interface LeakyFooAgain {
    val foo: nat
}
@Module
internal interface EmptyBarLeakyFoo : LeakyFooAgain

//// Case M1: unmakeable empty fields ADT and bad (empty fields) implementation, MIGHT FAIL? [or MUST FAIL? ALREADY FAILS]
////          why? empty ADT has internal implementation without representation (empty fields); harmless but useless?
//@Module(makeable = false)
//@ImplementedBy(EmptyBar::class)
//interface EmptyFoo
//
//@Module
//internal interface EmptyBar : EmptyFoo
//
//// Case M2: makeable ADT doesn't make sense, MIGHT FAIL? [or MUST FAIL? ALREADY FAILS]
////          why? making empty record is harmless/useless yet mistaken if an implementation expects more fields
//@Module
//@ImplementedBy(BarMakeableFoo::class)
//interface FooMakeable
//
//@Module
//internal interface BarMakeableFoo : FooMakeable {
//    val bar: nat
//}
//
//// Case M3: unmakeable empty fields ADT not linked to implementation, MIGHT FAIL? [or MUST FAIL? ALREADY FAILS]
//@Module(makeable = false)
//@ImplementedBy(WrongBar::class)
//interface UnextendedFoo
//
//@Module
//internal interface WrongBar : Other {
//    val bar: nat
//}

// Case M4: unmakeable empty fields ADT with mixed interface inheritance, MIGHT FAIL? [or MUST FAIL? WORKS WITH MIXED mk_]
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