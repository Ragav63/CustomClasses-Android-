package com.example.customclass.cubeanimation

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.customclass.R
import com.google.android.material.card.MaterialCardView

class CubeBannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // Views
    private lateinit var cubeAnimationContainer: FrameLayout
    private lateinit var mainContentContainer: MaterialCardView
    private lateinit var nextContentContainer: MaterialCardView
    private lateinit var mainImageView: ImageView
    private lateinit var nextImageView: ImageView
    private lateinit var loginOverlayButton: Button
    private lateinit var nextLoginButton: Button
    private lateinit var greetingTextView: TextView
    private lateinit var greetingTextView1: TextView

    // Configuration
    private lateinit var bannerManager: BannerAnimationManager
    private var eventListeners: BannerEventListeners? = null

    // Customizable properties
    var bannerMarginTop: Int = 10
        set(value) {
            field = value
            updateLayoutParams()
        }

    var bannerMarginHorizontal: Int = 15
        set(value) {
            field = value
            updateLayoutParams()
        }

    var bannerHeight: Int = 200
        set(value) {
            field = value
            updateLayoutParams()
        }

    var cornerRadius: Float = 20f
        set(value) {
            field = value
            updateCardViewRadius()
        }

    var buttonText: String = "Login"
        set(value) {
            field = value
            updateButtonText()
        }

    var buttonTextColor: Int = Color.WHITE
        set(value) {
            field = value
            updateButtonAppearance()
        }

    var buttonBackgroundColor: Int = Color.BLACK
        set(value) {
            field = value
            updateButtonAppearance()
        }

    var buttonTextSize: Float = 16f
        set(value) {
            field = value
            updateButtonAppearance()
        }

    var greetingTextSize: Float = 16f
        set(value) {
            field = value
            updateGreetingAppearance()
        }

    var greetingTextColor: Int = Color.BLACK
        set(value) {
            field = value
            updateGreetingAppearance()
        }

    var isButtonAllCaps: Boolean = false
        set(value) {
            field = value
            updateButtonAppearance()
        }

    var isButtonBold: Boolean = true
        set(value) {
            field = value
            updateButtonAppearance()
        }

    init {
        initView(context)
        setupAttributes(attrs)
    }

    private fun initView(context: Context) {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_cube_banner, this, true)

        // Initialize views
        cubeAnimationContainer = view.findViewById(R.id.cubeAnimationContainer)
        mainContentContainer = view.findViewById(R.id.mainContentContainer)
        nextContentContainer = view.findViewById(R.id.nextContentContainer)
        mainImageView = view.findViewById(R.id.mainImageView)
        nextImageView = view.findViewById(R.id.nextImageView)
        loginOverlayButton = view.findViewById(R.id.loginOverlayButton)
        nextLoginButton = view.findViewById(R.id.nextLoginButton)
        greetingTextView = view.findViewById(R.id.greetingTextView)
        greetingTextView1 = view.findViewById(R.id.greetingTextView1)

        // Set default properties
        updateLayoutParams()
        updateCardViewRadius()
        updateButtonAppearance()
        updateGreetingAppearance()
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.CubeBannerView)

            // Banner layout attributes
            bannerMarginTop = typedArray.getDimensionPixelSize(
                R.styleable.CubeBannerView_bannerMarginTop, 10
            )
            bannerMarginHorizontal = typedArray.getDimensionPixelSize(
                R.styleable.CubeBannerView_bannerMarginHorizontal, 15
            )
            bannerHeight = typedArray.getDimensionPixelSize(
                R.styleable.CubeBannerView_bannerHeight, 200
            )
            cornerRadius = typedArray.getDimension(
                R.styleable.CubeBannerView_cornerRadius, 20f
            )

            // Button attributes
            buttonText = typedArray.getString(R.styleable.CubeBannerView_buttonText) ?: "Login"
            buttonTextColor = typedArray.getColor(
                R.styleable.CubeBannerView_buttonTextColor, Color.WHITE
            )
            buttonBackgroundColor = typedArray.getColor(
                R.styleable.CubeBannerView_buttonBackgroundColor, Color.BLACK
            )
            buttonTextSize = typedArray.getDimension(
                R.styleable.CubeBannerView_buttonTextSize, 16f
            )
            isButtonAllCaps = typedArray.getBoolean(
                R.styleable.CubeBannerView_buttonAllCaps, false
            )
            isButtonBold = typedArray.getBoolean(
                R.styleable.CubeBannerView_buttonBold, true
            )

            // Greeting attributes
            greetingTextSize = typedArray.getDimension(
                R.styleable.CubeBannerView_greetingTextSize, 16f
            )
            greetingTextColor = typedArray.getColor(
                R.styleable.CubeBannerView_greetingTextColor, Color.BLACK
            )

            typedArray.recycle()
        }
    }

    private fun updateLayoutParams() {
        val layoutParams = cubeAnimationContainer.layoutParams as MarginLayoutParams
        layoutParams.topMargin = bannerMarginTop
        layoutParams.marginStart = bannerMarginHorizontal
        layoutParams.marginEnd = bannerMarginHorizontal
        layoutParams.height = bannerHeight
        cubeAnimationContainer.layoutParams = layoutParams
    }

    private fun updateCardViewRadius() {
        mainContentContainer.radius = cornerRadius
        nextContentContainer.radius = cornerRadius
    }

    private fun updateButtonText() {
        loginOverlayButton.text = buttonText
        nextLoginButton.text = buttonText
    }

    private fun updateButtonAppearance() {
        // Text appearance
        loginOverlayButton.textSize = buttonTextSize / resources.displayMetrics.density
        nextLoginButton.textSize = buttonTextSize / resources.displayMetrics.density

        loginOverlayButton.setTextColor(buttonTextColor)
        nextLoginButton.setTextColor(buttonTextColor)

        loginOverlayButton.isAllCaps = isButtonAllCaps
        nextLoginButton.isAllCaps = isButtonAllCaps

        if (isButtonBold) {
            loginOverlayButton.setTypeface(null, android.graphics.Typeface.BOLD)
            nextLoginButton.setTypeface(null, android.graphics.Typeface.BOLD)
        } else {
            loginOverlayButton.setTypeface(null, android.graphics.Typeface.NORMAL)
            nextLoginButton.setTypeface(null, android.graphics.Typeface.NORMAL)
        }

        // Background color
        loginOverlayButton.setBackgroundColor(buttonBackgroundColor)
        nextLoginButton.setBackgroundColor(buttonBackgroundColor)
    }

    private fun updateGreetingAppearance() {
        greetingTextView.textSize = greetingTextSize / resources.displayMetrics.density
        greetingTextView1.textSize = greetingTextSize / resources.displayMetrics.density

        greetingTextView.setTextColor(greetingTextColor)
        greetingTextView1.setTextColor(greetingTextColor)

        greetingTextView.setTypeface(null, android.graphics.Typeface.BOLD)
        greetingTextView1.setTypeface(null, android.graphics.Typeface.BOLD)
    }

    // In CubeBannerView class, update the initialize method:
    fun initialize(
        imageResources: List<Int>,
        eventListeners: BannerEventListeners,
        config: BannerAnimationManager.BannerConfig = BannerAnimationManager.BannerConfig(
            imageResources = imageResources,
            autoSlideInterval = 3000L,
            swipeDebounceDelay = 500L,
            animationDuration = 500L,
            cubeAnimationEnabled = true
        )
    ) {
        this.eventListeners = eventListeners

        val bannerViews = object : BannerAnimationManager.BannerViews {
            override val mainImageView: ImageView get() = this@CubeBannerView.mainImageView
            override val nextImageView: ImageView get() = this@CubeBannerView.nextImageView
            override val cubeAnimationContainer: FrameLayout get() = this@CubeBannerView.cubeAnimationContainer
            override val mainContentContainer: MaterialCardView get() = this@CubeBannerView.mainContentContainer
            override val nextContentContainer: MaterialCardView get() = this@CubeBannerView.nextContentContainer
            override val loginOverlayButton: Button get() = this@CubeBannerView.loginOverlayButton
            override val nextLoginButton: Button get() = this@CubeBannerView.nextLoginButton
            override val greetingTextView: TextView get() = this@CubeBannerView.greetingTextView
            override val greetingTextView1: TextView get() = this@CubeBannerView.greetingTextView1
            override val nestedScrollView: androidx.core.widget.NestedScrollView? = null
        }

        bannerManager = BannerAnimationManager(context, config, eventListeners)
        bannerManager.initialize(bannerViews)

        // Force initial view setup
        bannerManager.setCurrentIndex(0)
    }

    fun updateLoginState(isLoggedIn: Boolean, userName: String? = null, userGender: String? = null) {
        bannerManager.updateLoginState(isLoggedIn, userName, userGender)
    }

    fun updateImages(newImageResources: List<Int>) {
        bannerManager.updateImages(newImageResources)
    }

    fun setCurrentIndex(index: Int) {
        bannerManager.setCurrentIndex(index)
    }

    fun pauseAutoSlide() {
        bannerManager.pauseAutoSlide()
    }

    fun resumeAutoSlide() {
        bannerManager.resumeAutoSlide()
    }

    fun cleanup() {
        bannerManager.cleanup()
    }

    // Direct view access methods
    fun getMainImageView(): ImageView = mainImageView
    fun getNextImageView(): ImageView = nextImageView
    fun getLoginButton(): Button = loginOverlayButton
    fun getGreetingTextView(): TextView = greetingTextView

    // Convenience methods for quick setup
    fun setupWithImages(
        images: List<Int>,
        onLoginClick: () -> Unit,
        onBannerClick: (Int) -> Unit = {},
        onSwipe: (Int, Int) -> Unit = { _, _ -> }
    ) {
        val listeners = object : BannerEventListeners {
            override fun onLoginButtonClicked() = onLoginClick()
            override fun onBannerImageClicked(index: Int) = onBannerClick(index)
            override fun onSwipeDetected(direction: Int, currentIndex: Int) = onSwipe(direction, currentIndex)
        }
        initialize(images, listeners)
    }
}