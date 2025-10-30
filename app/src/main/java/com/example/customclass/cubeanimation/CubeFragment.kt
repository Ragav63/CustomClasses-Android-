package com.example.customclass.cubeanimation

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.core.os.postDelayed
import androidx.navigation.fragment.findNavController
import com.example.customclass.R
import com.example.customclass.databinding.FragmentCubeBinding
import com.google.android.material.snackbar.Snackbar
import java.util.Calendar
import kotlin.text.toInt


class CubeFragment : Fragment()  {

    private var _binding : FragmentCubeBinding?=null
    private val binding get() = _binding!!

    private var currentIndex = 0

    private var isUserLoggedIn = false

    private var isCubeAnimationActive = false

    // Add this class variable
    private var lastSwipeTime = 0L
    private val SWIPE_DEBOUNCE_DELAY = 500L // Minimum time between swipes

    companion object {
        const val CUBE = 2
        const val LEFT = 3
        const val RIGHT = 4
        private const val DURATION = 500L
        private const val AUTO_SLIDE_INTERVAL = 3000L


    }

    private var gestureDetector: GestureDetector? = null


    private val imageResources = arrayOf(
        R.drawable.onebanner,
        R.drawable.banner1,
        R.drawable.banner2,
        R.drawable.banner3
    )

