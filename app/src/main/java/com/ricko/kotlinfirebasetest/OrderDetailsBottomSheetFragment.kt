package com.ricko.kotlinfirebasetest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.ricko.kotlinfirebasetest.MainBottomNavigationActivity.Companion.HIDE_VIEWS
import com.ricko.kotlinfirebasetest.MainBottomNavigationActivity.Companion.SHOW_VIEWS
import kotlinx.android.synthetic.main.order_details_layout.view.*

class OrderDetailsBottomSheetFragment: BottomSheetDialogFragment() {

    private lateinit var parentActivity: MainBottomNavigationActivity

    private lateinit var order: OrderModel

    override fun onDestroy() {
        super.onDestroy()
        parentActivity.showHideNavigationViewAndFab(SHOW_VIEWS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        parentActivity = activity as MainBottomNavigationActivity
        parentActivity.showHideNavigationViewAndFab(HIDE_VIEWS)

        order = Gson().fromJson(arguments?.get(ORDER_DETAILS_KEY).toString(), OrderModel::class.java)

        val actionBar = parentActivity.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.order_details_layout, container, false)

        root.testTxtView.text = order.orderDescription

        return root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            parentActivity.onBackPressed()
            return true
        }
        return false
    }

    companion object{
        const val ORDER_DETAILS_KEY = "order"
        const val ORDER_DETAILS_TAG = "orderDetailsBottomSheetFragment"
    }

}