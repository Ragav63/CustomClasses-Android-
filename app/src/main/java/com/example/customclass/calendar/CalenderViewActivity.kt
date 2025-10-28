package com.example.customclass.calendar

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.customclass.R
import com.example.customclass.databinding.ActivityCalenderViewBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class CalenderViewActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCalenderViewBinding




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCalenderViewBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }


}