    private var currentImageIndex = 0
    private var isForwardSequence = true
    private val handler = Handler(Looper.getMainLooper())
    private val autoSlideRunnable = object : Runnable {
        override fun run() {
            onAutoSlide()
            handler.postDelayed(this, AUTO_SLIDE_INTERVAL)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCubeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize gesture detector FIRST
        setupGestureDetector()

        onAnim()
        val userId = ""
        if (userId.isNullOrEmpty()) {
            // reset margin → centered as per XML
            setupLoggedOutCubeAnimation()
        } else {
            setupLoggedInUi("John")
        }



    }

    private fun setupGestureDetector() {
        val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onDown(e: MotionEvent): Boolean {
                Log.d("SWIPE_DEBUG", "onDown event")
                return true
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false

                Log.d("SWIPE_DEBUG", "onFling called: vX=$velocityX, vY=$velocityY")

                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y

                if (Math.abs(diffX) > Math.abs(diffY) &&
                    Math.abs(diffX) > SWIPE_THRESHOLD &&
                    Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD
                ) {
                    if (diffX > 0) {
                        Log.d("SWIPE_DEBUG", "Right swipe detected")
                        onSwipeRight()
                        return true
                    } else {
                        Log.d("SWIPE_DEBUG", "Left swipe detected")
                        onSwipeLeft()
                        return true
                    }
                }
                Log.d("SWIPE_DEBUG", "Swipe not detected or too weak")
                return false
            }
        }

        gestureDetector = GestureDetector(requireContext(), gestureListener)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun onAnim() {
        currentImageIndex = arguments?.getInt("imageIndex") ?: 0
        binding.mainImageView.setImageResource(imageResources[currentImageIndex])


        var initialX = 0f
        var initialY = 0f
        var isSwiping = false

        binding.cubeAnimationContainer.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = event.x
                    initialY = event.y
                    isSwiping = false
                    // Disable scroll view to give our container priority
                    binding.nestedScrollView.requestDisallowInterceptTouchEvent(true)
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.x - initialX
                    val deltaY = event.y - initialY

                    // Check if this is a horizontal swipe (not a scroll)
                    if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > 20) {
                        isSwiping = true
                        // Keep scroll view disabled for swipes
                        binding.nestedScrollView.requestDisallowInterceptTouchEvent(true)
                    } else if (Math.abs(deltaY) > Math.abs(deltaX) && Math.abs(deltaY) > 20) {
                        // Vertical movement - allow scroll view to handle it
                        binding.nestedScrollView.requestDisallowInterceptTouchEvent(false)
                        return@setOnTouchListener false
                    }
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_UP -> {
                    binding.nestedScrollView.requestDisallowInterceptTouchEvent(false)

                    if (isSwiping) {
                        val finalX = event.x
                        val deltaX = finalX - initialX

                        // Determine swipe direction
                        if (Math.abs(deltaX) > 100) { // Minimum swipe distance
                            if (deltaX > 0) {
                                onSwipeRight()
                            } else {
                                onSwipeLeft()
                            }
                        }
                        isSwiping = false
                        return@setOnTouchListener true
                    }
                    return@setOnTouchListener false
                }
                MotionEvent.ACTION_CANCEL -> {
                    binding.nestedScrollView.requestDisallowInterceptTouchEvent(false)
                    isSwiping = false
                    return@setOnTouchListener false
                }
            }
            false
        }
    }

    private fun onSwipeLeft() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSwipeTime < SWIPE_DEBOUNCE_DELAY) {
            Log.d("SWIPE_DEBUG", "Swipe ignored - too fast")
            return
        }
        lastSwipeTime = currentTime

        handler.removeCallbacks(autoSlideRunnable)
        if (currentImageIndex < imageResources.size - 1) {
            startAnimation(currentImageIndex + 1, LEFT)
        }
        handler.postDelayed(autoSlideRunnable, AUTO_SLIDE_INTERVAL)
    }

    private fun onSwipeRight() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSwipeTime < SWIPE_DEBOUNCE_DELAY) {
            Log.d("SWIPE_DEBUG", "Swipe ignored - too fast")
            return
        }
        lastSwipeTime = currentTime

        handler.removeCallbacks(autoSlideRunnable)
        if (currentImageIndex > 0) {
            startAnimation(currentImageIndex - 1, RIGHT)
        }
        handler.postDelayed(autoSlideRunnable, AUTO_SLIDE_INTERVAL)
    }


    private fun onAutoSlide() {
        if (isForwardSequence) {
            if (currentImageIndex < imageResources.size - 1) {
                startAnimation(currentImageIndex + 1, LEFT
                )
            } else {
                isForwardSequence = false
                startAnimation(currentImageIndex - 1, RIGHT
                )
            }
        } else {
            if (currentImageIndex > 0) {
                startAnimation(currentImageIndex - 1, RIGHT
                )
            } else {
                isForwardSequence = true
                startAnimation(currentImageIndex + 1, LEFT
                )
            }
        }
    }



    private fun getGreetingMessage(context: Context): String {
        val language = "en" // your function
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        val greetingEn = when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }

        val greetingAr = when (hour) {
            in 0..11 -> "صباح الخير"
            in 12..16 -> "مساء الخير"
            in 17..20 -> "مساء الخير"
            else -> "تصبح على خير"
        }

        return if (language == "en") greetingEn else greetingAr
    }


    private fun setupLoggedOutCubeAnimation() {
        // Show the first image with login button
        binding.mainImageView.setImageResource(imageResources[0])

        // Make the login button visible and clickable
        binding.loginOverlayButton.visibility = View.VISIBLE
        binding.loginOverlayButton.setOnClickListener {
            // Navigate to the mobile confirmation fragment when the button is clicked
            findNavController().navigate(R.id.dynamicImageFragment)
        }

        // Disable cube animation until login
        binding.cubeAnimationContainer.setOnClickListener(null)
        isCubeAnimationActive = false
    }

    private fun setupLoggedInCubeAnimation(userName: String) {
        // Hide login button, show greeting
        binding.loginOverlayButton.visibility = View.GONE
        binding.greetingTextView.visibility=View.VISIBLE
        showGreeting(userName)

        // Start with first image (without login button overlay)
        binding.mainImageView.setImageResource(imageResources[0])

        // Enable cube animation
        setupCubeAnimation()
        isCubeAnimationActive = true
    }

    private fun setupLoggedInUi(userName: String) {
        isUserLoggedIn = true  // ✅ mark as logged in
        binding.loginOverlayButton.visibility = View.GONE
        binding.nextLoginButton.visibility = View.GONE
        showGreeting(userName)
        binding.greetingTextView.bringToFront()
        setupLoggedInCubeAnimation(userName)
    }

    private fun showGreeting(userName: String) {
        // Hide the login button and make the greeting TextView visible
        binding.loginOverlayButton.visibility = View.GONE
        binding.greetingTextView.visibility = View.VISIBLE

        // Get the current hour to determine the greeting
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)



        val greetingText = "${getGreetingMessage(requireContext())} ${userName.orEmpty().trim()}"

        // Set the greeting text for both the main and next views
        binding.greetingTextView.text = greetingText
        binding.greetingTextView1.text = greetingText


    }

    private fun setupCubeAnimation() {
        binding.cubeAnimationContainer.setOnClickListener {
            animateToNextImage()
        }
    }

    private fun animateToNextImage() {
        if (!isCubeAnimationActive) return

        val nextIndex = (currentImageIndex + 1) % imageResources.size
        binding.nextImageView.setImageResource(imageResources[nextIndex])

        // Cube animation logic
        binding.nextImageView.visibility = View.VISIBLE
        binding.nextImageView.rotationY = -90f

        val animator = ObjectAnimator.ofFloat(binding.nextImageView, "rotationY", -90f, 0f)
        animator.duration = 500
        animator.start()

        val currentAnimator = ObjectAnimator.ofFloat(binding.mainImageView, "rotationY", 0f, 90f)
        currentAnimator.duration = 500
        currentAnimator.start()

        currentImageIndex = nextIndex

        Handler(Looper.getMainLooper()).postDelayed({
            binding.mainImageView.setImageResource(imageResources[nextIndex])
            binding.mainImageView.rotationY = 0f
            binding.nextImageView.visibility = View.INVISIBLE

            // ✅ Make sure login button stays hidden if user is logged in
            if (isUserLoggedIn) {
                binding.loginOverlayButton.visibility = View.GONE
            }
        }, 500)
    }

    private fun startAnimation(newIndex: Int, direction: Int) {
        // Get the current and next containers
        val outContainer = binding.mainContentContainer
        val inContainer = binding.nextContentContainer

        // Get the views inside the containers
        val outView = binding.mainImageView
        val inView = binding.nextImageView
        val outButton = binding.loginOverlayButton
        val inButton = binding.nextLoginButton
        val outGreeting = binding.greetingTextView
        val inGreeting = binding.greetingTextView1 // Use the other TextView for the incoming view

        // Clear any previous animations
        outContainer.clearAnimation()
        inContainer.clearAnimation()

        // Set the image and visibility for the incoming view
        inView.setImageResource(imageResources[newIndex])
        inContainer.visibility = View.VISIBLE

        // Manage visibility for the INCOMING views
        if (newIndex == 0) {
            if (isUserLoggedIn) {
                inButton.visibility = View.GONE
                inGreeting.visibility = View.VISIBLE
                // Set the actual greeting text here
                // inGreeting.text = "Hello, ${userName}!"
            } else {
                inButton.visibility = View.VISIBLE
                inGreeting.visibility = View.GONE
            }
        } else {
            inButton.visibility = View.GONE
            inGreeting.visibility = View.GONE
        }

        val outAnimation = CubeAnimation.create(direction, false, DURATION)
        val inAnimation = CubeAnimation.create(direction, true, DURATION)

        outAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                // After the animation, switch the content of the "main" container
                outView.setImageResource(imageResources[newIndex])

                // Manage visibility for the OUTGOING views (now the "main" views)
                if (newIndex == 0) {
                    if (isUserLoggedIn) {
                        outButton.visibility = View.GONE
                        outGreeting.visibility = View.VISIBLE
                    } else {
                        outButton.visibility = View.VISIBLE
                        outGreeting.visibility = View.GONE
                    }
                } else {
                    outButton.visibility = View.GONE
                    outGreeting.visibility = View.GONE
                }

                // Hide the "next" container
                inContainer.visibility = View.INVISIBLE

                // Update the current image index
                currentImageIndex = newIndex
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        // Start the animation on the containers
        outContainer.startAnimation(outAnimation)
        inContainer.startAnimation(inAnimation)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}