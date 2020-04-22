package com.ricko.kotlinfirebasetest

import com.google.firebase.Timestamp
import com.google.firestore.v1.MapValue


data class OrderModel(
    val orderBy: String? = null,
    val orderDescription: String? = null,
    val orderStatus: String? = null,
    val orderSucks: Int? = null,
    val orderTimeStamp: Timestamp? = null,
    val orderTip: Float? = null,
    val orderByNickname: String? = null,
    val orderLocation: String? = null,
    val orderRoom: String? = null,
    val orderPav: String? = null,
    val orderAcceptedBy: String? = null,
    val orderSucksBy: List<String>? = null,
    var id: String? = null
) {
    companion object {
        const val ORDER_BY = "orderBy"
        const val ORDER_DESCRIPTION = "orderDescription"
        const val ORDER_STATUS = "orderStatus"
        const val ORDER_SUCKS = "orderSucks"
        const val ORDER_TIMESTAMP = "orderTimeStamp"
        const val ORDER_TIP = "orderTip"
        const val ORDER_BY_NICKNAME = "orderByNickname"
        const val ORDER_LOCATION = "orderLocation"
        const val ORDER_ROOM = "orderRoom"
        const val ORDER_PAV = "orderPav"
        const val ORDER_ACCEPTED_BY = "orderAcceptedBy"
        const val ORDER_SUCKS_BY = "orderSucksBy"
        const val ORDER_ID = "id"

        const val STATUS_ORDER_MADE = "Order Made"
        const val STATUS_ORDER_ACCEPTED = "Order Accepted"
        const val STATUS_ORDER_FINISHED = "Order Finished"
    }

}