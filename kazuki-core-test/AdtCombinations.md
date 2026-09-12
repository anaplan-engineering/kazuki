# ADT module combinations (beyond `AdtChallenge.kt`)

Companion to `AdtChallenge.kt`. Each case uses the same comment style: **P** = must pass, **F** = must fail, **M** = ambiguous / policy decision / needs clearer diagnostics.

Valid ADT (target spec, from `AdtChallenge.kt`):

1. `@Module(makeable = false) @ImplementedBy(Impl::class) interface ADT`
2. No record fields on the abstract ADT (only `@FunctionProvider` APIs)
3. `@Module internal interface Impl : ADT` with at least one non–function-provider representation field declared on `Impl`
4. Construction via public `mk_ADT(...)` delegating to internal `mk_Impl(...)`
5. `@Module object X` cannot carry `@ImplementedBy`

---

## Passing cases (P)

### Case P1: standard empty abstract + internal concrete (in `AdtChallenge.kt`)

```kotlin
// Case P1: unmakeable empty fields ADT with implementation that is internal and non-empty, MUST PASS
//          why? usual use case (see Stack.kt example)
@Module(makeable = false)
@ImplementedBy(Bar::class)
interface Foo

@Module
internal interface Bar : Foo {
    val bar: nat
}
```

### Case P2: abstract exposes only `@FunctionProvider` APIs, no record fields

```kotlin
// Case P2: ADT with function-provider-only public API, MUST PASS
//          why? exported behaviour without leaking representation
@Module(makeable = false)
@ImplementedBy(CounterImpl::class)
interface Counter {
    @FunctionProvider
    val increment: () -> Counter
}

@Module
internal interface CounterImpl : Counter {
    val value: nat
}
```

### Case P3: generic ADT

```kotlin
// Case P3: parametric ADT, MUST PASS
//          why? Stack<T>-style opaque container
@Module(makeable = false)
@ImplementedBy(BoxImpl::class)
interface Box<T>

@Module
internal interface BoxImpl<T> : Box<T> {
    val payload: T
}
```

### Case P4: ADT where abstract extends another unmakeable module (no extra record fields on ADT)

```kotlin
// Case P4: ADT extending unmakeable super-interface without adding fields, MUST PASS
//          why? layered ADT contracts (compare M4 mixed inheritance)
@Module(makeable = false)
interface Readable {
    @FunctionProvider
    val read: () -> nat
}

@Module(makeable = false)
@ImplementedBy(ReadableImpl::class)
interface ReadableADT : Readable

@Module
internal interface ReadableImpl : ReadableADT {
    val buffer: nat
}
```

### Case P5: internal concrete with multiple representation fields

```kotlin
// Case P5: multi-field internal representation, MUST PASS
//          why? richer state behind opaque API
@Module(makeable = false)
@ImplementedBy(PairImpl::class)
interface OpaquePair

@Module
internal interface PairImpl : OpaquePair {
    val first: nat
    val second: nat
}
```

### Case P6: `@ImplementedBy` points at intermediate abstract module in delegation chain

```kotlin
// Case P6: @ImplementedBy(AnotherAbstract::class) with internal concrete below, MUST PASS
//          why? mk_ chain may delegate through a layered abstract module to internal concrete record
@Module(makeable = false)
@ImplementedBy(Middle::class)
interface ADT12

@Module(makeable = false)
interface Middle : ADT12 {
    val mid: nat
}

@Module
internal interface MiddleImpl : Middle {
    val rep: nat
}
```

---

## Failing cases (F)

Cases **F1–F4** are defined in `AdtChallenge.kt`. Additional combinations worth enforcing:

### Case F5: `@ImplementedBy` on a `@Module` object

```kotlin
// Case F5: object module with @ImplementedBy, MUST FAIL
//          why? objects are singletons; ADT construction model does not apply
@Module
@ImplementedBy(BarObj::class)
object FooObj

@Module
internal interface BarObj
```

### Case F6: `@ImplementedBy` target is not a `@Module` interface

```kotlin
// Case F6: implementation not a @Module, MUST FAIL
//          why? Kazuki cannot generate mk_/tuple for non-modules
@Module(makeable = false)
@ImplementedBy(PlainBar::class)
interface FooPlain

internal interface PlainBar : FooPlain {
    val bar: nat
}
```

### Case F7: `@ImplementedBy` target does not extend the abstract ADT

