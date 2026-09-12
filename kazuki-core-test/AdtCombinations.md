# ADT module combinations

Companion to `AdtChallenge.kt`. Each case uses the same comment style: **P** = must pass, **F** = must fail, **M** = ambiguous / policy decision / needs clearer diagnostics.

Active cases live uncommented in `AdtChallenge.kt` and have matching tests in `TestAdtChallenge.kt`. Cases **F6–F10** and **F13** are commented out in the fixture (enable one at a time when probing). **M1** is likewise commented.

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
//          why? usual use case
@Module(makeable = false)
@ImplementedBy(Bar::class)
interface Foo

@Module
internal interface Bar : Foo {
    val bar: nat
}
```

### Case P2: generic ADT (in `AdtChallenge.kt`)

```kotlin
// Case P2: parametric ADT, MUST PASS
//          why? Stack<T>-style opaque container
@Module(makeable = false)
@ImplementedBy(BoxImpl::class)
interface Box<T>

@Module
internal interface BoxImpl<T> : Box<T> {
    val payload: T
}
```

---

## Failing cases (F)

### Case F1: unmakeable empty ADT without `@ImplementedBy` (in `AdtChallenge.kt`)

```kotlin
// Case F1: unmakeable empty fields ADT with no implementation, MUST FAIL
//          why? ADT only allowed if internal @ImplementedBy is present
@Module(makeable = false)
interface FooNoImplementation
```

### Case F2: non-internal implementation (in `AdtChallenge.kt`)

```kotlin
// Case F2: unmakeable empty fields ADT with non-internal implementation, MUST FAIL
//          why? ADT @ImplementedBy must be internal
@Module(makeable = false)
@ImplementedBy(BarNotInternal::class)
interface FooBarNotInternal

@Module
interface BarNotInternal : FooBarNotInternal {
    val bar: nat
}
```

### Case F3: leaky fields on abstract ADT (in `AdtChallenge.kt`)

```kotlin
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
```

### Case F4: leaky abstract + empty impl (in `AdtChallenge.kt`)

```kotlin
// Case F4: unmakeable ADT with leaky fields and bad (empty fields) implementation, MUST FAIL
//          why? ADT has internal implementation without representation (empty fields), also with leaky fields
@Module(makeable = false)
@ImplementedBy(EmptyBarLeakyFoo::class)
interface LeakyFooAgain {
    val foo: nat
}

@Module
internal interface EmptyBarLeakyFoo : LeakyFooAgain
```

### Case F5: `@ImplementedBy` on a `@Module` object (in `AdtChallenge.kt`)

```kotlin
// Case F5: object module with @ImplementedBy, MUST FAIL
//          why? objects are singletons; ADT construction model does not apply
@Module
@ImplementedBy(BarObj::class)
object FooObj

@Module
internal interface BarObj {
    val bar: nat
}
```

### Case F6: `@ImplementedBy` target is not a `@Module` interface (commented in `AdtChallenge.kt`)

```kotlin
// Case F6: @ImplementedBy target is not a @Module interface, MUST FAIL
//          why? Kazuki cannot generate mk_/tuple for non-modules
@Module(makeable = false)
@ImplementedBy(PlainBar::class)
interface FooPlain

internal interface PlainBar : FooPlain {
    val bar: nat
}
```

### Case F7: `@ImplementedBy` target does not extend the abstract ADT (commented in `AdtChallenge.kt`)

```kotlin
// Case F7: @ImplementedBy target does not extend the abstract ADT, MUST FAIL
//          why? mk_ADT would delegate to unrelated module; currently may crash KSP
@Module(makeable = false)
@ImplementedBy(WrongBar::class)
interface UnextendedFoo

@Module(makeable = false)
interface WrongOther {
    val x: nat
}

@Module
internal interface WrongBar : WrongOther {
    val bar: nat
}
```

### Case F8: abstract ADT declares `@ComparableProperty` / fixed record field (commented in `AdtChallenge.kt`)

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

### Case F9: `@ImplementedBy` on makeable module (commented in `AdtChallenge.kt`)

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

### Case F10: `@ImplementedBy` with internal abstract + public concrete (commented in `AdtChallenge.kt`)

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

### Case F11: ADT abstract is `internal` (in `AdtChallenge.kt`)

```kotlin
// Case F11: ADT abstract is internal, MUST FAIL
//          why? ADT abstract must be public; internal abstract breaks intended opaque export surface
@Module(makeable = false)
@ImplementedBy(LocalImpl::class)
internal interface LocalADT

