# Kazuki language manual

## Commands vs conditions

Make clear the difference between the language of the conditions and the language of the commands.
That is: the language of the conditions is the specification, the language of the commands is for animation.
Constructs like transform should be reserved for commands.

## Set types

### Typing

Generic set of `E`:

* `val a: Set<E>`
* `val a: Set1<E>`

Set-based type:

```
@Module
interface SetType: Set<E> {}
```

### Construction

Set enumeration:

* `mk_Set<E>(a, ...)`
* `mk_Set1<E>(a, ...)`
* `mk_SetType(a, ...`

Set comprehension:

* `set(1..3) { it * it }`
* `set1(1..3) { it * it }`

### Operators

Given set type `S` with elements typed `E`

| Operator           | Short operator | Name           | VDM Equivalent                 | Type                    
|--------------------|----------------|----------------|--------------------------------|-------------------------
| `e in s1`          | -              | Membership     | `e in set s1`                  | `(E, S) -> bool`        
| `e !in s1`         | -              | Not membership | `e not in set s1`              | `(E, S) -> bool`        
| `s1 union s2`      | `s1 + s2`      | Union          | `s1 union s2`                  | `(S, Set<E>) -> S`      
| `s1 inter s2`      | -              | Intersection   | `s1 inter s2`                  | `(S, Set<E>) -> S`      
| `s1 diff s2`       | `s1 - s2`      | Difference     | `s1 \ s2`                      | `(S, Set<E>) -> S`      
| `s1 / s2`          | -              | Difference*    | `s1 \ s2`                      | `(S, Set<E>) -> Set<E>` 
| `s1 subset s2`     | -              | Subset         | `s1 subset s2`                 | `(Set<E>, S) -> bool`   
| `s1 psubset s2`    | -              | Proper subset  | `s1 psubset s2`                | `(Set<E>, S) -> bool`   
| `s1 == s2`         | -              | Equality       | `s1 = s2`                      | `(Set<E>, S) -> bool`   
| `s1 != s2`         | -              | Inequality     | `s1 <> s2`                     | `(Set<E>, S) -> bool`   
| `s1.card`          | -              | Cardinality    | `card s1`                      | `(S) -> nat1`           
| `s1.single()`      | -              | Unique choice  | `iota e in set s1 & true`      | `(S) -> E`              
| `s1.filter(fn)`    | -              | Filter         | `{ e \| e in set s1 & fn(e) }` | `(S, (E) -> bool) -> S` 
| `s1.transform(fn)` | -              | Transform      | `{ fn(e) \| e in set s1 }`     | `(S, (E) -> E) -> S`    

TODO:

* dunion, dinter, power
* fold, filter etc..

## Sequence types

### Typing

Generic sequence of E:

* `val a: Sequence<E>`
* `val a: Sequence1<E>`

Sequence-based type:

```
@Module
interface SequenceType: Sequence<E> {}
```

### Construction

Sequence enumeration:

* `mk_Seq<E>(a, ...)`
* `mk_Seq1<E>(a, ...)`
* `mk_SeqType(a, ...)`

Sequence comprehension:

* `seq(1..3) { it * it }`
* `seq1(1..3) { it * it }`

### Operators

Given sequence type `S` with elements typed `E`

| Operator           | Short operator | Name           | VDM Equivalent                 | Type                    
|--------------------|----------------|----------------|--------------------------------|-------------------------
| `e in s1`          | -              | Membership     | `e in seq s1`                  | `(E, S) -> bool`        
| `e !in s1`         | -              | Not membership | `e not in seq s1`              | `(E, S) -> bool`        
| `s1.head()`        | -              | Head           | `hd s1`                        | `(S) -> E`      
| `s1.tail()`        | -              | Tail           | `tl sq`                        | `(S) -> Seq<E>`      
| `s1.len`           | -              | Length         | `len s1`                       | `(S) -> nat`      
| `s1.elems`         | -              | Elements       | `elems s1`                     | `(S) -> Set<E>` 
| `s1.inds`          | -              | Indices        | `inds s1`                      | `(S) -> Set<nat1>`   
| `s1.reverse()`     | -              | Reverse        | `reverse s1`                   | `(S) -> S`   
| `s1 == s2`         | -              | Equality       | `s1 = s2`                      | `(Seq<E>, S) -> bool`   
| `s1 != s2`         | -              | Inequality     | `s1 <> s2`                     | `(Seq<E>, S) -> bool`   
| `s1.single()`      | -              | Unique choice  | `iota e in seq s1 & true`      | `(S) -> E`              
| `s1.filter(fn)`    | -              | Filter         | `[ e \| e in seq s1 & fn(e) ]` | `(S, (E) -> bool) -> S` 
| `s1.transform(fn)` | -              | Transform      | `[ fn(e) \| e in set s1 ]`     | `(S, (E) -> E) -> S`    

TODO:

* type of tail? / needs retain type?
* 
* dunion, dinter, power
* fold, filter etc..

## The language of commands

### Type conversion

* E.g. `as_, to_` 

### Mutability

* `transform`
* `conditionalTransform`