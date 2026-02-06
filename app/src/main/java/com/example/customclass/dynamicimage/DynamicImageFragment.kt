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
            R.drawable.hotel1,
            R.drawable.hotel2,
            R.drawable.hotel3,
            R.drawable.hotel4,
            )

        fun setupClick(textView: TextView, count: Int) {
            textView.setOnClickListener {
                val subList = images.take(count)
                binding.dynamicImageGrid.setImages(subList, true, ImageView.ScaleType.CENTER_CROP,"Values")
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