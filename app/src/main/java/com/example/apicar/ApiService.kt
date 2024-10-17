package com.example.apicar

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

data class Car(
    val id: Int,
    val name: String,
    val price: Double,
    val isFullOptions: Boolean,
    val image: String
)

data class AddResponse(
    val status: Int,
    val status_message: String
)

interface ApiService {
    @GET("CarAPI/readAll.php")
    fun getCars(): Call<List<Car>>

    @POST("CarAPI/create.php")
    fun addCar(@Body car: Car): Call<AddResponse>

    @POST("/CarAPI/update.php")
    fun updateCar(@Body cars: Car): Call<AddResponse>

    @POST("CarAPI/delete.php")
    fun deleteCar(@Body car: Car): Call<AddResponse>
}
