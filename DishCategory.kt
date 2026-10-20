package com.example.bit242713_foodmanager

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "BIT242713_DishCategory")
data class DishCategory(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "BIT242713_Id")
    val id: Int = 0,

    @ColumnInfo(name = "BIT242713_Name")
    var name: String,

    @ColumnInfo(name = "BIT242713_Description")
    var description: String,

    @ColumnInfo(name = "BIT242713_IsSynced")
    var isSynced: Boolean = false
)