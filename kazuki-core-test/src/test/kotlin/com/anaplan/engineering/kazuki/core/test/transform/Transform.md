
---

> **Note**: This page is a work in progress, capturing current thinking before a more formal write up of semantics 

---

# Transform

Transform is a Kazuki language concept that faciliates the mutation of an object.
As Kazuki objects are immutable, a transform always involves the creation of a new object.
The new object will always retain its type. 

## Set transform

Transform on a set enables the mutation of a set's members, it does not enable the addition or removal of members. 

## Record Transform




# Design

* Want to be able to transform on super type
* Can't set derived properties
* Can't check derived properties when they depend upon values of other properties -- so can't use generic numeric construction pattern.
* 
