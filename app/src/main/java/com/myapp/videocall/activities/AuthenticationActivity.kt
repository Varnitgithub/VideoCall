package com.myapp.videocall.activities


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.myapp.videocall.R
import com.myapp.videocall.application.ApplicationClass
import com.myapp.videocall.databinding.AuthenticationActivityBinding
import com.myapp.videocall.models.UserDetails
import java.util.Objects

class AuthenticationActivity : ComponentActivity() {
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var binding: AuthenticationActivityBinding
    private var userMap: HashMap<String, Objects>? = null
    private lateinit var firebaseDatabase: DatabaseReference

    companion object {
        private const val REQUEST_CODE_SIGN_IN = 111
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.authentication_activity)


        // Initialize application
        application as ApplicationClass

        // Initialize Firebase components
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseDatabase = Firebase.database.getReference("Profiles")
        Log.d(
            "TAGGGG",
            "onCreate: email in authentication class:-> ${firebaseAuth.currentUser?.email}"
        )

        checkLoggedInUser()



        binding.loginBtn.setOnClickListener {
            if (firebaseAuth.currentUser != null) {
                Log.d("TAGGGG", "onCreate: user is not null")
            } else {
                Log.d("TAGGGG", "onCreate: user is null")
                val signInIntent = mGoogleSignInClient.signInIntent
                startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN)

            }

        }

        val gso: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                val googleSignInAccount = task.result
                if (googleSignInAccount != null) {
                    val idToken = googleSignInAccount.idToken
                    if (idToken != null) {
                        signIn(idToken)
                        Log.d("TAGGGGGGG", "onActivityResult: id token $idToken")
                    }
                } else {
                    Log.d("TAGGGG", "Google Sign-In failed")
                }
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun signIn(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("TAGGGG", "saveUserOnFirebase: here 3")
                    saveUserOnFirebase()
                    startActivity(Intent(this, ConnectingActivity::class.java))
                    finishAffinity()
                } else {
                    Log.e("TAGGGG", "signInWithCredential: ${task.exception}")
                }
            }.addOnFailureListener {
                Log.d("TAGGGG", "signIn: error ${it.localizedMessage}")
            }
    }

    private fun saveUserOnFirebase() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            // Save user data to Firebase
            val userDetails = UserDetails(
                user.email!!, user.displayName!!,
                user.photoUrl.toString(), user.uid, "A", 500
            )

            firebaseDatabase.child(user.uid).setValue(userDetails)
        }
    }

    fun checkLoggedInUser() {
        firebaseDatabase.child("users").orderByChild("email").limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val email = snapshot.getValue(String::class.java)

                    if (email == firebaseAuth.currentUser?.email) {
                        Log.d("TAGGGGGGGG", "onDataChange: $email")

                    } else {
                        Log.d("TAGGGGGGGG", "onDataChange: user not found")

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }
}