```kotlin
// Case F7: wrong implementation type (see M3), MUST FAIL
//          why? mk_ADT would delegate to unrelated module; currently may crash KSP
@Module(makeable = false)
@ImplementedBy(WrongBar::class)
interface UnextendedFoo

@Module
internal interface WrongBar : Other {
    val bar: nat
}

@Module(makeable = false)
interface Other {
    val x: nat
}
```

### Case F8: abstract ADT declares `@ComparableProperty` / fixed record field

```kotlin
// Case F8: ADT with fixed/comparable record field on abstract, MUST FAIL
//          why? fixed fields are part of tuple representation and leak structure
@Module(makeable = false)
@ImplementedBy(FixedImpl::class)
interface FixedADT {
    @ComparableProperty
    val id: nat
}

@Module
internal interface FixedImpl : FixedADT {
    val secret: nat
}
```

### Case F9: `@ImplementedBy` on makeable module (see M2 — treat as MUST FAIL)

```kotlin
// Case F9: makeable module with @ImplementedBy, MUST FAIL
//          why? public mk_/transform on abstract defeats encapsulation
@Module
@ImplementedBy(BarMakeableFoo::class)
interface FooMakeable

@Module
internal interface BarMakeableFoo : FooMakeable {
    val bar: nat
}
```

### Case F10: `@ImplementedBy` with internal abstract + public concrete

```kotlin
// Case F10: inverted visibility (internal abstract, public concrete), MUST FAIL
//          why? public mk_Concrete bypasses internal abstract ADT barrier (inverse of F2)
@Module(makeable = false)
@ImplementedBy(PublicImpl::class)
internal interface InternalADT

@Module
interface PublicImpl : InternalADT {
    val bar: nat
}
```

### Case F11: ADT abstract is `internal`

```kotlin
// Case F11: internal abstract ADT with internal implementation, MUST FAIL
//          why? ADT abstract must be public; internal abstract breaks intended opaque export surface
@Module(makeable = false)
@ImplementedBy(LocalImpl::class)
internal interface LocalADT

@Module
internal interface LocalImpl : LocalADT {
    val state: nat
}
```

### Case F12: `@ImplementedBy` on sequence/set/map module (non-record)

```kotlin
// Case F12: @ImplementedBy on non-record @Module kind, MUST FAIL
//          why? ImplementedBy wiring currently lives in RecordType only
@Module(makeable = false)
@ImplementedBy(SeqImpl::class)
interface OpaqueSeq<T> : Sequence<T>

@Module
internal interface SeqImpl<T> : OpaqueSeq<T>
```

---

## Maybe / policy cases (M)

Cases **M1–M4** are defined in `AdtChallenge.kt` only.

---

## Open design questions (TODO from `AdtChallenge.kt`)

These are deferred product/design items, not test cases.

### TODO: construction annotation for `mk_ADT`

Add an annotation (or `@ImplementedBy` parameter) that documents the expected public constructor shape for `mk_ADT`, so mismatches between abstract API and implementation fields are caught early with a targeted diagnostic (instead of inferred arity from merged tuple components).

Example intent:

```kotlin
// TODO: @AdtConstructor("bar: nat") or similar on Foo → mk_Foo(bar: nat) contract is explicit
@Module(makeable = false)
@ImplementedBy(Bar::class)
interface Foo
```

### TODO: multiple implementations per ADT

Support (or explicitly forbid) more than one `@ImplementedBy` target or a registry of internal implementations. Complicates:

- Which `mk_Impl` `mk_ADT` delegates to
- Whether multiple internal representations may exist behind one abstract type
- Testing and pretty-print / `is_` semantics across implementations

Leave unimplemented until policy is decided (single canonical impl vs factory/pluggable impl).

---

## Other combinations matrix (quick reference)

| Abstract | `@ImplementedBy` | Impl visibility | Abstract fields | Impl local fields | Expected |
|----------|-------------------|-----------------|-----------------|-------------------|----------|
| empty | yes | internal | none | ≥1 | **P** (P1) |
| empty | no | — | none | — | **F** (F1) |
| empty | yes | public | none | ≥1 | **F** (F2) |
| leaky | yes | internal | ≥1 | ≥1 | **F** (F3) |
| leaky | yes | internal | ≥1 | 0 (inherit only) | **F** (F4) |
| empty | yes | internal | none | 0 | **M/F** (M1) |
| makeable | yes | internal | none | ≥1 | **F** (M2/F9) |
| empty | wrong type | internal | none | ≥1 | **F** (M3/F7) |
| empty | yes | internal | none | via mixed supers | **M** (M4) |
| fn providers only | yes | internal | none | ≥1 | **P** (P2) |
| layered abstract `@ImplementedBy` | yes | internal | on middle only | ≥1 on concrete | **P** (P6) |
| internal abstract | yes | internal | none | ≥1 | **F** (F11) |
| non-record module | yes | internal | — | — | **F** (F12) |

