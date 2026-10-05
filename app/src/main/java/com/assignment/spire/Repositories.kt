package com.assignment.spire

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.SocketTimeoutException

sealed class Resource<T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String) : Resource<T>()
    class Loading<T> : Resource<T>()
}

class ProductRepository(private val api: DummyJsonApi) {
    suspend fun fetchProducts(query: String = ""): Resource<List<Product>> {
        return try {
            val response = if (query.isEmpty()) {
                api.getProducts()
            } else {
                api.searchProducts(query)
            }

            if (response.isSuccessful) {
                val products = response.body()?.products ?: emptyList()
                Resource.Success(products)
            } else {
                Resource.Error("API request failed (Error: ${response.code()})")
            }
        } catch (e: SocketTimeoutException) {
            Resource.Error("The request timed out. Please try again.")
        } catch (e: IOException) {
            Resource.Error("No internet connection. Please check your network.")
        } catch (e: Exception) {
            Resource.Error("An unexpected error occurred.")
        }
    }
}

class CartRepository(private val cartDao: CartDao) {
    val cartItems: Flow<List<CartItem>> = cartDao.getAllCartItems()

    suspend fun addToCart(product: Product) = withContext(Dispatchers.IO) {
        val existingItem = cartDao.getCartItemById(product.id)
        if (existingItem != null) {
            cartDao.insertOrUpdate(existingItem.copy(quantity = existingItem.quantity + 1))
        } else {
            cartDao.insertOrUpdate(CartItem(product.id, product.title, product.price, product.thumbnail))
        }
    }

    suspend fun updateQuantity(item: CartItem, isIncrement: Boolean) = withContext(Dispatchers.IO) {
        if (isIncrement) {
            cartDao.insertOrUpdate(item.copy(quantity = item.quantity + 1))
        } else {
            if (item.quantity > 1) {
                cartDao.insertOrUpdate(item.copy(quantity = item.quantity - 1))
            } else {
                cartDao.delete(item) // Remove the item if quantity drops below 1
            }
        }
    }

    suspend fun removeFromCart(item: CartItem) = withContext(Dispatchers.IO) {
        cartDao.delete(item)
    }
}