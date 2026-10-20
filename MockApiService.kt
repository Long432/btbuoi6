package com.example.bit242713_foodmanager

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface MockApiService {

    // Tên endpoint trên MockAPI của bạn (Ví dụ: /dishes)
    @POST("BIT242713_Dish")
    suspend fun pushDishToApi(@Body dish: Dish): Dish

    companion object {
        // THAY ĐƯỜNG LINK MOCKAPI CỦA BẠN VÀO ĐÂY (Giữ lại dấu / ở cuối)
        private const val BASE_URL = "https://650000000000000000.mockapi.io/api/v1/"

        fun create(): MockApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MockApiService::class.java)
        }
    }
}