package com.example.customclass.swipebehaviours

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.customclass.R
import com.example.customclass.databinding.ActivitySwipeUnlockBinding

class SwipeUnlockActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySwipeUnlockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySwipeUnlockBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }
}