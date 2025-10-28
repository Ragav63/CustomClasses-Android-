package com.example.customclass.panorama

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.example.customclass.R
import com.example.customclass.databinding.FragmentPanaromaViewBinding
import com.panoramagl.PLImage
import com.panoramagl.PLManager
import com.panoramagl.PLSphericalPanorama
import com.panoramagl.utils.PLUtils


class PanaromaViewFragment : Fragment() {

    private var _binding : FragmentPanaromaViewBinding?=null
    private val binding get() = _binding!!

    private lateinit var plManager: PLManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPanaromaViewBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializePlManager()

        val panorama = PLSphericalPanorama().apply {
            setImage(PLImage(PLUtils.getBitmap(requireContext(), R.drawable.panaromaimg)))
            // Set initial camera position
            camera.lookAt(0f, 0f, 0f)
        }
        plManager.panorama = panorama

        binding.container.setOnTouchListener { _, event ->
            plManager.onTouchEvent(event)
            true
        }
    }

    private fun initializePlManager(){
        plManager = PLManager(requireContext()).apply {
            setContentView(binding.container)
            onCreate()
            isScrollingEnabled = true
            isAccelerometerEnabled = true
            isZoomEnabled = true
            isInertiaEnabled = true
            isAcceleratedTouchScrollingEnabled = false
        }
    }


    fun onTouchEvent(event: MotionEvent?): Boolean {
        return plManager.onTouchEvent(event!!)
    }

    override fun onResume() {
        super.onResume()
        plManager.onResume()
    }

    override fun onPause() {
        plManager.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        plManager.onDestroy()      // Clean up GL resources
        super.onDestroy()
        plManager.onDestroy()
    }
}