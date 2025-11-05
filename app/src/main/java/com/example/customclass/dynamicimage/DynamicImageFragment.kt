package com.example.customclass.dynamicimage

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
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
import com.ragav63.dynamic_image_sdk.OnImageClickListener


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
            "https://storagehotelbooking.blob.core.windows.net/hotel-images/H00040/5d62820e-9270-4b15-9c16-29aae89842fa.jpg?sv=2025-05-05&se=2025-11-30T05%3A10%3A27Z&sr=b&sp=r&sig=JlxjzDdFH6phLg5odT4n4jvcFLZjkyYhrwuJs48YRME%3D",
            "https://storagehotelbooking.blob.core.windows.net/hotel-images/H00040/0f0340db-bbe0-4e37-8660-6c03b4f911d0.jpg?sv=2025-05-05&se=2025-11-30T05%3A10%3A27Z&sr=b&sp=r&sig=rGhOy5E92NjuUEcYOA4ABG8iYynLlYehrSxFCZAvDFA%3D",
            "https://storagehotelbooking.blob.core.windows.net/hotel-images/H00040/73a7fb2d-424e-449c-b8e9-17c04ab2aeeb.jpg?sv=2025-05-05&se=2025-11-30T05%3A10%3A27Z&sr=b&sp=r&sig=vKXpIrRQPYqm3iCjA7%2Bn%2FEmH58ZqZmPojKgz1JHhJ%2Bk%3D",
            "https://storagehotelbooking.blob.core.windows.net/hotel-images/H00040/5be8e70b-0f0a-400e-8a42-0fe203d824d9.jpg?sv=2025-05-05&se=2025-11-30T05%3A10%3A27Z&sr=b&sp=r&sig=xaieYax8cQ%2FM84Cag7BdMm5GQunaCqsggvNKZoSaTJI%3D",
            "https://storagehotelbooking.blob.core.windows.net/hotel-images/H00040/6768f447-9803-4281-89fc-ce34acfefe58.jpg?sv=2025-05-05&se=2025-11-30T05%3A10%3A27Z&sr=b&sp=r&sig=mwYc1GxWfx8tnClR9IjQfpu0b464K3IElf4jLmsS9g0%3D",
            "https://storagehotelbooking.blob.core.windows.net/hotel-images/H00040/1de94f87-ca63-4eef-b022-f50e6833ed29.jpg?sv=2025-05-05&se=2025-11-30T05%3A10%3A27Z&sr=b&sp=r&sig=8w3cnetoqjxA4OjcBzJxrPq4wb0E7L7ZPnWDL1m3%2FY0%3D",
            "https://storagehotelbooking.blob.core.windows.net/hotel-images/H00040/43ec9716-d95a-4ea6-a951-2ad82bc26a7a.jpg?sv=2025-05-05&se=2025-11-30T05%3A10%3A27Z&sr=b&sp=r&sig=1lFKk4MmacUdnh%2BVVsxZXjdXa4DDWdHrN9WU%2FeH%2B7aE%3D",

            )

        fun setupClick(textView: TextView, count: Int) {
            textView.setOnClickListener {
                val subList = images.take(count)
                binding.dynamicImageGrid.setImages(subList, true, ImageView.ScaleType.CENTER_CROP)
            }
        }


        binding.dynamicImageGrid.setOnImageClickListener(object : OnImageClickListener {


            override fun onImageClick(
                index: Int,
                imageUrl: Any?,
                allImages: List<Any>?
            ) {
                Log.d("ClickedImageCheck", "Clicked index=$index, url=$imageUrl, total=${allImages?.size}")
            }
        })

        setupClick(binding.one, 1)
        setupClick(binding.two, 2)
        setupClick(binding.three, 3)
        setupClick(binding.four, 4)
        setupClick(binding.five, 5)
        setupClick(binding.six, images.size)

    }


}