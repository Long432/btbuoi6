package com.example.bit242713_foodmanager

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface DishDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dish: Dish): Long

    @Update
    suspend fun update(dish: Dish)

    @Delete
    suspend fun delete(dish: Dish)

    @Query("SELECT * FROM BIT242713_Dish")
    suspend fun getAllDishes(): List<Dish>

    @Query("SELECT * FROM BIT242713_Dish WHERE BIT242713_DishCategoryId = :categoryId")
    suspend fun getDishesByCategory(categoryId: Int): List<Dish>

    @Query("SELECT * FROM BIT242713_Dish WHERE BIT242713_IsSynced = 0")
    suspend fun getUnsyncedDishes(): List<Dish>
}