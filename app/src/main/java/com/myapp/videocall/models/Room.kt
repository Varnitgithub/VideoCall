package com.myapp.videocall.models

data class Room(
    var userUid: String,
    val incoming: String,
    var isAvailable: Boolean,
    var status: String,
    var profile:String
)
