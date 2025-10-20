package com.anaplan.engineering.kazuki.core

@Module
interface RelationExtension<D, R>: Relation<D, R> {
}

@Module
interface RelationExtensionExtension: RelationExtension<Int, Int> {
}