@Module
internal interface LocalImpl : LocalADT {
    val state: nat
}
```

### Case F12: `@ImplementedBy` on sequence/set/map module (non-record) (in `AdtChallenge.kt`)

```kotlin
// Case F12: @ImplementedBy on non-record @Module kind, MUST FAIL
//          why? ImplementedBy wiring currently lives in RecordType only
@Module(makeable = false)
@ImplementedBy(SeqImpl::class)
interface OpaqueSeq<T> : Sequence<T>

@Module
internal interface SeqImpl<T> : OpaqueSeq<T>
```

### Case F13: `@ImplementedBy` points at intermediate abstract module, not internal concrete (commented in `AdtChallenge.kt`)

```kotlin
// Case F13: @ImplementedBy(AnotherAbstract::class) instead of internal concrete, MUST FAIL
//          why? mk_ chain must target the internal concrete record directly, not a layered abstract module
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

## Maybe / policy cases (M)

### Case M1: empty abstract + empty impl (commented in `AdtChallenge.kt`)

```kotlin
// Case M1: unmakeable empty fields ADT and bad (empty fields) implementation, MIGHT FAIL?
//          why? empty ADT has internal implementation without representation (empty fields); harmless but useless?
@Module(makeable = false)
@ImplementedBy(EmptyBar::class)
interface EmptyFoo

@Module
internal interface EmptyBar : EmptyFoo
```

### Case M2: mixed interface inheritance (in `AdtChallenge.kt`)

```kotlin
// Case M2: unmakeable empty fields ADT with mixed interface inheritance, MIGHT FAIL?
//          why? WORKS WITH MIXED mk_
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
```

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
| generic empty | yes | internal | none | ≥1 | **P** (P2) — *currently crashes KSP* |
| empty | no | — | none | — | **F** (F1) |
| empty | yes | public | none | ≥1 | **F** (F2) |
| leaky | yes | internal | ≥1 | ≥1 | **F** (F3) |
| leaky | yes | internal | ≥1 | 0 (inherit only) | **F** (F4) |
| object | yes | internal | — | — | **F** (F5) |
| impl not `@Module` | yes | — | empty | ≥1 | **F** (F6) — *commented in fixture* |
| empty | yes | internal (wrong super) | none | ≥1 | **F** (F7) — *commented in fixture* |
| leaky / fixed | yes | internal | `@ComparableProperty` | ≥1 | **F** (F8) — *commented in fixture* |
| makeable | yes | internal | none | ≥1 | **F** (F9) — *commented in fixture* |
| internal abstract | yes | public concrete | none | ≥1 | **F** (F10) — *commented in fixture* |
| internal abstract | yes | internal | none | ≥1 | **F** (F11) |
| non-record module | yes | internal | — | — | **F** (F12) |
| layered abstract `@ImplementedBy` | yes | internal (middle) | on middle | ≥1 on concrete | **F** (F13) — *commented in fixture* |
| empty | yes | internal | none | 0 | **M/F** (M1) — *commented in fixture* |
| empty | yes | internal | none | via mixed supers | **M** (M2) |

---

## Suggested KSP touchpoints (investigation notes; no code changes yet)

