package com.example.bit242713_foodmanager

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FoodRepository(private val db: AppDatabase) {

    private val categoryDao = db.dishCategoryDao()
    private val dishDao = db.dishDao()
    private val gson = Gson()

    suspend fun getAllCategories(): List<DishCategory> = categoryDao.getAllCategories()
    suspend fun getAllDishes(): List<Dish> = dishDao.getAllDishes()

    suspend fun insertCategory(category: DishCategory) = categoryDao.insert(category)
    suspend fun insertDish(dish: Dish) = dishDao.insert(dish)
    suspend fun updateDish(dish: Dish) = dishDao.update(dish)
    suspend fun deleteDish(dish: Dish) = dishDao.delete(dish)

    suspend fun getUnsyncedDishes(): List<Dish> = dishDao.getUnsyncedDishes()
    suspend fun getUnsyncedCategories(): List<DishCategory> = categoryDao.getUnsyncedCategories()

    fun imagesToJson(images: List<DishImageItem>): String = gson.toJson(images)

    fun jsonToImages(json: String): List<DishImageItem> {
        if (json.isEmpty()) return emptyList()
        val type = object : TypeToken<List<DishImageItem>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    suspend fun seedInitialDataIfEmpty() {
        val categories = categoryDao.getAllCategories()
        if (categories.isEmpty()) {
            val cat1Id = categoryDao.insert(
                DishCategory(name = "Lẩu & Nướng", description = "Các món lẩu và nướng hấp dẫn", isSynced = true)
            ).toInt()

            val cat2Id = categoryDao.insert(
                DishCategory(name = "Món Á", description = "Ẩm thực truyền thống châu Á", isSynced = true)
            ).toInt()

            val cat3Id = categoryDao.insert(
                DishCategory(name = "Đồ Uống & Tráng Miệng", description = "Nước giải khát và món tráng miệng", isSynced = true)
            ).toInt()

            val imgSample = listOf(
                DishImageItem("https://picsum.photos/200/200", isThumbnail = true)
            )
            val jsonImg = imagesToJson(imgSample)

            dishDao.insert(
                Dish(
                    name = "Lẩu Thái Hải Sản",
                    price = 250000.0,
                    preparationTime = 20,
                    isAvailable = true,
                    description = "Lẩu Thái chua cay đậm đà với tôm, mực, ngao",
                    categoryId = cat1Id,
                    dishImageJson = jsonImg,
                    isSynced = true
                )
            )

            dishDao.insert(
                Dish(
                    name = "Duysuc",
                    price = 180000.0,
                    preparationTime = 12,
                    isAvailable = true,
                    description = "Thịt bò sốt tiêu đen nướng xèo xèo",
                    categoryId = cat1Id,
                    dishImageJson = jsonImg,
                    isSynced = true
                )
            )

            dishDao.insert(
                Dish(
                    name = "Cơm Chiên Dương Châu",
                    price = 65000.0,
                    preparationTime = 10,
                    isAvailable = true,
                    description = "Cơm chiên hạt vàng ươm cùng lạp xưởng, tôm, đậu hà lan",
                    categoryId = cat2Id,
                    dishImageJson = jsonImg,
                    isSynced = true
                )
            )

            dishDao.insert(
                Dish(
                    name = "Phở Bò Hà Nội",
                    price = 55000.0,
                    preparationTime = 15,
                    isAvailable = true,
                    description = "Phở bò tái lăn nước dùng đậm đà truyền thống",
                    categoryId = cat2Id,
                    dishImageJson = jsonImg,
                    isSynced = true
                )
            )

            dishDao.insert(
                Dish(
                    name = "Trà Sữa Matcha",
                    price = 35000.0,
                    preparationTime = 5,
                    isAvailable = false,
                    description = "Trà sữa vị trà xanh Nhật Bản đậm vị",
                    categoryId = cat3Id,
                    dishImageJson = jsonImg,
                    isSynced = true
                )
            )
        }
    }
}

