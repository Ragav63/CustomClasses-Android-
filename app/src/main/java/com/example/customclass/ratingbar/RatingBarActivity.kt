package com.example.customclass.ratingbar

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.customclass.R
import com.example.customclass.databinding.ActivityRatingBarBinding

class RatingBarActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRatingBarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRatingBarBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}