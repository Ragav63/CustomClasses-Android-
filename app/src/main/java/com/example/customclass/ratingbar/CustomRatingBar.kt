package com.example.customclass.ratingbar

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.customclass.R
import kotlin.ranges.coerceIn
import kotlin.ranges.until

class CustomRatingBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var numStars: Int = 5
    private var stepSize: Float = 0.5f // We'll handle this manually
    private var currentRating: Float = 0f
    private val starImageViews = mutableListOf<ImageView>()

    // Listener for when the rating changes
    var onRatingChangeListener: ((Float) -> Unit)? = null

    var isReadOnly: Boolean = false
        set(value) {
            field = value
            updateStarClickability() // Update clickability when readOnly state changes
        }

    init {
        orientation = HORIZONTAL
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomRatingBar, defStyleAttr, 0)
        numStars = typedArray.getInt(R.styleable.CustomRatingBar_numStars, 5)
        stepSize = typedArray.getFloat(R.styleable.CustomRatingBar_stepSize, 0.5f) // Read stepSize if provided
        currentRating = typedArray.getFloat(R.styleable.CustomRatingBar_rating, 0f) // Initial rating
        typedArray.recycle()

        setupStars()
        setRating(currentRating) // Set initial rating visually
        updateStarClickability() // Initial setup of clickability
    }

    private fun setupStars() {
        // Clear any existing views if called multiple times (e.g., in editor preview)
        removeAllViews()
        starImageViews.clear()

        // Extension function to convert dp to pixels
        fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

        for (i in 0 until numStars) {
            val starImageView = ImageView(context).apply {
                layoutParams = LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                )
                setImageResource(R.drawable.ic_star_empty)
                setPadding(2.dpToPx(), 2.dpToPx(), 2.dpToPx(), 2.dpToPx())
                adjustViewBounds = true
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
            starImageView.tag = i // Store the index of the star
            starImageView.setOnClickListener {
                onStarClick(it.tag as Int)
            }
            starImageViews.add(starImageView)
            addView(starImageView)
        }
    }

    private fun updateStarClickability() {
        for (imageView in starImageViews) {
            if (isReadOnly) {
                // Remove existing click listener and disable clickability
                imageView.setOnClickListener(null)
                imageView.isClickable = false
                imageView.isFocusable = false
            } else {
                // Set click listener and enable clickability
                imageView.setOnClickListener {
                    onStarClick(it.tag as Int)
                }
                imageView.isClickable = true
                imageView.isFocusable = true
            }
        }
    }

    private fun onStarClick(clickedIndex: Int) {
        // Only proceed if not in read-only mode (redundant if updateStarClickability is correctly called, but good for safety)
        if (isReadOnly) return

        if (clickedIndex == 0) { // Special handling for the first star (index 0)
            currentRating = if (currentRating == 1.0f) 0.0f else 1.0f
        } else { // For stars from index 1 onwards
            val clickedStarFullValue = (clickedIndex + 1).toFloat()
            val clickedStarHalfValue = clickedIndex + 0.5f

            if (currentRating >= clickedStarFullValue) {
                if (currentRating == clickedStarFullValue) {
                    currentRating = clickedStarHalfValue
                } else if (currentRating == clickedStarHalfValue) {
                    currentRating = clickedStarFullValue
                } else {
                    currentRating = clickedStarFullValue
                }

            } else if (currentRating >= clickedStarHalfValue) {
                currentRating = clickedStarFullValue

            } else {
                currentRating = clickedStarHalfValue
            }
        }

        currentRating = currentRating.coerceIn(0f, numStars.toFloat())

        for (i in 0 until clickedIndex) {
            if (currentRating > i + 0.5f && currentRating < i + 1.0f) {
                currentRating = (i + 1).toFloat()
            } else if (currentRating < i + 1.0f) {
                currentRating = (i + 1).toFloat()
            }
        }

        setRating(currentRating)
        onRatingChangeListener?.invoke(currentRating)
    }

    /**
     * Sets the rating visually and updates the internal state.
     */
    fun setRating(rating: Float) {
        if (rating < 0 || rating > numStars) {
            return // Invalid rating
        }
        currentRating = rating

        for (i in 0 until numStars) {
            val imageView = starImageViews[i]
            val starValue = (i + 1).toFloat()

            if (currentRating >= starValue) {
                // Full star
                imageView.setImageResource(R.drawable.ic_star_filled)
            } else if (currentRating >= starValue - 0.5f) {
                // Half star (if currentRating is between X.5 and X.99)
                imageView.setImageResource(R.drawable.ic_star_half)
            } else {
                // Empty star
                imageView.setImageResource(R.drawable.ic_star_empty)
            }
        }
        // No need to notify adapter as this is a custom view.
        // The onRatingChangeListener callback handles updates to the outside.
    }

    /**
     * Gets the current rating.
     */
    fun getRating(): Float {
        return currentRating
    }

    // You can add more public methods here to set/get numStars, stepSize dynamically if needed
}