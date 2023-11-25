package com.myapp.videocall.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.databinding.DataBindingUtil.setContentView
import com.google.firebase.auth.FirebaseAuth
import com.myapp.videocall.R
import com.myapp.videocall.databinding.EntryActivityBinding

class EntryActivity : ComponentActivity() {
    private lateinit var firebaseauth: FirebaseAuth
    private lateinit var binding: EntryActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  setContentView(R.layout.activity_entry)

        binding = setContentView(this, R.layout.entry_activity)

        firebaseauth = FirebaseAuth.getInstance()

        Log.d("TAGGGG", "onCreate: email in entry class:-> ${firebaseauth.currentUser?.email}")
        binding.getStarted.setOnClickListener {

            if (firebaseauth.currentUser != null) {
                startActivity(Intent(this, ConnectingActivity::class.java))
                finishAffinity()
            } else {
                startActivity(Intent(this, AuthenticationActivity::class.java))
                finishAffinity()

            }
        }
    }
}