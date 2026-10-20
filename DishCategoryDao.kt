package com.example.bit242713_foodmanager

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface DishCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: DishCategory): Long

    @Update
    suspend fun update(category: DishCategory)

    @Delete
    suspend fun delete(category: DishCategory)

    @Query("SELECT * FROM BIT242713_DishCategory")
    suspend fun getAllCategories(): List<DishCategory>

    @Query("SELECT * FROM BIT242713_DishCategory WHERE BIT242713_IsSynced = 0")
    suspend fun getUnsyncedCategories(): List<DishCategory>
}