| Rule | Where to enforce | Rationale |
|------|------------------|-----------|
| F1: empty unmakeable requires `@ImplementedBy` | `RecordType.addEmptyUnmakeableRecordType` or new `validateAdt()` before generation | Today generates `is_`/`pretty` only; no error |
| F2: impl must be `internal` | `ImplementedBy.validateAndResolveImplementedBy` | Today only checks `@Module` + field count on concrete |
| F3/F4: no record fields on abstract ADT | `RecordType` when `!makeable && hasImplementedBy`: reject non–function-provider properties | Today `LeakyFoo` gets public `as_Tuple` / `mk_LeakyFoo(foo, bar)` |
| F4: impl needs local representation fields | Extend validation: count tuple components declared on impl, not inherited from abstract | `PropertyProcessor` merges inherited fields; empty impl looks non-empty |
| F5: no `@ImplementedBy` on object modules | `ImplementedBy` or `ModuleProcessor` for object kind | Today `FooObj_Module` is empty; `@ImplementedBy` ignored |
| F6: impl must be `@Module` | `ImplementedBy.validateAndResolveImplementedBy` | Non-module target cannot get `mk_`/tuple |
| F7: impl must extend abstract | `ImplementedBy` before `resolveAncestorTypeArguments` | Today `IllegalStateException` in `getSuperTypePathTo!!` |
| F8: no fixed/leaky fields on abstract ADT | Same as F3 | `@ComparableProperty` leaks structure |
| F9: `@ImplementedBy` requires `makeable = false` | Already in `validateAndResolveImplementedBy` | Message is adequate |
| F10: public concrete with internal abstract | `ImplementedBy.validateAndResolveImplementedBy` | Inverse visibility bypass |
| F11: internal abstract ADT | `validateAdt()` or `ImplementedBy` on abstract module | Today generates internal `mk_LocalADT`; ADT export surface must be public |
| F12: non-record `@ImplementedBy` | Reject outside `RecordType` or extend to other module kinds | Today `OpaqueSeq_Module` empty; construction via `mk_SeqImpl` only |
| F13: layered `@ImplementedBy` target | `ImplementedBy.validateAndResolveImplementedBy` | Today: unmakeable middle → “must be makeable”; need explicit “must be internal concrete” rule |
| M1: empty impl | Already errors on concrete field count | Message could mention “ADT representation” |
| M2: mixed super constructors | Policy: document merged `mk_` params or restrict multi-inheritance ADT | Currently **passes** (`mk_MixedFoo(x, bar)`) |
| P2: generic ADT mk_ generation | `ImplementedBy.addImplementedByMkFunction` + `AncestorTypeArguments` | Today: `IndexOutOfBoundsException` when mapping type params (Box/BoxImpl) |

---

## Plan of work

Re-evaluation (Sep 2026) against adjusted `AdtCombinations.md` case numbering. **No Kotlin source changes yet** — isolated IDE KSP builds used temporary `AdtChallenge.kt` probes (file restored afterward).

### Build summary

| Probe | Result |
|-------|--------|
| **P1 only** (`Foo` / `Bar`) | **PASS** — `mk_Foo(bar)` → internal `Bar_Module.mk_Bar` |
| **P1 + F1–F4** (no `Box`) | **PASS** — all four F cases compile with no KSP error (gap) |
| **P3 / `Box<T>`** (+ P1) | **FAIL (crash)** — `IndexOutOfBoundsException: Index -1` in `AncestorTypeArguments.getTypeName` (`KspUtilities.kt:97`) while generating `mk_Box` |
| **Full `AdtChallenge.kt`** (includes `Box`) | **FAIL** — blocked by P3 crash before F-case validation matters |
| **M1** (`EmptyFoo` / `EmptyBar`) | **FAIL** — see messages below |
| **M2** (`FooMakeable`) | **FAIL** — see messages below |
| **M3** (`UnextendedFoo` / `WrongBar`) | **FAIL (crash)** — see messages below |

**P1** matches the `Stack` ADT pattern in `examples/adt`: public abstract + `@ImplementedBy` + internal concrete with representation fields; public `mk_` delegates inward. `Stack` avoids the P3 bug because its abstract API uses `@FunctionProvider` only (no empty generic record + `mk_` type-param wiring).

### `AdtChallenge.kt` cases — current KSP behaviour

| Case | Expected | Today | What gets generated / observed |
|------|----------|-------|--------------------------------|
| **P1** `Foo` / `Bar` | PASS | **PASS** | Public `mk_Foo(bar)` → internal `Bar_Module.mk_Bar` |
| **P2/P3** `Box<T>` / `BoxImpl` | PASS | **CRASH** | KSP dies in `addImplementedByMkFunction` resolving generic `payload: T` |
| **F1** `FooNoImplementation` | FAIL | **PASS** | `FooNoImplementation_Module`: `is_` / `pretty` only; **no `mk_`**, no error |
| **F2** `FooBarNotInternal` | FAIL | **PASS** | Public `mk_FooBarNotInternal` → public `BarNotInternal_Module.mk_BarNotInternal` |
| **F3** `LeakyFoo` | FAIL | **PASS** | Public `as_Tuple`, tuple `is_`, `mk_LeakyFoo(foo, bar)` — abstract field in public factory |
| **F4** `LeakyFooAgain` / `EmptyBarLeakyFoo` | FAIL | **PASS** | Public `mk_LeakyFooAgain(foo)`; impl inherits `foo` via `PropertyProcessor` so field-count check passes |
| **M1–M4** | — | Commented in `AdtChallenge.kt` | Probed separately (M1–M3 below); M4 still policy-only |

