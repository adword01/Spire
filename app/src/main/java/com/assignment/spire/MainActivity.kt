package com.assignment.spire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.assignment.spire.ui.theme.SpireTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as StoreApplication

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
                    return ProductViewModel(app.productRepository) as T
                }
                if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
                    return CartViewModel(app.cartRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val productViewModel: ProductViewModel = viewModel(factory = factory)
                val cartViewModel: CartViewModel = viewModel(factory = factory)

                StoreNavHost(navController, productViewModel, cartViewModel)
            }
        }
    }
}

@Composable
fun StoreNavHost(
    navController: NavHostController,
    productViewModel: ProductViewModel,
    cartViewModel: CartViewModel
) {
    val cartItemCount by cartViewModel.itemCount.collectAsState()

    NavHost(navController = navController, startDestination = "productList") {

        composable("productList") {
            ProductListScreen(
                viewModel = productViewModel,
                cartItemCount = cartItemCount, // Pass count
                onProductClick = { product ->
                    navController.navigate("productDetails/${product.id}")
                },
                onNavigateToCart = { navController.navigate("cart") }
            )
        }

        composable(
            route = "productDetails/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: return@composable

            val state = productViewModel.uiState.collectAsState().value
            val product = (state as? Resource.Success)?.data?.find { it.id == productId }

            if (product != null) {
                ProductDetailScreen(
                    product = product,
                    cartItemCount = cartItemCount, // Pass count
                    onAddToCart = { cartViewModel.addProductToCart(product) },
                    onNavigateUp = { navController.navigateUp() },
                    onNavigateToCart = { navController.navigate("cart") }
                )
            }
        }

        composable("cart") {
            CartScreen(
                viewModel = cartViewModel,
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}