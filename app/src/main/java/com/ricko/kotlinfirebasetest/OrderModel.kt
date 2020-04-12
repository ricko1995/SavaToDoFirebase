package com.ricko.kotlinfirebasetest

import com.google.firebase.Timestamp

data class OrderModel(
    val orderBy: String? = null,
    val orderDescription: String? = null,
    val orderStatus: String? = null,
    val orderSucks: Int? = null,
    val orderTimeStamp: Timestamp? = null,
    val orderTip: Float? = null
)