package com.vishalgaur.shoppingapp.data.source.repository

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import com.vishalgaur.shoppingapp.ERR_UPLOAD
import com.vishalgaur.shoppingapp.data.Product
import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.Result.*
import com.vishalgaur.shoppingapp.data.source.local.ProductsLocalDataSource
import com.vishalgaur.shoppingapp.data.source.local.ShoppingAppDatabase
import com.vishalgaur.shoppingapp.data.source.remote.ProductsRemoteDataSource
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.*

class ProductsRepository(application: Application) {
    private val productsRemoteSource: ProductsRemoteDataSource
    private val productsLocalSource: ProductsLocalDataSource

    companion object {
        private const val TAG = "ProductsRepository"

        @Volatile
        private var INSTANCE: ProductsRepository? = null

        fun getRepository(app: Application): ProductsRepository {
            return INSTANCE ?: synchronized(this) {
                ProductsRepository(app).also {
                    INSTANCE = it
                }
            }
        }
    }

    init {
        val database = ShoppingAppDatabase.getInstance(application)
        productsLocalSource = ProductsLocalDataSource(database.productsDao())
        productsRemoteSource = ProductsRemoteDataSource()
    }

    suspend fun refreshProducts() {
        Log.d(TAG, "Updating Products in Room")
        updateProductsFromRemoteSource()
    }

    fun observeProducts(): LiveData<Result<List<Product>>?> {
        return productsLocalSource.observeProducts()
    }

    suspend fun getAllProductsByOwner(ownerId: String): LiveData<Result<List<Product>>> {
        return productsLocalSource.getAllProductsByOwner(ownerId)
    }

    suspend fun getProductById(productId: String, forceUpdate: Boolean = false): Result<Product> {
        if (forceUpdate) {
            updateProductFromRemoteSource(productId)
        }
        return productsLocalSource.getProductById(productId)
    }

    suspend fun insertProduct(newProduct: Product) {
        coroutineScope {
            launch {
                Log.d(TAG, "onInsertProduct: adding product to local source")
                productsLocalSource.insertProduct(newProduct)
            }
            launch {
                Log.d(TAG, "onInsertProduct: adding product to remote source")
                productsRemoteSource.insertProduct(newProduct)
            }
        }
    }

    suspend fun insertImages(imgList: List<Uri>): List<String> {
        var urlList = mutableListOf<String>()
        imgList.forEach label@{ uri ->
            val uniId = UUID.randomUUID().toString()
            val fileName = uniId + uri.lastPathSegment?.split("/")?.last()
            try {
                val downloadUrl = productsRemoteSource.uploadImage(uri, fileName)
                urlList.add(downloadUrl.toString())
            } catch (e: Exception) {
                productsRemoteSource.revertUpload(fileName)
                Log.d(TAG, "exception: message = $e")
                urlList = mutableListOf()
                urlList.add(ERR_UPLOAD)
                return@label
            }
        }
        return urlList
    }

    private suspend fun updateProductsFromRemoteSource() {
        val remoteProducts = productsRemoteSource.getAllProducts()
        if (remoteProducts is Success) {
            productsLocalSource.deleteAllProducts()
            productsLocalSource.insertMultipleProducts(remoteProducts.data)
        } else if (remoteProducts is Error) {
            throw remoteProducts.exception
        }
    }

    private suspend fun updateProductFromRemoteSource(productId: String) {
        val remoteProduct = productsRemoteSource.getProductById(productId)
        if (remoteProduct is Success && remoteProduct.data != null) {
            productsLocalSource.insertProduct(remoteProduct.data)
        }
    }
}