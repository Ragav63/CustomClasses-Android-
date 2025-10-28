package com.example.customclass.swipebehaviours

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.customclass.R
import kotlin.math.abs

@SuppressLint("ClickableViewAccessibility")
class SwipeToActionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    // Views
    private lateinit var startIcon: ImageView
    private lateinit var centerText: TextView
    private lateinit var endIcon: ImageView
    private lateinit var handleView: View
    private lateinit var trackBackground: View

    // Swipe properties
    private var initialX = 0f
    private var isSwiping = false
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var maxSwipeDistance = 0f
    private var thresholdPercentage = 0.7f

    // Colors and resources
    private var originalBackground = 0
    private var successBackground = 0
    private var direction = SwipeDirection.LEFT_TO_RIGHT
    private var startIconTint = Color.BLACK
    private var endIconTint = Color.BLACK
    private var textColor = Color.BLACK
    private var trackBackgroundRes = 0

    // Listener
    var onSwipeSuccess: (() -> Unit)? = null

    enum class SwipeDirection {
        LEFT_TO_RIGHT, RIGHT_TO_LEFT
    }

    init {
        // Create views programmatically
        createViews()
        setupAttributes(attrs)
        setupTouchHandling()
    }

    @SuppressLint("ResourceType")
    private fun createViews() {
        // Create and add all views programmatically
        trackBackground = View(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
            addView(this)
        }

        startIcon = ImageView(context).apply {
            id = R.id.startIcon
            layoutParams = LayoutParams(
                dpToPx(24),
                dpToPx(24)
            ).apply {
                startToStart = LayoutParams.PARENT_ID
                topToTop = LayoutParams.PARENT_ID
                bottomToBottom = LayoutParams.PARENT_ID
            }
            setImageResource(android.R.drawable.ic_lock_lock)
            addView(this)
        }

        centerText = TextView(context).apply {
            id = R.id.centerText
            layoutParams = LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT
            ).apply {
                startToEnd = R.id.startIcon
                endToStart = R.id.endIcon
                topToTop = LayoutParams.PARENT_ID
                bottomToBottom = LayoutParams.PARENT_ID
            }
            text = "Swipe to complete action"
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(textColor)
            addView(this)
        }

        endIcon = ImageView(context).apply {
            id = R.id.endIcon
            layoutParams = LayoutParams(
                dpToPx(24),
                dpToPx(24)
            ).apply {
                endToEnd = LayoutParams.PARENT_ID
                topToTop = LayoutParams.PARENT_ID
                bottomToBottom = LayoutParams.PARENT_ID
            }
            setImageResource(android.R.drawable.checkbox_on_background)
            alpha = 0f
            visibility = View.VISIBLE
            addView(this)
        }

        handleView = View(context).apply {
            id = R.id.handleView
            layoutParams = LayoutParams(
                dpToPx(40),
                LayoutParams.MATCH_PARENT
            ).apply {
                startToStart = LayoutParams.PARENT_ID
                topToTop = LayoutParams.PARENT_ID
                bottomToBottom = LayoutParams.PARENT_ID
            }
// Create a typed array to resolve the attribute
            val typedArray = context.obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
            background = typedArray.getDrawable(0)
            typedArray.recycle()
            addView(this)
        }

        // Set padding
        setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.SwipeToActionView).apply {
            originalBackground = getResourceId(
                R.styleable.SwipeToActionView_originalBackground,
                ContextCompat.getColor(context, android.R.color.holo_red_light)
            )
            successBackground = getResourceId(
                R.styleable.SwipeToActionView_successBackground,
                ContextCompat.getColor(context, android.R.color.holo_green_light)
            )
            thresholdPercentage = getFloat(
                R.styleable.SwipeToActionView_thresholdPercentage,
                0.7f
            ).coerceIn(0.1f, 1.0f)
            direction = if (getInt(R.styleable.SwipeToActionView_swipeDirection, 0) == 0) {
                SwipeDirection.LEFT_TO_RIGHT
            } else {
                SwipeDirection.RIGHT_TO_LEFT
            }
            startIconTint = getColor(
                R.styleable.SwipeToActionView_startIconTint,
                Color.BLACK
            )
            endIconTint = getColor(
                R.styleable.SwipeToActionView_endIconTint,
                Color.BLACK
            )
            textColor = getColor(
                R.styleable.SwipeToActionView_textColor,
                Color.BLACK
            )
            trackBackgroundRes = getResourceId(
                R.styleable.SwipeToActionView_trackBackground,
                0
            )
            recycle()
        }

        // Apply attributes
        startIcon.setColorFilter(startIconTint)
        endIcon.setColorFilter(endIconTint)
        centerText.setTextColor(textColor)
        if (trackBackgroundRes != 0) {
            trackBackground.background = ContextCompat.getDrawable(context, trackBackgroundRes)
        }
        resetUI()
    }


    private fun setupTouchHandling() {
        handleView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = event.rawX
                    isSwiping = false
                    // Show end icon immediately when touch starts
                    endIcon.isVisible = true
                    endIcon.alpha = 0f
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialX
                    if (!isSwiping && abs(deltaX) > touchSlop) {
                        isSwiping = true
                        // Hide center text when swipe starts
                        centerText.alpha = 0f
                    }

                    if (isSwiping) {
                        updateSwipePosition(deltaX)
                        updateIconVisibility()
                        // Move start icon along with handle
                        startIcon.translationX = handleView.translationX
                        true
                    } else {
                        false
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isSwiping) {
                        handleSwipeCompletion()
                    } else {
                        resetUI()
                    }
                    isSwiping = false
                    true
                }

                else -> false
            }
        }
    }



    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        maxSwipeDistance = when (direction) {
            SwipeDirection.LEFT_TO_RIGHT -> (width - paddingEnd - handleView.width).toFloat()
            SwipeDirection.RIGHT_TO_LEFT -> (width - paddingStart - handleView.width).toFloat()
        }.coerceAtLeast(0f)
    }

    private fun updateSwipePosition(deltaX: Float) {
        handleView.translationX = when (direction) {
            SwipeDirection.LEFT_TO_RIGHT -> deltaX.coerceIn(0f, maxSwipeDistance)
            SwipeDirection.RIGHT_TO_LEFT -> deltaX.coerceIn(-maxSwipeDistance, 0f)
        }
    }

    private fun updateIconVisibility() {
        val progress = when (direction) {
            SwipeDirection.LEFT_TO_RIGHT -> handleView.translationX / maxSwipeDistance
            SwipeDirection.RIGHT_TO_LEFT -> abs(handleView.translationX) / maxSwipeDistance
        }
        // Fade out start icon and fade in end icon
        startIcon.alpha = 1f - progress
        endIcon.alpha = progress
    }



    private fun handleSwipeCompletion() {
        val threshold = maxSwipeDistance * thresholdPercentage
        val distanceMoved = abs(handleView.translationX)

        if (distanceMoved >= threshold) {
            setBackgroundResource(successBackground)
            centerText.text = "Action completed!"
            centerText.alpha = 1f // Show text again
            endIcon.isVisible = false
            startIcon.isVisible = false
            onSwipeSuccess?.invoke()
        } else {
            resetUI()
        }
    }

    fun resetUI() {
        handleView.animate().translationX(0f).withEndAction {
            startIcon.translationX = 0f // Reset start icon position
        }.start()
        setBackgroundResource(originalBackground)
        startIcon.isVisible = true
        startIcon.alpha = 1f
        endIcon.isVisible = true
        endIcon.alpha = 0f
        centerText.alpha = 1f // Make sure text is visible
        centerText.text = when (direction) {
            SwipeDirection.LEFT_TO_RIGHT -> "Swipe right to complete action"
            SwipeDirection.RIGHT_TO_LEFT -> "Swipe left to complete action"
        }
    }

    // Public setters
    fun setStartIcon(resId: Int) = startIcon.setImageResource(resId)
    fun setEndIcon(resId: Int) = endIcon.setImageResource(resId)
    fun setInstructionText(text: String) { centerText.text = text }
    fun setSwipeDirection(direction: SwipeDirection) {
        this.direction = direction
        resetUI()
    }

    fun setStartIconTint(color: Int) {
        startIconTint = color
        startIcon.setColorFilter(color)
    }

    fun setEndIconTint(color: Int) {
        endIconTint = color
        endIcon.setColorFilter(color)
    }

    fun setTextColor(color: Int) {
        textColor = color
        centerText.setTextColor(color)
    }

    fun setTrackBackground(resId: Int) {
        trackBackgroundRes = resId
        trackBackground.background = ContextCompat.getDrawable(context, resId)
    }
}