package com.example.emergencycommunicationsystem.data.local

import androidx.room.TypeConverter
import com.example.emergencycommunicationsystem.data.models.ForecastItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromForecastItemList(value: List<ForecastItem>): String {
        val gson = Gson()
        // Avoid relying on generic signature metadata (can be stripped by R8 in release).
        val type = TypeToken.getParameterized(List::class.java, ForecastItem::class.java).type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toForecastItemList(value: String): List<ForecastItem> {
        val gson = Gson()
        // Avoid relying on generic signature metadata (can be stripped by R8 in release).
        val type = TypeToken.getParameterized(List::class.java, ForecastItem::class.java).type
        return gson.fromJson(value, type) ?: emptyList()
    }
}
