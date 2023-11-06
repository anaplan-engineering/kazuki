# kazuki

Kazuki is a formal specification language for general purpose software engineering.
The language is implemented as a layer over [Kotlin](https://kotlinlang.org/).

Aim is to provide:
* a specification syntax similar to [VDM-SL](https://www.overturetool.org/languages/) (although no stateful aspects will be supported)
* full, modern tool support 
* fast animation, facilitated by:
  * compiled and strongly-typed specification  
  * in-line 'drop to code'

## Status

Kazuki is immature and under active development.
There are big chunks of missing functionality/capability and all aspects of the language should be expected to change.
Documentation is currently sparse at best.
**Use now at your own risk!**

We are evolving the language as we look to translate some of our existing specifications. 
Expect frequent updates.

### Gradle setup

Kazuki is powered by Kotlin and the [Kotlin Symbol Processing API](https://kotlinlang.org/docs/ksp-overview.html) (KSP).
Thus, it is necessary to use the Kotlin and KSP plugins to use Kazuki.

Dependencies must then be added on the core api of Kazuki (`com.anaplan.engineering:kazuki-core`) and also its KSP library (`com.anaplan.engineering:kazuki-ksp`).

For example:

```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm' version("1.9.10")
    id 'com.google.devtools.ksp' version("1.9.10-1.0.13")
}

dependencies {
    api "com.anaplan.engineering:kazuki-core:0.0.1"
    ksp "com.anaplan.engineering:kazuki-ksp:0.0.1"
}
```

A dependency on `com.anaplan.engineering:kazuki-toolkit` can also be included to make use of Kazuki's standard toolkit. 

## Licence

See [LICENCE.txt](LICENCE.txt).