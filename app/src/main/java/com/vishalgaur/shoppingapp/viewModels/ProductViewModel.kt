package com.vishalgaur.shoppingapp.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vishalgaur.shoppingapp.data.Product
import com.vishalgaur.shoppingapp.data.Result
import com.vishalgaur.shoppingapp.data.ShoppingAppSessionManager
import com.vishalgaur.shoppingapp.data.source.repository.ProductsRepository
import com.vishalgaur.shoppingapp.data.utils.StoreDataStatus
import kotlinx.coroutines.launch

private const val TAG = "ProductViewModel"

class ProductViewModel(private val productId: String, application: Application) :
	AndroidViewModel(application) {

	private val _productData = MutableLiveData<Product?>()
	val productData: LiveData<Product?> get() = _productData

	private val _dataStatus = MutableLiveData<StoreDataStatus>()
	val dataStatus: LiveData<StoreDataStatus> get() = _dataStatus

	private val _isLiked = MutableLiveData<Boolean>()
	val isLiked: LiveData<Boolean> get() = _isLiked

	private val productsRepository = ProductsRepository.getRepository(application)
	private val sessionManager = ShoppingAppSessionManager(application.applicationContext)

	init {
		Log.d(TAG, "init: productId: $productId")
		getProductDetails()
		_isLiked.value = false
	}

	private fun getProductDetails() {
		viewModelScope.launch {
			_dataStatus.value = StoreDataStatus.LOADING
			try {
				Log.d(TAG, "getting product Data")
				val res = productsRepository.getProductById(productId)
				if (res is Result.Success) {
					_productData.value = res.data
					_dataStatus.value = StoreDataStatus.DONE
				} else if (res is Result.Error) {
					throw Exception("Error getting product")
				}
			} catch (e: Exception) {
				_productData.value = Product()
				_dataStatus.value = StoreDataStatus.ERROR
			}
		}
	}

	fun toggleLikeProduct() {
		_isLiked.value = !_isLiked.value!!
	}

	fun isSeller() = sessionManager.isUserSeller()
}