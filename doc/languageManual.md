## Set type

### Typing

Generic set of A:

* `val a: Set<A>`
* `val a: Set1<A>`

Set-based type:

```
@Module
interface SetType: Set<A> {}
```

### Construction

Set enumeration:

* `mk_Set<A>(a, ...)`
* `mk_Set1<A>(a, ...)`
* `mk_SetType(a, ...`

Set comprehension:

* `set(1..3) { it * it }`

### Operators

Given set type `S` with elements typed `E`

| Operator           | Short operator | Name           | VDM Equivalent                | Type                    
|--------------------|----------------|----------------|-------------------------------|-------------------------
| `e in s1`          | -              | Membership     | `e in set s1`                 | `(E, S) -> bool`        
| `e !in s1`         | -              | Not membership | `e not in set s1`             | `(E, S) -> bool`        
| `s1 union s2`      | `s1 + s2`      | Union          | `s1 union s2`                 | `(S, Set<E>) -> S`      
| `s1 inter s2`      | -              | Intersection   | `s1 inter s2`                 | `(S, Set<E>) -> S`      
| `s1 diff s2`       | `s1 - s2`      | Difference     | `s1 \ s2`                     | `(S, Set<E>) -> S`      
| `s1 / s2`          | -              | Difference*    | `s1 inter s2`                 | `(S, Set<E>) -> Set<E>` 
| `s1 subset s2`     | -              | Subset         | `s1 subset s2`                | `(Set<E>, S) -> bool`   
| `s1 psubset s2`    | -              | Proper subset  | `s1 psubset s2`               | `(Set<E>, S) -> bool`   
| `s1 == s2`         | -              | Equality       | `s1 = s2`                     | `(Set<E>, S) -> bool`   
| `s1 != s2`         | -              | Inequality     | `s1 <> s2`                    | `(Set<E>, S) -> bool`   
| `s1.card`          | -              | Cardinality    | `card s1`                     | `(S) -> nat1`           
| `s1.single()`      | -              | ?              | `iota e in set s1 & true`     | `(S) -> E`              
| `s1.filter(fn)`    | -              | Filter         | `{e \| e in set s1 & fn(e) }` | `(S, (E) -> bool) -> S` 
| `s1.transform(fn)` | -              | Transform      | `{fn(e) \| e in set s1 }`     | `(S, (E) -> E) -> S`    

TODO:

* dunion, dinter, power
* fold, filter etc..

