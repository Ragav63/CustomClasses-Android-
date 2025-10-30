package com.example.customclass.dynamicimage

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.customclass.R
import com.example.customclass.databinding.FragmentDynamicImageBinding


class DynamicImageFragment : Fragment() {

    private var _binding : FragmentDynamicImageBinding?=null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDynamicImageBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val images = listOf(
            "https://storagehotelbooking.blob.core.windows.net/hotel-images/H00037/7369b10d-77e4-4174-adf3-0e78334e4937.jpg",
            "https://storagehotelbooking.blob.core.windows.net/hotel-images/H00037/d8556e75-cb30-43bb-8c8f-df9fd85cb004.jpg",
            "https://storagehotelbooking.blob.core.windows.net/hotel-images/H00037/07380dde-0ef6-4808-b10b-413f3b61dbf9.jpg",
            "https://storagehotelbooking.blob.core.windows.net/hotel-images/H00037/ed10da4c-aa21-4987-85f2-c68978d99351.jpg",
            "https://storagehotelbooking.blob.core.windows.net/hotel-images/H00037/R00002/d8431959-477d-400b-acdb-93f5f7de67bc.png",
            "https://storagehotelbooking.blob.core.windows.net/hotel-images/H00037/R00002/9159356c-d0a9-4ffc-a081-8fde59be6cfd.png?sv=2025-05-05&se=2025-09-07T05%3A39%3A53Z&sr=b&sp=r&sig=rNQmyYHZWmCs0sywhBhLSM1lKMzrizcvfxUG5C0T4R4%3D",
            "https://storagehotelbooking.blob.core.windows.net/hotel-images/H00037/R00002/1c5ef5b8-f34e-47a6-8e60-05b310ab5b9e.jpg?sv=2025-05-05&se=2025-09-07T05%3A39%3A53Z&sr=b&sp=r&sig=6Vmpjz84O8whK6rbMcN5lPxIl1PaEI6TW3MXfX7n59s%3D",

            )

        fun setupClick(textView: TextView, count: Int) {
            textView.setOnClickListener {
                val subList = images.take(count)
                setupHotelImages(subList)
            }
        }

        // Assign behavior
        setupClick(binding.one, 1)
        setupClick(binding.two, 2)
        setupClick(binding.three, 3)
        setupClick(binding.four, 4)
        setupClick(binding.five, 5)
        setupClick(binding.six, images.size)

    }

    fun setupHotelImages(imageUrls: List<String>) {
        val context = binding.gridHotelImages.context
        binding.gridHotelImages.removeAllViews()

        val screenWidth = resources.displayMetrics.widthPixels
        val density = context.resources.displayMetrics.density

        val totalImages = imageUrls.size
        val displayImages = minOf(5, totalImages)

        // Set column count based on the desired layout for the displayed images
        binding.gridHotelImages.columnCount = when (displayImages) {
            1 -> 1
            2 -> 2
            3 -> 3
            4 -> 2
            5 -> 6
            else -> 6
        }

        val normalHeight = (220 * density).toInt()
        val baseHeight = (130 * density).toInt()

        for (index in 0 until displayImages) {
            val url = imageUrls[index]
            val gridLayoutParams: GridLayout.LayoutParams
            var backgroundResId: Int? = null

            when (displayImages) {
                1 -> {
                    gridLayoutParams = GridLayout.LayoutParams().apply {
                        width = 0
                        height = normalHeight
                        columnSpec = GridLayout.spec(0, 1, 1f)
                        setMargins(4, 4, 4, 4)
                    }
                    backgroundResId = R.drawable.curve_trs
                }
                2 -> {
                    gridLayoutParams = GridLayout.LayoutParams().apply {
                        width = 0
                        height = normalHeight
                        columnSpec = GridLayout.spec(index, 1, 1f)
                        setMargins(4, 4, 4, 4)
                    }
                    backgroundResId = when (index) {
                        0 -> R.drawable.top_left_btm_left_curve_trs
                        else -> R.drawable.top_right_btm_right_curve_trs
                    }
                }
                3 -> {
                    gridLayoutParams = GridLayout.LayoutParams().apply {
                        width = 0
                        height = normalHeight
                        columnSpec = GridLayout.spec(index, 1, 1f)
                        setMargins(4, 4, 4, 4)
                    }
                    backgroundResId = when (index) {
                        0 -> R.drawable.top_left_btm_left_curve_trs
                        2 -> R.drawable.top_right_btm_right_curve_trs
                        else -> null
                    }
                }
                4 -> {
                    gridLayoutParams = GridLayout.LayoutParams().apply {
                        width = 0
                        height = baseHeight
                        rowSpec = GridLayout.spec(index / 2, 1)
                        columnSpec = GridLayout.spec(index % 2, 1, 1f)
                        setMargins(4, 4, 4, 4)
                    }
                    backgroundResId = when (index) {
                        0 -> R.drawable.top_left_curve_trs
                        1 -> R.drawable.top_right_curve_trs
                        2 -> R.drawable.bottom_left_curve_trs
                        else -> R.drawable.bottom_right_curve_trs
                    }
                }
                5 -> {
                    if (index < 2) {
                        gridLayoutParams = GridLayout.LayoutParams().apply {
                            width = 0
                            height = baseHeight
                            rowSpec = GridLayout.spec(0, 1)
                            columnSpec = GridLayout.spec(index * 3, 3, 1f)
                            setMargins(4, 4, 4, 4)
                        }
                        backgroundResId = when (index) {
                            0 -> R.drawable.top_left_curve_trs
                            else -> R.drawable.top_right_curve_trs
                        }
                    } else {
                        gridLayoutParams = GridLayout.LayoutParams().apply {
                            width = 0
                            height = baseHeight
                            rowSpec = GridLayout.spec(1, 1)
                            columnSpec = GridLayout.spec((index - 2) * 2, 2, 1f)
                            setMargins(4, 4, 4, 4)
                        }
                        backgroundResId = when (index) {
                            4 -> R.drawable.bottom_right_curve_trs
                            2 -> R.drawable.bottom_left_curve_trs
                            else -> null
                        }
                    }
                }
                else -> {
                    gridLayoutParams = GridLayout.LayoutParams()
                }
            }

            if (index == 4 && totalImages > 5) {
                val overlayLayout = FrameLayout(context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    backgroundResId?.let {
                        background = ContextCompat.getDrawable(context, it)
                        outlineProvider = ViewOutlineProvider.BACKGROUND
                        clipToOutline = true
                    }
                }

                val overlayImage = ImageView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    clipToOutline = true
                }
                Glide.with(context).load(url).into(overlayImage)

                val overlay = View(context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(Color.parseColor("#88000000"))
                }

                val countText = TextView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.CENTER
                    }
                    text = "+${totalImages - 5}"
                    setTextColor(Color.WHITE)
                    textSize = 20f
                    setTypeface(null, Typeface.BOLD)
                }

                overlayLayout.addView(overlayImage)
                overlayLayout.addView(overlay)
                overlayLayout.addView(countText)
                binding.gridHotelImages.addView(overlayLayout, gridLayoutParams)
            } else {
                val imageView = ImageView(context).apply {
                    layoutParams = gridLayoutParams
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    clipToOutline = true
                    backgroundResId?.let {
                        background = ContextCompat.getDrawable(context, it)
                        outlineProvider = ViewOutlineProvider.BACKGROUND
                    }
                }
                Glide.with(context).load(url).into(imageView)
                binding.gridHotelImages.addView(imageView)
            }
        }
    }
}