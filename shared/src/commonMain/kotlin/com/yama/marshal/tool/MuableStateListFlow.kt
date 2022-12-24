package com.yama.marshal.tool

import kotlinx.coroutines.flow.*

fun <T> StateFlow<List<T>>.find(predicate: (T) -> Boolean) =
    this.value.toList().find(predicate)

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

suspend fun <T> MutableStateFlow<List<T>>.add(item: T) {
    val data = this.value.toMutableList()
    data.add(item)
    this.emit(data)
}

fun <T> StateFlow<List<T>>.any(predicate: (T) -> Boolean) =
    this.value.any(predicate)

fun <T> MutableStateFlow<List<T>>.removeAll(predicate: (T) -> Boolean) {
    val data = this.value.toMutableList()
    data.removeAll(predicate)
    this.value = data
}

fun <T> Flow<List<T>>.filterList(predicate: (T) -> Boolean) = this.map {
    it.filter { d ->
        predicate(d)
    }
}

fun <T, R> Flow<List<T>>.mapList(transform: (T) -> R) = this.map { list ->
    list.map {
        transform(it)
    }
}

fun <T> Flow<List<T>>.onEachList(action: suspend (T) -> Unit) = this.onEach { c ->
    c.onEach {
        action(it)
    }
}