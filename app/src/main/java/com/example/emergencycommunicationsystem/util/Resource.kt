package com.example.emergencycommunicationsystem.util

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val code: Int? = null) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}
