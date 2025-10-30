package com.example.customclass.cubeanimation

import android.graphics.Camera
import android.os.Build
import android.view.animation.Animation
import android.view.animation.Transformation

class CubeAnimation private constructor(
    private val direction: Int,
    private val enter: Boolean
) : Animation() {

    companion object {
        const val UP = 1
        const val DOWN = 2
        const val LEFT = 3
        const val RIGHT = 4

        fun create(direction: Int, enter: Boolean, duration: Long): CubeAnimation {
            return CubeAnimation(direction, enter).apply {
                setDuration(duration)
            }
        }
    }

    private val camera = Camera()

    protected var width = 0
    protected var height = 0
    protected var alpha = 1.0f
    protected var pivotX = 0.0f
    protected var pivotY = 0.0f
    protected var scaleX = 1.0f
    protected var scaleY = 1.0f
    protected var rotationX = 0.0f
    protected var rotationY = 0.0f
    protected var rotationZ = 0.0f
    protected var translationX = 0.0f
    protected var translationY = 0.0f
    protected var translationZ = 0.0f
    protected var cameraX = 0.0f
    protected var cameraY = 0.0f
    protected var cameraZ = -8.0f

    private var fromAlpha = -1.0f
    private var toAlpha = -1.0f

    fun fading(fromAlpha: Float, toAlpha: Float): CubeAnimation {
        this.fromAlpha = fromAlpha
        this.toAlpha = toAlpha
        return this
    }

    override fun initialize(width: Int, height: Int, parentWidth: Int, parentHeight: Int) {
        super.initialize(width, height, parentWidth, parentHeight)
        this.width = width
        this.height = height

        when (direction) {
            UP, DOWN -> {
                // Vertical animation
                pivotX = width * 0.5f
                pivotY = if (enter == (direction == UP)) 0.0f else height.toFloat()
                cameraZ = -height * 0.015f
            }
            LEFT, RIGHT -> {
                // Horizontal animation
                pivotX = if (enter == (direction == LEFT)) 0.0f else width.toFloat()
                pivotY = height * 0.5f
                cameraZ = -width * 0.015f
            }
        }
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        super.applyTransformation(interpolatedTime, t)

        // Handle alpha fading
        if (fromAlpha >= 0 && toAlpha >= 0) {
            alpha = fromAlpha + (toAlpha - fromAlpha) * interpolatedTime
        }

        // Apply cube transformation based on direction
        when (direction) {
            UP, DOWN -> applyVerticalTransformation(interpolatedTime)
            LEFT, RIGHT -> applyHorizontalTransformation(interpolatedTime)
        }

        // Apply the final transformation matrix
        applyFinalTransformation(t)
    }

    private fun applyVerticalTransformation(interpolatedTime: Float) {
        val value = if (enter) interpolatedTime - 1.0f else interpolatedTime
        val adjustedValue = if (direction == DOWN) value * -1.0f else value
        rotationX = adjustedValue * 90.0f
        translationY = -adjustedValue * height
    }

    private fun applyHorizontalTransformation(interpolatedTime: Float) {
        val value = if (enter) interpolatedTime - 1.0f else interpolatedTime
        val adjustedValue = if (direction == RIGHT) value * -1.0f else value
        rotationY = -adjustedValue * 90.0f
        translationX = -adjustedValue * width
    }

    private fun applyFinalTransformation(t: Transformation) {
        val matrix = t.matrix
        val w = width.toFloat()
        val h = height.toFloat()
        val pX = pivotX
        val pY = pivotY

        // Apply 3D transformations using Camera
        if (rotationX != 0f || rotationY != 0f || rotationZ != 0f) {
            camera.save()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                camera.setLocation(cameraX, cameraY, cameraZ)
            }
            if (translationZ != 0f) {
                camera.translate(0f, 0f, translationZ)
            }
            camera.rotateX(rotationX)
            camera.rotateY(rotationY)
            camera.rotateZ(-rotationZ)
            camera.getMatrix(matrix)
            camera.restore()
            matrix.preTranslate(-pX, -pY)
            matrix.postTranslate(pX, pY)
        }

        // Apply scaling
        if (scaleX != 1.0f || scaleY != 1.0f) {
            matrix.postScale(scaleX, scaleY)
            val sPX = -(pX / w) * ((scaleX * w) - w)
            val sPY = -(pY / h) * ((scaleY * h) - h)
            matrix.postTranslate(sPX, sPY)
        }

        // Apply translation
        matrix.postTranslate(translationX, translationY)

        // Set alpha
        t.alpha = alpha
    }
}