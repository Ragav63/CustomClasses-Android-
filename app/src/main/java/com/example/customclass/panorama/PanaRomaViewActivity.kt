package com.example.customclass.panorama

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.example.customclass.R
import com.example.customclass.databinding.ActivityPanaRomaViewBinding
import com.panoramagl.PLImage
import com.panoramagl.PLManager
import com.panoramagl.PLSphericalPanorama
import com.panoramagl.utils.PLUtils

class PanaRomaViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPanaRomaViewBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityPanaRomaViewBinding.inflate(layoutInflater)

        setContentView(binding.root)



    }


}