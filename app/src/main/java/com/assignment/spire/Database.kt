package com.assignment.spire

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val id: Int,
    val title: String,
    val price: Double,
    val thumbnail: String,
    val quantity: Int = 1
)

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getAllCartItems(): Flow<List<CartItem>>

    @Query("SELECT * FROM cart_items WHERE id = :productId LIMIT 1")
    suspend fun getCartItemById(productId: Int): CartItem?

    // Change this to return Long (the inserted row ID)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: CartItem): Long

    // Change this to return Int (the number of rows deleted)
    @Delete
    suspend fun delete(item: CartItem): Int
}

@Database(entities = [CartItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
}