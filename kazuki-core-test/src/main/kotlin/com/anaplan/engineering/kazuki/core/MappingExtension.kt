package com.anaplan.engineering.kazuki.core

@Module
interface MappingExtension<D, R>: Mapping<D, R> {
}

@Module
interface Mapping1Extension<D, R>: Mapping1<D, R> {
}

@Module
interface InjectiveMappingExtension<D, R>: InjectiveMapping<D, R> {
}

@Module
interface InjectiveMapping1Extension<D, R>: InjectiveMapping1<D, R> {
}