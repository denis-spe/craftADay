package com.den.craftaday.backend.states


sealed interface DataState<out T> {
    object Loading : DataState<Nothing>
    data class Success<T>(val data: T) : DataState<T>
    data class Error(val exception: Throwable) : DataState<Nothing>
}
