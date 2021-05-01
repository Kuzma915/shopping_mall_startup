package com.vishalgaur.shoppingapp.ui.home

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Application
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.PagerSnapHelper
import com.google.android.material.chip.Chip
import com.vishalgaur.shoppingapp.R
import com.vishalgaur.shoppingapp.database.products.ShoeColors
import com.vishalgaur.shoppingapp.database.products.ShoeSizes
import com.vishalgaur.shoppingapp.databinding.FragmentProductDetailsBinding
import com.vishalgaur.shoppingapp.ui.DotsIndicatorDecoration
import com.vishalgaur.shoppingapp.viewModels.ProductViewModel
import java.lang.IllegalArgumentException

class ProductDetailsFragment : Fragment() {

	inner class ProductViewModelFactory(
            private val productId: String,
            private val application: Application
    ) : ViewModelProvider.Factory {
		@Suppress("UNCHECKED_CAST")
		override fun <T : ViewModel?> create(modelClass: Class<T>): T {
			if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
				return ProductViewModel(productId, application) as T
			}
			throw IllegalArgumentException("Unknown ViewModel Class")
		}
	}

	private lateinit var binding: FragmentProductDetailsBinding
	private lateinit var viewModel: ProductViewModel

	override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
		binding = FragmentProductDetailsBinding.inflate(layoutInflater)
		val productId = arguments?.getString("productId")

		if (activity != null && productId != null) {
			val viewModelFactory = ProductViewModelFactory(productId, requireActivity().application)
			viewModel = ViewModelProvider(this, viewModelFactory).get(ProductViewModel::class.java)
		}

		setViews()

		setObservers()
		return binding.root
	}

	private fun setViews() {
		binding.addProAppBar.topAppBar.title = viewModel.productData.value?.name
		binding.addProAppBar.topAppBar.setNavigationOnClickListener {
			findNavController().navigateUp()
		}

		setImagesView()

		binding.proDetailsTitleTv.text = viewModel.productData.value?.name ?: ""
		binding.proDetailsLikeBtn.apply {
			setOnClickListener {
				viewModel.toggleLikeProduct()
			}
		}
		binding.proDetailsRatingBar.rating = (viewModel.productData.value?.rating ?: 0.0).toFloat()
		binding.proDetailsPriceTv.text = resources.getString(
                R.string.pro_details_price_value,
                viewModel.productData.value?.price.toString()
        )
		setShoeSizeButtons()
//		setShoeColorsChips()
		binding.proDetailsSpecificsText.text = viewModel.productData.value?.description ?: ""
	}

	private fun setObservers() {
		viewModel.isLiked.observe(viewLifecycleOwner) {
			if (it == true) {
				binding.proDetailsLikeBtn.setImageResource(R.drawable.liked_heart_drawable)
			} else {
				binding.proDetailsLikeBtn.setImageResource(R.drawable.heart_icon_drawable)
			}
		}
	}

	private fun setImagesView() {
		if (context != null) {
			binding.proDetailsImagesRecyclerview.isNestedScrollingEnabled = false
			val adapter = ProductImagesAdapter(
                    requireContext(),
                    viewModel.productData.value?.images ?: emptyList()
            )
			binding.proDetailsImagesRecyclerview.adapter = adapter
			val rad = resources.getDimension(R.dimen.radius)
			val dotsHeight = resources.getDimensionPixelSize(R.dimen.dots_height)
			val inactiveColor = ContextCompat.getColor(requireContext(), R.color.gray)
			val activeColor = ContextCompat.getColor(requireContext(), R.color.blue_accent_300)
			val itemDecoration =
					DotsIndicatorDecoration(rad, rad * 4, dotsHeight, inactiveColor, activeColor)
			binding.proDetailsImagesRecyclerview.addItemDecoration(itemDecoration)
			PagerSnapHelper().attachToRecyclerView(binding.proDetailsImagesRecyclerview)
		}
	}

	@SuppressLint("ResourceAsColor")
	private fun setShoeSizeButtons() {

		binding.proDetailsSizesRadioGroup.apply {
			for((_, v) in ShoeSizes) {
				if(viewModel.productData.value?.availableSizes?.contains(v) == true) {
					val radioButton = RadioButton(context)
					radioButton.id = v
					radioButton.tag = v
					radioButton.setPadding(resources.getDimensionPixelSize(R.dimen.radio_padding_size))
					val param = binding.proDetailsSizesRadioGroup.layoutParams as ViewGroup.MarginLayoutParams
					param.setMargins(resources.getDimensionPixelSize(R.dimen.radio_margin_size))
					param.width = ViewGroup.LayoutParams.WRAP_CONTENT
					param.height = ViewGroup.LayoutParams.WRAP_CONTENT
					radioButton.layoutParams = param
					radioButton.background = ContextCompat.getDrawable(context, R.drawable.radio_selector)
					radioButton.setButtonDrawable(R.color.transparent)
					radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
					radioButton.setTextColor(R.color.black)
					radioButton.setTypeface(null, Typeface.BOLD)
					radioButton.textAlignment = View.TEXT_ALIGNMENT_CENTER
					radioButton.text = "$v"
					addView(radioButton)
				}
			}
			invalidate()
		}
//		binding.proDetailsSizesChipGroup.apply {
//			for ((_, v) in ShoeSizes) {
//				if (viewModel.productData.value?.availableSizes?.contains(v) == true) {
//					val chip = Chip(context)
//					chip.id = v
//					chip.tag = v
//					chip.text = "$v"
//
//					chip.chipStrokeColor =
//							ColorStateList.valueOf(ContextCompat.getColor(context, R.color.gray))
//					chip.checkedIcon = null
//					chip.chipStrokeWidth = TypedValue.applyDimension(
//                            TypedValue.COMPLEX_UNIT_DIP,
//                            1F,
//                            context.resources.displayMetrics
//                    )
//					chip.isCheckable = true
//					chip.isEnabled = true
//					chip.setTextColor(Color.BLACK)
//					chip.chipBackgroundColor = ColorStateList.valueOf(Color.TRANSPARENT)
//					chip.setOnCheckedChangeListener { _, isChecked ->
//						if (isChecked) {
//							chip.chipStrokeColor = ColorStateList.valueOf(
//                                    ContextCompat.getColor(
//                                            context,
//                                            R.color.blue_accent_300
//                                    )
//                            )
//						} else {
//							chip.chipStrokeColor = ColorStateList.valueOf(
//                                    ContextCompat.getColor(
//                                            context,
//                                            R.color.gray
//                                    )
//                            )
//						}
//					}
//
//					addView(chip)
//				}
//			}
//			invalidate()
//		}
	}

//	private fun setShoeColorsChips() {
//		binding.proDetailsColorsChipGroup.apply {
//			var ind = 1
//			for ((k, v) in ShoeColors) {
//				if (viewModel.productData.value?.availableColors?.contains(k) == true) {
//					val chip = Chip(context)
//					chip.id = ind
//					chip.tag = k
//					chip.text = ".."
//					chip.setTextColor(ColorStateList.valueOf(Color.parseColor(v)))
//					chip.chipStrokeColor =
//							ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blue_accent_300))
//					chip.chipStrokeWidth = TypedValue.applyDimension(
//                            TypedValue.COMPLEX_UNIT_DIP,
//                            1F,
//                            context.resources.displayMetrics
//                    )
//					chip.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor(v))
//					chip.isEnabled = true
//					chip.isCheckable = true
//					addView(chip)
//					ind++
//				}
//			}
//			invalidate()
//		}
//	}

}