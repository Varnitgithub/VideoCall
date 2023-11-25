package com.myapp.videocall.application

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class ApplicationClass: Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        Log.d("TAGGGG", "onCreate: firebase app initialize")


    }
}