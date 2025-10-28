package com.example.customclass.cubeanimation

import android.view.animation.Animation
import android.view.animation.Transformation
import android.graphics.Camera
import android.graphics.Matrix

class CubeAnimation private constructor(
    private val direction: Int,
    private val enter: Boolean,
    duration: Long
) : Animation() {

    init {
        setDuration(duration)
    }

    companion object {
        const val LEFT = 3
        const val RIGHT = 4

        fun create(direction: Int, enter: Boolean, duration: Long): CubeAnimation {
            return CubeAnimation(direction, enter, duration)
        }
    }

    private val camera = Camera()
    private var width = 0
    private var height = 0
    // These variables are now used for manual matrix translation
    private var pivotXValue = 0f
    private var pivotYValue = 0f

    override fun initialize(width: Int, height: Int, parentWidth: Int, parentHeight: Int) {
        super.initialize(width, height, parentWidth, parentHeight)
        this.width = width
        this.height = height

        // Calculate pivot values once in initialization
        initializeHorizontal()

        // Use a better Z location for perspective
        camera.setLocation(0f, 0f, -width * 0.02f)
    }

    private fun initializeHorizontal() {
        // Pivot for cube animation: must be on the edge for rotation
        pivotXValue = if (enter == (direction == LEFT)) 0.0f else width.toFloat()
        pivotYValue = height * 0.5f

        // NO setPivotX/Y call here.
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        val matrix: Matrix = t.matrix
        camera.save()

        val degreeStart = if (enter) 90.0f else 0.0f
        val degreeEnd = if (enter) 0.0f else -90.0f

        val degrees = degreeStart + ((degreeEnd - degreeStart) * interpolatedTime)

        val adjustedDegrees = when (direction) {
            LEFT -> degrees // 90 -> 0 (enter), 0 -> -90 (exit)
            RIGHT -> -degrees // -90 -> 0 (enter), 0 -> 90 (exit)
            else -> 0f
        }

        // 1. Prepare for pivot-based rotation: Translate origin to pivot point (edge).
        matrix.preTranslate(-pivotXValue, -pivotYValue)

        // 2. APPLY ROTATION
        camera.rotateY(adjustedDegrees)
        camera.getMatrix(matrix)

        // 3. Translate origin back. This performs the rotation around the edge.
        matrix.postTranslate(pivotXValue, pivotYValue)


        // 🔑 FINAL, EXTREME ALIGNMENT FIX: Adjust Z-translation (depth) for entering view
        // and X-translation (slide) for exiting view.

        if (enter) {
            // ENTERING VIEW: The rotation should handle most of the X-translation.
            // We just need to ensure the Z depth aligns.
            camera.translate(0f, 0f, (1.0f - interpolatedTime) * -width) // Translate along Z axis
            camera.getMatrix(matrix)

        } else {
            // EXITING VIEW: Needs explicit X-translation to ensure it slides off while rotating.
            val translationX = when (direction) {
                LEFT -> interpolatedTime * -width
                RIGHT -> interpolatedTime * width
                else -> 0f
            }
            matrix.postTranslate(translationX, 0f)
        }

        camera.restore()
    }
}