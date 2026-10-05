package com.assignment.spire


import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class ProductResponse(val products: List<Product>)

data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val rating: Double,
    val category: String,
    val brand: String?,
    val stock: Int,
    val thumbnail: String
)

interface DummyJsonApi {
    @GET("products")
    suspend fun getProducts(): Response<ProductResponse>

    @GET("products/search")
    suspend fun searchProducts(@Query("q") query: String): Response<ProductResponse>

    @GET("products/{id}")
    suspend fun getProductDetails(@Path("id") id: Int): Response<Product>
}