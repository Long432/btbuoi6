package com.example.bit242713_foodmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SyncWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val repository = FoodRepository(db)
        val apiService = MockApiService.create()

        val unsyncedDishes = repository.getUnsyncedDishes()

        if (unsyncedDishes.isEmpty()) {
            return Result.success() // Không có gì cần đồng bộ
        }

        return try {
            for (dish in unsyncedDishes) {
                // Đẩy lên MockAPI
                apiService.pushDishToApi(dish)

                // Đẩy thành công -> Cập nhật Local Database thành Đã đồng bộ
                dish.isSynced = true
                repository.updateDish(dish)
                Log.d("SyncWorker", "Đã đồng bộ thành công món: ${dish.name}")
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Đồng bộ thất bại, sẽ thử lại khi có mạng: ${e.message}")
            Result.retry() // Mất mạng giữa chừng -> Yêu cầu hệ thống tự thử lại sau
        }
    }
}