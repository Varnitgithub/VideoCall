package com.myapp.videocall.activities

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.myapp.videocall.R
import com.myapp.videocall.databinding.MainActivityBinding

class MainActivity : ComponentActivity() {
    private lateinit var binding: MainActivityBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var currentUserUid: String? = null
    private var balanceCoins: String? = null
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)



        val incoming = intent.getStringExtra("incoming")
        val userUid = intent.getStringExtra("userUid")
        val status = intent.getStringExtra("status")
        val profile = intent.getStringExtra("profile")
        val isAvailable = intent.getStringExtra("isAvailable")


        Log.d(
            "TAGGGGGGGGGGG",
            "onCreate:calling activity incoming = $incoming \n userUid = $userUid \n status = $status \n profile = $profile \n isAvailable = $isAvailable"
        )

        firebaseAuth = FirebaseAuth.getInstance()
        currentUserUid = firebaseAuth.currentUser?.uid

        databaseReference = Firebase.database.getReference("Profiles")

    }

    private fun getProfileFromFirebase() {
        databaseReference.child(currentUserUid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val _profilePicture = snapshot.child("profile").getValue<String>()
                    Log.d("TAGGGGGGGG", "onDataChange:profile image is  $_profilePicture")
                    Glide.with(this@MainActivity).load(_profilePicture)
                        .into(binding.profile)
                }
                override fun onCancelled(error: DatabaseError) {
                    binding.profile.setImageResource(R.drawable.user_bg)
                }
            })
    }
}