**Additional F cases (F5–F13)** live in this MD file only — not yet wired in `AdtChallenge.kt`. Not build-probed here; touchpoints table above records intended enforcement.

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

### M1–M3 — probed KSP errors (one case at a time; commented in `AdtChallenge.kt`)

Builds **fail** as expected, but diagnostics are poor (logged via `processingState.errors` / uncaught exceptions, often surfaced only as “Compilation error”).

| Case | Fails? | Actual KSP behaviour | Suggested improvement |
|------|--------|----------------------|------------------------|
| **M1** empty abstract + empty impl | Yes | (1) `@ImplementedBy target com.anaplan.engineering.kazuki.core.EmptyBar must have at least one non-fixed field` from `ImplementedBy.kt:69–71`. (2) Likely also `Record …EmptyBar must have fields when makeable` because `EmptyBar` defaults to `@Module` (makeable) with no fields — **second error is confusing and ADT-unaware**. | Single ADT-focused error on `EmptyBar`: *“ADT implementation must declare at least one internal representation field”*; suppress or reword generic makeable-record error when `@ImplementedBy` is in play. **Fix:** `ImplementedBy.kt` message text; optionally `@Module(makeable = false)` on impl in test fixture. |
| **M2** makeable + `@ImplementedBy` | Yes | `@ImplementedBy on com.anaplan.engineering.kazuki.core.FooMakeable requires @Module(makeable = false)` (`ImplementedBy.kt:42–44`) | Adequate; optional suffix *“(ADT abstract must be unmakeable)”*. **Fix:** message string only. |
| **M3** wrong impl (`WrongBar : Other`) | Yes | **`IllegalStateException: No super type path found to com.anaplan.engineering.kazuki.core.UnextendedFoo`** at `KspUtilities.kt:40`, stack through `addImplementedByMkFunction` → `addEmptyUnmakeableRecordType` | Pre-check subtype **before** `resolveAncestorTypeArguments`: *“@ImplementedBy target 'WrongBar' must extend 'UnextendedFoo'”* with file/line. **Fix:** `ImplementedBy.validateAndResolveImplementedBy` or guard in `addImplementedByMkFunction`. |

**M4** (commented): when enabled, **passes**; `mk_MixedFoo(x, bar)` would merge super-interface record params — policy case (see matrix).

#### P3 generic ADT — separate bug (must pass, currently crashes)

```
IndexOutOfBoundsException: Index -1 out of bounds for length 1
  at AncestorTypeArguments.getTypeName (KspUtilities.kt:97)
  at addImplementedByMkFunction (ImplementedBy.kt:97)
```

**Cause:** empty generic abstract `Box<T>` + `@ImplementedBy(BoxImpl)`; type-param mapping from concrete field `payload: T` fails when resolving ancestor type arguments (`typeParameters.indexOf` returns -1).

**Fix location:** `ImplementedBy.addImplementedByMkFunction` / `AncestorTypeArguments` — must handle generic ADT before F1–F4 rules matter for full `AdtChallenge.kt` compilation.

### Recommended implementation order (when KT changes are approved)

1. **P3** — fix generic `@ImplementedBy` mk_ generation (unblocks `AdtChallenge.kt` + `Box<T>`)
2. **F7/M3** — subtype check (replace `IllegalStateException` with clear error)
3. **F2/F10** — impl must be `internal`; reject inverted visibility
4. **F1** — empty unmakeable requires `@ImplementedBy`
5. **F3** — no record fields on ADT abstract
6. **F4** — local-only representation fields on impl
7. **F11/F12/F13** — internal abstract; non-record `@ImplementedBy`; `@ImplementedBy` must target internal concrete (replace vague “must be makeable” for F13)
8. **M1/M2** — improved diagnostic strings only

Implement one rule at a time with `AdtChallenge.kt` as the regression fixture. Keep **P1** green throughout; add **P3** once generic mk_ is fixed.
