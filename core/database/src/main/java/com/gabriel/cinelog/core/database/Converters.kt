package com.gabriel.cinelog.core.database

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

// Lists never queried by element are stored as JSON
internal class Converters {
    @TypeConverter
    fun stringListToJson(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun jsonToStringList(value: String): List<String> = Json.decodeFromString(value)
}
