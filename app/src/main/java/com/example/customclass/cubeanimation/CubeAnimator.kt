package com.example.customclass.cubeanimation

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.ImageView
import androidx.core.widget.NestedScrollView
import kotlin.math.abs

class CubeAnimator(
    private val context: Context,
    private val mainView: ImageView,
    private val nextView: ImageView,
    private val mainContainer: View,
    private val nextContainer: View,
    private val imageResources: Array<Int>,
    // optional overlay views (pass null if you don't use them)
    private val mainLoginButton: View? = null,
    private val nextLoginButton: View? = null,
    private val mainGreetingView: View? = null,
    private val nextGreetingView: View? = null,
    private val nestedScrollView: NestedScrollView? = null,
    private val onImageChange: ((Int) -> Unit)? = null
) {

    companion object {
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
        private const val AUTO_SLIDE_INTERVAL = 3000L
        private const val SWIPE_DEBOUNCE_DELAY = 400L
        private const val DURATION = 500L
    }

    private var currentIndex = 0
    private var isForward = true
    private var lastSwipeTime = 0L
    private var isUserLoggedIn = false

    private val handler = Handler(Looper.getMainLooper())

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean = true
        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (e1 == null || e2 == null) return false
            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y
            if (abs(diffX) > abs(diffY) &&
                abs(diffX) > SWIPE_THRESHOLD &&
                abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) onSwipeRight() else onSwipeLeft()
                return true
            }
            return false
        }
    })

    private val autoSlideRunnable = object : Runnable {
        override fun run() {
            autoSlide()
            handler.postDelayed(this, AUTO_SLIDE_INTERVAL)
        }
    }

    // Public API
    fun attachTo(container: ViewGroup) {
        // initialize main image
        if (imageResources.isNotEmpty()) {
            mainView.setImageResource(imageResources[currentIndex])
            updateOverlaysForIndex(currentIndex) // ensure overlays initial state
        }
        container.setOnTouchListener { _, event -> handleTouch(event) }
        startAutoSlide()
    }

    fun stop() {
        handler.removeCallbacks(autoSlideRunnable)
    }

    fun setUserLoggedIn(loggedIn: Boolean) {
        isUserLoggedIn = loggedIn
        updateOverlaysForIndex(currentIndex)
    }

    private fun updateOverlaysForIndex(index: Int) {
        // Hide all overlays first
        resetAllOverlays()

        // Show appropriate overlays based on index and login state
        if (index == 0) {
            if (isUserLoggedIn) {
                mainGreetingView?.visibility = View.VISIBLE
                mainLoginButton?.visibility = View.GONE
            } else {
                mainGreetingView?.visibility = View.GONE
                mainLoginButton?.visibility = View.VISIBLE
            }
        } else {
            // For all other slides, hide both greeting and button
            mainGreetingView?.visibility = View.GONE
            mainLoginButton?.visibility = View.GONE
        }
    }


    fun getCurrentIndex(): Int = currentIndex

    // Touch & nestedScroll handling
    private var initialX = 0f
    private var initialY = 0f
    private var isSwiping = false

    private fun handleTouch(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                initialX = event.x
                initialY = event.y
                isSwiping = false
                nestedScrollView?.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - initialX
                val dy = event.y - initialY
                if (abs(dx) > abs(dy) && abs(dx) > 20) {
                    isSwiping = true
                    nestedScrollView?.requestDisallowInterceptTouchEvent(true)
                } else if (abs(dy) > abs(dx) && abs(dy) > 20) {
                    nestedScrollView?.requestDisallowInterceptTouchEvent(false)
                    return false
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                nestedScrollView?.requestDisallowInterceptTouchEvent(false)
                if (isSwiping) {
                    val dx = event.x - initialX
                    if (abs(dx) > SWIPE_THRESHOLD) {
                        if (dx > 0) onSwipeRight() else onSwipeLeft()
                    }
                    isSwiping = false
                    return true
                }
                return false
            }
            MotionEvent.ACTION_CANCEL -> {
                nestedScrollView?.requestDisallowInterceptTouchEvent(false)
                isSwiping = false
                return false
            }
        }
        return false
    }

    // Swipe handlers
    private fun onSwipeLeft() {
        val now = System.currentTimeMillis()
        if (now - lastSwipeTime < SWIPE_DEBOUNCE_DELAY) return
        lastSwipeTime = now
        handler.removeCallbacks(autoSlideRunnable)
        if (currentIndex < imageResources.size - 1) {
            startAnimation(currentIndex + 1, CubeAnimation.LEFT)
        } else {
            // optionally loop to 0 — comment out if you want bounce behavior
            // startAnimation(0, CubeAnimation.LEFT)
        }
        startAutoSlide()
    }

    private fun onSwipeRight() {
        val now = System.currentTimeMillis()
        if (now - lastSwipeTime < SWIPE_DEBOUNCE_DELAY) return
        lastSwipeTime = now
        handler.removeCallbacks(autoSlideRunnable)
        if (currentIndex > 0) {
            startAnimation(currentIndex - 1, CubeAnimation.RIGHT)
        } else {
            // optionally loop to last
            // startAnimation(imageResources.size - 1, CubeAnimation.RIGHT)
        }
        startAutoSlide()
    }

    private fun autoSlide() {
        if (isForward) {
            if (currentIndex < imageResources.size - 1) {
                startAnimation(currentIndex + 1, CubeAnimation.LEFT)
            } else {
                isForward = false
                startAnimation(currentIndex - 1, CubeAnimation.RIGHT)
            }
        } else {
            if (currentIndex > 0) {
                startAnimation(currentIndex - 1, CubeAnimation.RIGHT)
            } else {
                isForward = true
                startAnimation(currentIndex + 1, CubeAnimation.LEFT)
            }
        }
    }

    private fun startAnimation(newIndex: Int, direction: Int) {
        if (newIndex == currentIndex) return
        if (newIndex !in imageResources.indices) return

        resetAllOverlays()

        // Set next image and prepare container
        nextView.setImageResource(imageResources[newIndex])
        nextContainer.visibility = View.VISIBLE

        // ✅ Apply correct overlay state *immediately* for next container
        applyOverlayStateForIndex(
            index = newIndex,
            greetingView = nextGreetingView,
            loginButton = nextLoginButton
        )

        val outAnim = CubeAnimation.create(direction, false, DURATION)
        val inAnim = CubeAnimation.create(direction, true, DURATION)

        outAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                // ✅ Keep outgoing overlays consistent during animation
                applyOverlayStateForIndex(
                    index = currentIndex,
                    greetingView = mainGreetingView,
                    loginButton = mainLoginButton
                )
            }

            override fun onAnimationEnd(animation: Animation?) {
                // Finalize swap
                mainView.setImageResource(imageResources[newIndex])
                applyOverlayStateForIndex(
                    index = newIndex,
                    greetingView = mainGreetingView,
                    loginButton = mainLoginButton
                )

                nextContainer.visibility = View.INVISIBLE
                currentIndex = newIndex
                onImageChange?.invoke(newIndex)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        mainContainer.startAnimation(outAnim)
        nextContainer.startAnimation(inAnim)
    }

    private fun applyOverlayStateForIndex(index: Int, greetingView: View?, loginButton: View?) {
        greetingView?.visibility = View.GONE
        loginButton?.visibility = View.GONE

        if (index == 0) {
            if (isUserLoggedIn) {
                greetingView?.visibility = View.VISIBLE
            } else {
                loginButton?.visibility = View.VISIBLE
            }
        }
    }


    private fun resetAllOverlays() {
        mainLoginButton?.visibility = View.GONE
        nextLoginButton?.visibility = View.GONE
        mainGreetingView?.visibility = View.GONE
        nextGreetingView?.visibility = View.GONE
    }

    private fun startAutoSlide() {
        handler.removeCallbacks(autoSlideRunnable)
        handler.postDelayed(autoSlideRunnable, AUTO_SLIDE_INTERVAL)
    }
}
