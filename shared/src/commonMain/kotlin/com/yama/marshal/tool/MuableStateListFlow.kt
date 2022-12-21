package com.yama.marshal.tool

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

fun <T> StateFlow<List<T>>.find(predicate: (T) -> Boolean) =
    this.value.find(predicate)

fun <T>  StateFlow<List<T>>.indexOf(element: @UnsafeVariance T) =
    this.value.indexOf(element)

operator fun <T>  MutableStateFlow<List<T>>.set(index: Int, element: T) {
    val data = this.value.toMutableList()
    data[index] = element
    this.value = data
}

operator fun <T>  MutableStateFlow<List<T>>.get(index: Int) =
    this.value[index]

fun <T>  StateFlow<List<T>>.indexOfFirst(predicate: (T) -> Boolean) =
    this.value.indexOfFirst(predicate)

val <T> StateFlow<List<T>>.size
    get() = this.value.size

fun <T> MutableStateFlow<List<T>>.addAll(list: List<T>) {
    val data = this.value.toMutableList()
    data.addAll(list)
    this.value = data
}

fun <T> MutableStateFlow<List<T>>.add(item: T) {
    val data = this.value.toMutableList()
    data.add(item)
    this.value = data
}

fun <T> StateFlow<List<T>>.any(predicate: (T) -> Boolean) =
    this.value.any(predicate)

fun <T> MutableStateFlow<List<T>>.removeAll(predicate: (T) -> Boolean) {
    val data = this.value.toMutableList()
    data.removeAll(predicate)
    this.value = data
}