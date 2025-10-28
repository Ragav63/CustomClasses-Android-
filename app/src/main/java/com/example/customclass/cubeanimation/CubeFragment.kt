package com.example.customclass.cubeanimation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.customclass.R
import com.example.customclass.databinding.FragmentCubeBinding


class CubeFragment : Fragment() {

    private var _binding : FragmentCubeBinding?=null
    private val binding get() = _binding!!



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCubeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        
        val imgs = listOf(
           R.drawable.hotel1,
           R.drawable.hotel2,
           R.drawable.hotel3,
           R.drawable.hotel4,
        )

        binding.cubeSlider.setImages(imgs)
        binding.cubeSlider.setAutoSlide(true) // Enable auto sliding
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.cubeSlider.cleanup() // Important to prevent memory leaks
    }

    // Handle swipe events from your touch listener
    fun handleSwipeLeft() {
        binding.cubeSlider.onSwipeLeft()
    }

    fun handleSwipeRight() {
        binding.cubeSlider.onSwipeRight()
    }
}