---

## Suggested KSP touchpoints (investigation notes; no code changes yet)

| Rule | Where to enforce | Rationale |
|------|------------------|-----------|
| F1: empty unmakeable requires `@ImplementedBy` | `RecordType.addEmptyUnmakeableRecordType` or new `validateAdt()` before generation | Today generates `is_`/`pretty` only; no error |
| F2: impl must be `internal` | `ImplementedBy.validateAndResolveImplementedBy` | Today only checks `@Module` + field count on concrete |
| F3/F4: no record fields on abstract ADT | `RecordType` when `!makeable && hasImplementedBy`: reject non–function-provider properties | Today `LeakyFoo` gets public `as_Tuple` / `mk_LeakyFoo(foo, bar)` |
| F4: impl needs local representation fields | Extend validation: count tuple components declared on impl, not inherited from abstract | `PropertyProcessor` merges inherited fields; empty impl looks non-empty |
| F7/M3: impl must extend abstract | `ImplementedBy` before `resolveAncestorTypeArguments` | Today NPE in `getSuperTypePathTo!!` |
| F10: public concrete with internal abstract | `ImplementedBy.validateAndResolveImplementedBy` | Inverse visibility bypass |
| F11: internal abstract ADT | `validateAdt()` or `ImplementedBy` on abstract module | ADT export surface must be public |
| F12: non-record `@ImplementedBy` | Reject outside `RecordType` or extend to other module kinds | Currently unhandled module kinds |
| M2/F9: `@ImplementedBy` requires `makeable = false` | Already in `validateAndResolveImplementedBy` | Message is adequate |
| M1: empty impl | Already errors on concrete field count | Message could mention “ADT representation” |
| M4: mixed super constructors | Policy: document merged `mk_` params or restrict multi-inheritance ADT | Currently **passes** (`mk_MixedFoo(x, bar)`) |
| P6: layered `@ImplementedBy` target | Ensure mk_ delegation resolves through abstract middle to internal concrete | Treat as supported pattern |

---

## Plan ot work

Investigation summary from `AdtChallenge.kt` build analysis. **No Kotlin source changes yet** — this section records current behaviour, gaps, and recommended implementation order.

### `AdtChallenge.kt` cases — current KSP behaviour

A full IDE build of `kazuki-core-test` with all cases present **succeeds** (`isSuccess: true`). Every F case currently **passes** KSP; that is the gap to close.

| Case | Expected | Today | What gets generated |
|------|----------|-------|---------------------|
| **P1** `Foo` / `Bar` | PASS | PASS | Public `mk_Foo(bar)` → internal `Bar_Module.mk_Bar` — correct ADT barrier |
| **F1** `FooNoImplementation` | FAIL | **PASS** | Only `is_` / `pretty`; no `mk_` — incomplete ADT, no error |
| **F2** `FooBarNotInternal` | FAIL | **PASS** | Public `mk_FooBarNotInternal` → public `BarNotInternal_Module.mk_BarNotInternal` — full bypass |
| **F3** `LeakyFoo` | FAIL | **PASS** | Public `as_Tuple`, tuple-based `is_`, `mk_LeakyFoo(foo, bar)` — representation leaked on abstract |
| **F4** `LeakyFooAgain` / `EmptyBarLeakyFoo` | FAIL | **PASS** | Abstract still leaky; `mk_LeakyFooAgain(foo)` only — impl has no local fields but **inherits `foo`** via `PropertyProcessor`, so concrete field-count check passes |
| **M4** `MixedFoo` / `MixedBar` | MAYBE | PASS | `mk_MixedFoo(x, bar)` merges params from `Other` + `MixedBar` |

**P1** matches `Stack`: empty abstract, internal concrete with representation fields, public factory delegating inward.

### F1–F4 — where defensive checks are needed (and why)

#### F1 — unmakeable empty module without `@ImplementedBy`

**Gap:** `RecordType.addEmptyUnmakeableRecordType` generates `is_` / `pretty` only; no ADT validation.

**Fix location:** `RecordType.kt` (empty unmakeable path) or a new `validateAdtModule()` called from `ModuleProcessor` before generation.

