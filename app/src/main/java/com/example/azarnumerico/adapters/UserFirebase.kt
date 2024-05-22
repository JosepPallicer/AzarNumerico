package com.example.azarnumerico.adapters

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserFirebase(
    val name: String = "",
    val coins: Int = 0
)