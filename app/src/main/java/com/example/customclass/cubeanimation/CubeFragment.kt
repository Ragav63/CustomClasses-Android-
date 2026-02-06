package com.example.customclass.cubeanimation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.customclass.R
import com.example.customclass.databinding.FragmentCubeBinding

class CubeFragment : Fragment() {

    private var _binding: FragmentCubeBinding? = null
    private val binding get() = _binding!!

    private lateinit var cubeAnimator: CubeAnimator

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCubeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val imageResources = arrayOf(
            R.drawable.onebanner,
            R.drawable.banner1,
            R.drawable.banner2,
            R.drawable.banner3
        )

        cubeAnimator = CubeAnimator(
            context = requireContext(),
            mainView = binding.mainImageView,
            nextView = binding.nextImageView,
            mainContainer = binding.mainContentContainer,
            nextContainer = binding.nextContentContainer,
            imageResources = imageResources,
            mainLoginButton = binding.loginOverlayButton,
            nextLoginButton = binding.nextLoginButton,
            mainGreetingView = binding.greetingTextView,
            nextGreetingView = binding.greetingTextView1,
            nestedScrollView = binding.nestedScrollView
        )
        cubeAnimator.setUserLoggedIn(false) // or true

        cubeAnimator.attachTo(binding.cubeAnimationContainer)
    }

    override fun onDestroyView() {
        cubeAnimator.stop()
        _binding = null
        super.onDestroyView()
    }
}
