package com.assignment.spire

import android.app.Application
import androidx.room.Room
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class StoreApplication : Application() {
    lateinit var productRepository: ProductRepository
    lateinit var cartRepository: CartRepository

    override fun onCreate() {
        super.onCreate()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://dummyjson.com/")
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