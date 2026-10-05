package com.assignment.spire

import android.app.Application
import androidx.room.Room
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class StoreApplication : Application() {
    lateinit var productRepository: ProductRepository
    lateinit var cartRepository: CartRepository

    override fun onCreate() {
        super.onCreate()

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://dummyjson.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(DummyJsonApi::class.java)

        val database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "store_database"
        ).build()

        productRepository = ProductRepository(api)
        cartRepository = CartRepository(database.cartDao())
    }
}