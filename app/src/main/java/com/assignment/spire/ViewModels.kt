package com.assignment.spire

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<Resource<List<Product>>>(Resource.Loading())
    val uiState: StateFlow<Resource<List<Product>>> = _uiState

    init {
        loadProducts()
    }

    fun loadProducts(query: String = "") {
        viewModelScope.launch {
            _uiState.value = Resource.Loading()
            _uiState.value = repository.fetchProducts(query)
        }
    }
}

class CartViewModel(private val repository: CartRepository) : ViewModel() {
    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartTotal: StateFlow<Double> = cartItems.map { items ->
        items.sumOf { it.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val itemCount: StateFlow<Int> = cartItems.map { items ->
        items.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addProductToCart(product: Product) {
        viewModelScope.launch { repository.addToCart(product) }
    }

    fun updateQuantity(item: CartItem, isIncrement: Boolean) {
        viewModelScope.launch { repository.updateQuantity(item, isIncrement) }
    }

    fun removeItem(item: CartItem) {
        viewModelScope.launch { repository.removeFromCart(item) }
    }
}