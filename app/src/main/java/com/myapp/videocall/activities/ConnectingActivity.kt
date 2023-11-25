package com.myapp.videocall.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.myapp.videocall.R
import com.myapp.videocall.databinding.ConnectingActivityBinding
import com.myapp.videocall.models.Room


class ConnectingActivity : ComponentActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ConnectingActivityBinding
    private lateinit var firestore: FirebaseFirestore
    private var currentUserUid: String? = null
    private var requestPermission = false
    private var balanceCoins: Int? = null
    private var PICK_IMAGE_REQUEST_CODE = 123
    private val REQUEST_CODE = 121
    private lateinit var firebaseDatabaseReference: DatabaseReference
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_connecting)
        binding = DataBindingUtil.setContentView(this, R.layout.connecting_activity)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        currentUserUid = firebaseAuth.currentUser?.uid
        firebaseDatabaseReference = Firebase.database.getReference().child("users")
        databaseReference = Firebase.database.getReference("Profiles")



        binding.coins.text = "You have: 0"
        val getcoinsfromfirebase = getcoinsFromFirebase()
        binding.coins.text = "You have: $getcoinsfromfirebase"

        getProfileFromFirebase{

        }
        getcoinsFromFirebase()

        binding.logout.setOnClickListener {
            logoutUser()
        }
        binding.findButton.setOnClickListener {
            if (isPermissionGranted() || requestPermission) {
                moving()
            } else {
                requestPermissions()
            }
        }
        binding.getImage.setOnClickListener {
            getImageFromGalary()
        }
    }

    fun moving() {

        getuser()
    }

    private fun getuser() {
        firebaseDatabaseReference.orderByChild("status").equalTo("0").limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.childrenCount > 0 && snapshot.child("userUid").value.toString() != currentUserUid) {
                        Log.d("TAGGGGGG", "onDataChange: room found")
                        //Room available
                        for (user in snapshot.children) {
                            firebaseDatabaseReference.child(user.key!!).child("incoming")
                                .setValue(currentUserUid)
                            firebaseDatabaseReference.child(user.key!!).child("status")
                                .setValue("1")

                            val intent = Intent(
                                this@ConnectingActivity,
                                MainActivity::class.java
                            )
                            intent.putExtra(
                                "incoming",
                                user.child("incoming").value.toString()
                            )
                            intent.putExtra(
                                "profile",
                                user.child("profile").value.toString()
                            )

                            intent.putExtra(
                                "userUid",
                                user.child("userUid").value.toString()
                            )

                            intent.putExtra(
                                "status",
                                user.child("status").value.toString()
                            )
                            intent.putExtra(
                                "isAvailable",
                                user.child("isAvailable").value.toString()
                            )
                            startActivity(intent)

                        }


                    } else {
                        Log.d("TAGGGGGG", "onDataChange: room does not found")
                        var profilePic = ""
                        getProfileFromFirebase {
                            profilePic = it
                        }
                        //Room unavailable
                        val room =
                            Room(currentUserUid!!, currentUserUid!!, true, "0", profilePic)

                        firebaseDatabaseReference.child(currentUserUid!!).setValue(room)
                            .addOnSuccessListener {
                                firebaseDatabaseReference.child(currentUserUid!!)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (snapshot.exists()) {
                                                if (snapshot.child("status").value.toString()
                                                        .toInt() == 1
                                                ) {
                                                    coinsAfterDeduction()
                                                    val intent = Intent(
                                                        this@ConnectingActivity,
                                                        MainActivity::class.java
                                                    )
                                                    intent.putExtra(
                                                        "incoming",
                                                        snapshot.child("incoming").value.toString()
                                                    )
                                                    intent.putExtra(
                                                        "profile",
                                                        snapshot.child("profile").value.toString()
                                                    )
                                                    intent.putExtra(
                                                        "userUid",
                                                        snapshot.child("userUid").value.toString()
                                                    )
                                                    intent.putExtra(
                                                        "isAvailable",
                                                        snapshot.child("isAvailable").value.toString()
                                                    )
                                                    intent.putExtra(
                                                        "status",
                                                        snapshot.child("status").value.toString()
                                                    )
                                                    startActivity(intent)

                                                } else {

                                                }
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            TODO("Not yet implemented")
                                        }
                                    })


                            }.addOnFailureListener {


                            }

                    }


                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }


    private fun coinsAfterDeduction() {
        val coinsfromFirebase = getcoinsFromFirebase()
        if (coinsfromFirebase != null) {
            if (coinsfromFirebase.toInt() >= 50) {

                val mCoins = coinsfromFirebase.minus(50)
                binding.coins.text = "You have : ${mCoins.toString()}"

                val updateData = mapOf("coins" to mCoins)

                databaseReference.child(currentUserUid!!).updateChildren(updateData)
                    .addOnSuccessListener {

                        binding.coins.text = "You have: ${getcoinsFromFirebase()}"
                    }.addOnFailureListener {
                        Log.d("TAGGGG", "onDataChange: data does not Update")

                    }

            } else {
                Toast.makeText(
                    this@ConnectingActivity,
                    "You ran out of coins \n Please watch video for coins",
                    Toast.LENGTH_SHORT
                ).show()
            }
            //update value of coins on firebase
            Log.d("TAGGGG", "coinsAfterDeduction: Balnce deduction complete")
        }


    }

    private fun getcoinsFromFirebase(): Int? {
        databaseReference.child(currentUserUid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userCoins = snapshot.child("coins").value.toString().toInt()
                    balanceCoins = userCoins
                    binding.coins.text = "You have : $balanceCoins"
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        return balanceCoins
    }


    private fun getImageFromGalary() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {

            val selectedImageUri = data.data

            binding.profilePicture.setImageURI(selectedImageUri)
            Log.d("TAGGGGG", "onActivityResult: profile updated")
            val updates = HashMap<String, Any>()
            updates["profile"] = selectedImageUri.toString()
            firestore.collection(currentUserUid!!).document("lYU6NlP4rIN4PaZXk2E3")
                .update(updates)
        }
    }

    private fun isPermissionGranted(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        )
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
            (this, android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
            (this, android.Manifest.permission.MODIFY_AUDIO_SETTINGS)
                != PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.MODIFY_AUDIO_SETTINGS
            ), REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE) { // Check if this is the request code you used when requesting permissions
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Log.d("TAGGGGGGGGGG", "onRequestPermissionsResult: permission granted")
                moving()
                requestPermission = true
            } else {
                Log.d(
                    "TAGGGGGGGGGG",
                    "onRequestPermissionsResult: permission is not granted granted"
                )

                requestPermissions()
            }
        }
    }


    private fun getProfileFromFirebase(callback: (String) -> Unit) {
        databaseReference.child(currentUserUid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val _profilePicture = snapshot.child("profile").getValue<String>()
                    var newpro = _profilePicture
                    Log.d("TAGGGGGGGG", "onDataChange:profile image is  $_profilePicture")
                    Glide.with(this@ConnectingActivity).load(_profilePicture)
                        .into(binding.profilePicture)
                    callback(newpro!!)
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.profilePicture.setImageResource(R.drawable.user_bg)
                    callback("") // or handle the error in some way
                }
            })
    }


    private fun logoutUser() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            user.delete()
                .addOnSuccessListener {
                    Log.e("TAGGGGG", "User account deleted successfully")
                    firebaseAuth.signOut()
                    databaseReference.removeValue().addOnSuccessListener {
                        Log.e("TAGGGGG", "User deleted from database ")
                        unlinkCurrentUser()

                    }.addOnFailureListener {
                        Log.e("TAGGGGG", "User does not deleted from database ")

                    }// Sign the user out after the account is deleted
                    startActivity(Intent(this, EntryActivity::class.java))
                    finishAffinity()
                }
                .addOnFailureListener { e ->
                    // An error occurred while deleting the user account
                    Log.e("TAGGGGG", "Error deleting user account: $e")
                }
        } else {
            // Handle the case where the user is not logged in
        }
    }

    fun unlinkCurrentUser() {

        val currentUser = firebaseAuth.currentUser

        if (currentUser != null) {
            // Check if the user is signed in with Google
            for (userInfo in currentUser.providerData) {
                if (GoogleAuthProvider.PROVIDER_ID == userInfo.providerId) {
                    // User is signed in with Google, obtain the GoogleAuthCredential
                    val googleAuthCredential = GoogleAuthProvider.getCredential(userInfo.uid, null)

                    // Unlink Google provider
                    currentUser.unlink(GoogleAuthProvider.PROVIDER_ID)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Google provider unlinked successfully
                                Log.d("FirebaseAuth", "Google provider unlinked successfully")
                            } else {
                                // Failed to unlink Google provider
                                val exception = task.exception
                                if (exception is FirebaseAuthRecentLoginRequiredException) {
                                    // Re-authentication is required to unlink the provider
                                    Log.e(
                                        "FirebaseAuth",
                                        "Re-authentication is required to unlink Google provider"
                                    )
                                } else {
                                    // Handle other exceptions
                                    Log.e(
                                        "FirebaseAuth",
                                        "Failed to unlink Google provider",
                                        exception
                                    )
                                }
                            }
                        }

                    // Break the loop since we found the Google provider
                    break
                }
            }
        }
    }
}