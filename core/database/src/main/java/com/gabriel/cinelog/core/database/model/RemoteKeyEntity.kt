package com.gabriel.cinelog.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeyEntity(
    @PrimaryKey @ColumnInfo(name = "list_name") val listName: String,
    // Null after the last page
    @ColumnInfo(name = "next_page") val nextPage: Int?,
    // Epoch millis of the last refresh, to tell whether the cache is stale
    @ColumnInfo(name = "last_updated") val lastUpdated: Long,
)
