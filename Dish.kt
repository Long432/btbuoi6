package com.example.bit242713_foodmanager

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "BIT242713_Dish",
    foreignKeys = [
        ForeignKey(
            entity = DishCategory::class,
            parentColumns = ["BIT242713_Id"],
            childColumns = ["BIT242713_DishCategoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("BIT242713_DishCategoryId")]
)
data class Dish(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "BIT242713_Id")
    val id: Int = 0,

    @ColumnInfo(name = "BIT242713_Name")
    var name: String,

    @ColumnInfo(name = "BIT242713_Price")
    var price: Double,

    @ColumnInfo(name = "BIT242713_PreparationTime")
    var preparationTime: Int, // Tính bằng phút

    @ColumnInfo(name = "BIT242713_IsAvailable")
    var isAvailable: Boolean,

    @ColumnInfo(name = "BIT242713_Description")
    var description: String,

    @ColumnInfo(name = "BIT242713_DishCategoryId")
    var categoryId: Int,

    @ColumnInfo(name = "BIT242713_DishImage")
    var dishImageJson: String,

    @ColumnInfo(name = "BIT242713_IsSynced")
    var isSynced: Boolean = false
)