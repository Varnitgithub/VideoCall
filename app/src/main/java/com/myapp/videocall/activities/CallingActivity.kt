package com.myapp.videocall.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.myapp.videocall.R

class CallingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calling)

        val incoming = intent.getStringExtra("incoming")
        val userUid = intent.getStringExtra("userUid")
        val status = intent.getStringExtra("status")
        val profile = intent.getStringExtra("profile")
        val isAvailable = intent.getStringExtra("isAvailable")


        Log.d(
            "TAGGGGGGGGGGG",
            "onCreate:calling activity incoming = $incoming \n userUid = $userUid \n status = $status \n profile = $profile \n isAvailable = $isAvailable"
        )
    }
}