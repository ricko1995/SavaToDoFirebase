package com.ricko.kotlinfirebasetest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.order_detail_layout.*

class OrderDetailActivity : AppCompatActivity() {


    private lateinit var order: OrderModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.order_detail_layout)

        order = Gson().fromJson(intent.extras!!["order"].toString(), OrderModel::class.java)

        testTxtView.text = order.orderDescription

    }
}