**Rule:** `@Module(makeable = false)` with **zero record fields** should require `@ImplementedBy`, or fail with “incomplete ADT”.

**Suggested message:**  
`Unmakeable empty @Module 'FooNoImplementation' requires @ImplementedBy(Impl::class) for ADT construction`

#### F2 — non-internal implementation

**Gap:** `validateAndResolveImplementedBy` checks `@Module`, makeable, and field count — **not visibility**.

**Fix location:** `ImplementedBy.kt` → `validateAndResolveImplementedBy`, after resolving `concreteModule`.

**Rule:** `@ImplementedBy` target must be `internal` (Kotlin `KModifier.INTERNAL` on the declaration).

**Suggested message:**  
`@ImplementedBy target 'BarNotInternal' must be internal; ADT implementations must not be publicly constructible`

#### F3 — leaky fields on abstract ADT

**Gap:** `RecordType` treats any unmakeable module with properties as a normal record tuple. With `@ImplementedBy`, it still emits public `as_Tuple`, tuple `is_`, and merges abstract fields into `mk_*` parameters.

**Fix location:** `RecordType.kt` when `!makeable && resolvedImplementedBy != null`: reject non–`@FunctionProvider` / non–`@ComparableProperty` record properties on the **abstract** module. Optionally also skip tuple/`transform`/`as_Tuple` generation for ADT abstracts.

**Rule:** ADT abstract may only export `@FunctionProvider` APIs (per target spec in `AdtChallenge.kt` header).

**Suggested message:**  
`ADT 'LeakyFoo' must not declare record fields (found 'foo'); use @FunctionProvider for exported behaviour only`

#### F4 — empty impl + leaky abstract

**Two failures:**

1. **Leaky abstract** — same as F3.
2. **“Empty” impl that isn’t empty** — `PropertyProcessor` merges super-interface record fields into the concrete tuple, so `EmptyBarLeakyFoo` gets `foo` from `LeakyFooAgain` and satisfies the existing check in `ImplementedBy.kt`:

```kotlin
if (concreteVariableComponents.isEmpty()) {
    typeGenerationContext.processingState.errors.add(
        "@ImplementedBy target ${concreteModule.qualifiedName?.asString()} must have at least one non-fixed field"
    )
}
```

**Fix location:** extend validation in `ImplementedBy.kt` (or `PropertyProcessor`) to count **locally declared** representation fields on the impl, excluding those inherited from the abstract ADT.

**Suggested message:**  
`@ImplementedBy target 'EmptyBarLeakyFoo' must declare at least one internal representation field on the implementation itself (inherited ADT fields do not count)`

### M1–M3 — error messages (from code paths; cases still commented in `AdtChallenge.kt`)

| Case | Fails? | Current message | Improvement |
|------|--------|-----------------|-------------|
| **M1** empty abstract + empty impl | Yes | `@ImplementedBy target …EmptyBar must have at least one non-fixed field` | Mention ADT: *“implementation must declare internal representation state; empty ADT implementation is not allowed”* |
| **M2** makeable + `@ImplementedBy` | Yes | `@ImplementedBy on …FooMakeable requires @Module(makeable = false)` | Already clear; could add *“ADT abstracts must be unmakeable”* |
| **M3** wrong impl type (`WrongBar : Other`) | **Crash** | NPE at `getSuperTypePathTo(…)!!` in `AncestorTypeArgumentResolver.kt:22` when building `mk_UnextendedFoo` | Pre-check `concreteModule` is a subtype of abstract **before** `resolveAncestorTypeArguments`; e.g. *“@ImplementedBy target 'WrongBar' must extend 'UnextendedFoo'”* |

**M4** (active in `AdtChallenge.kt`): passes; `mk_MixedFoo(x, bar)` exposes `Other.x` in the public constructor — policy case (see matrix).

### Recommended implementation order (when KT changes are approved)

1. **F7/M3** — subtype check (prevents KSP crash; cheap win)
2. **F2/F10** — internal visibility on impl; reject inverted visibility (internal abstract + public concrete)
3. **F1** — require `@ImplementedBy` on empty unmakeable modules
4. **F3** — no record fields on ADT abstract
5. **F4** — local-only representation fields on impl
6. **F11/F12** — internal abstract ADT; non-record `@ImplementedBy`
7. **M1/M2** — improved diagnostics only
8. **P6** — verify layered `@ImplementedBy` delegation chain works end-to-end

Implement one rule at a time with `AdtChallenge.kt` as the regression fixture.
