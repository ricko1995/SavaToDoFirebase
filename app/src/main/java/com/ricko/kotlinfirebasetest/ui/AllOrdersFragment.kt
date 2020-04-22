package com.ricko.kotlinfirebasetest.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.gson.Gson
import com.ricko.kotlinfirebasetest.*
import com.ricko.kotlinfirebasetest.OrderModel.Companion.ORDER_ACCEPTED_BY
import com.ricko.kotlinfirebasetest.OrderModel.Companion.ORDER_BY
import com.ricko.kotlinfirebasetest.OrderModel.Companion.ORDER_STATUS
import com.ricko.kotlinfirebasetest.OrderModel.Companion.ORDER_SUCKS_BY
import com.ricko.kotlinfirebasetest.OrderModel.Companion.ORDER_TIMESTAMP
import com.ricko.kotlinfirebasetest.OrderModel.Companion.ORDER_TIP
import com.ricko.kotlinfirebasetest.OrderModel.Companion.STATUS_ORDER_ACCEPTED
import com.ricko.kotlinfirebasetest.OrderModel.Companion.STATUS_ORDER_MADE
import com.ricko.kotlinfirebasetest.OrdersRecyclerViewAdapter.Companion.ALL_ORDERS
import com.ricko.kotlinfirebasetest.R
import kotlinx.android.synthetic.main.fragment_all_orders.view.*

class AllOrdersFragment : Fragment(), OrdersRecyclerViewAdapter.OrderClicksInterface {

    private lateinit var parentActivity: MainBottomNavigationActivity
    private var previousScrollDir = "UP"
    private var currentScrollDir = "UP"

    private var allOrders: ArrayList<OrderModel> = ArrayList()
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var ordersCollection: CollectionReference
    private var recyclerViewAdapter: OrdersRecyclerViewAdapter? = null
    private lateinit var recyclerView: RecyclerView
    private var listenerRegistration: ListenerRegistration? = null
    private var sortBy: String = ORDER_TIMESTAMP
    private var sortDirection: Query.Direction = Query.Direction.DESCENDING

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove()
        recyclerViewAdapter = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        parentActivity = activity as MainBottomNavigationActivity
        parentActivity.scrolling(currentScrollDir)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_all_orders, container, false)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        ordersCollection = db.collection("orders")
        recyclerView = root.allOrdersRecyclerView

        registerListener()

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                currentScrollDir = if (dy > 0) "DOWN" else "UP"

                if (previousScrollDir != currentScrollDir) {
                    parentActivity.scrolling(currentScrollDir)
                    previousScrollDir = currentScrollDir
                }
            }
        })



        return root
    }

    private fun registerListener(){
        if (mAuth.currentUser != null) {
            listenerRegistration = ordersCollection.whereEqualTo(ORDER_STATUS, STATUS_ORDER_MADE)
                .orderBy(sortBy, sortDirection)
                .addSnapshotListener { querySnapshot, _ ->
                    val newOrders: ArrayList<OrderModel> = ArrayList()
                    var i = 0
                    if (recyclerViewAdapter != null && querySnapshot != null) {
                        for (document in querySnapshot.documents) {
                            if (document[ORDER_BY]!! != mAuth.uid) {
                                newOrders.add(
                                    i,
                                    document.toObject(OrderModel::class.java)!!
                                )
                                newOrders[i].id = document.id
                                i++
                            }
                        }
                        when {
                            newOrders.size > allOrders.size -> {
                                updateAddedOrder(newOrders)
                            }
                            newOrders.size < allOrders.size -> {
                                updateRemovedOrder(newOrders)
                            }
                            newOrders.size == allOrders.size -> {
                                updateModifiedOrder(newOrders)
                            }
                        }
                    } else getOrders()
                }
        }
    }

    private fun updateModifiedOrder(newOrders: ArrayList<OrderModel>) {
        for (order in newOrders.withIndex()) {
            if (order.value != allOrders[order.index]) {
                allOrders[order.index] = order.value
                recyclerViewAdapter?.notifyItemChanged(order.index)
            }
        }
    }

    private fun updateRemovedOrder(newOrders: ArrayList<OrderModel>) {
        val removedOrders = ArrayList(allOrders)
        removedOrders.removeAll(newOrders)

        for (removedOrder in removedOrders) {
            val index = allOrders.indexOf(removedOrder)
            allOrders.removeAt(index)
            recyclerViewAdapter?.notifyItemRemoved(index)
            recyclerViewAdapter?.notifyItemRangeChanged(index, allOrders.size)
        }

    }

    private fun updateAddedOrder(newOrders: ArrayList<OrderModel>) {
        newOrders.removeAll(allOrders)

        for (addedOrder in newOrders) {
            allOrders.add(0, addedOrder)
            if(sortDirection == Query.Direction.ASCENDING){
                when (sortBy) {
                    ORDER_TIMESTAMP -> {
                        allOrders.sortBy { it.orderTimeStamp }
                    }
                    ORDER_TIP -> {
                        allOrders.sortBy { it.orderTip }
                    }
                    ORDER_STATUS -> {
                        allOrders.sortBy { it.orderStatus }
                    }
                }

            } else{
                when (sortBy) {
                    ORDER_TIMESTAMP -> {
                        allOrders.sortByDescending { it.orderTimeStamp }
                    }
                    ORDER_TIP -> {
                        allOrders.sortByDescending { it.orderTip }
                    }
                    ORDER_STATUS -> {
                        allOrders.sortByDescending { it.orderStatus }
                    }
                }
            }

            recyclerViewAdapter?.notifyItemInserted(allOrders.indexOf(addedOrder))
        }

    }


    private fun getOrders() {
        if (mAuth.currentUser != null) {
            parentActivity.progressBarState(VISIBLE)
            allOrders.clear()

            val a = ordersCollection.whereEqualTo(ORDER_STATUS, STATUS_ORDER_MADE)
                .orderBy(sortBy, sortDirection).get()

            a.addOnCompleteListener {
                if (it.isSuccessful) {
                    var i = 0
                    for (document in it.result!!) {
                        if (document[ORDER_BY]!! != mAuth.uid) {
                            allOrders.add(
                                i,
                                document.toObject(OrderModel::class.java)
                            )
                            allOrders[i].id = document.id
                            i++
                        }
                    }
                    updateRecyclerView()

                } else {
                    toastMessage("Error")
                    Log.d("TAG", it.exception.toString())
                }
                parentActivity.progressBarState(GONE)
            }

        } else toastMessage("You are not signed in!")
    }

    private fun updateRecyclerView() {
        if (!allOrders.isNullOrEmpty()) {
            recyclerViewAdapter =
                OrdersRecyclerViewAdapter(allOrders, this, ALL_ORDERS, parentActivity)
            recyclerView.adapter = recyclerViewAdapter
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.setHasFixedSize(true)
        }
    }

    private fun toastMessage(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onCardClick(order: OrderModel) {
        //TODO open activity OrderDetailActivity

        val intent = Intent(context, OrderDetailActivity::class.java)
        intent.putExtra("order", Gson().toJson(order))
        startActivity(intent)
    }

    override fun onPositiveBtnClick(order: OrderModel) {
        ordersCollection.document(order.id!!).update(
            mapOf(
                ORDER_STATUS to STATUS_ORDER_ACCEPTED,
                ORDER_ACCEPTED_BY to mAuth.uid
            )
        )
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    toastMessage("Order Accepted")
                } else {
                    toastMessage("Error")
                    Log.d("TAG", it.exception.toString())
                }
            }
    }

    override fun onNegativeBtnClick(order: OrderModel) {
        ordersCollection.document(order.id!!)
            .update(ORDER_SUCKS_BY, FieldValue.arrayUnion(mAuth.uid))
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    toastMessage("Order Sucks")
                } else {
                    toastMessage("Error")
                    Log.d("TAG", it.exception.toString())
                }
            }
    }

    override fun onNeutralBtnClick(order: OrderModel) {
//        TODO("Not yet implemented")
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menuEditUser -> {
                updateRecyclerView()
                return true
            }
            R.id.menuChangeTheme -> {
                toastMessage("change theme")
                return true
            }
            R.id.menuLogout -> {
                mAuth.signOut()
                val intent = Intent(context, LoginActivity::class.java)
                startActivity(intent)
                activity?.finish()
                return true
            }
            R.id.menuSortDirection -> {
                sortDirection = if (sortDirection == Query.Direction.DESCENDING) {
                    Query.Direction.ASCENDING
                } else {
                    Query.Direction.DESCENDING
                }

                listenerRegistration?.remove()
                registerListener()
                Handler().postDelayed({
                    recyclerView.smoothScrollToPosition(0)
                },80)
                return true
            }
            R.id.menuSortByTime -> {
                if (sortBy != ORDER_TIMESTAMP) {
                    sortBy = ORDER_TIMESTAMP
                    listenerRegistration?.remove()
                    registerListener()
                    Handler().postDelayed({
                        recyclerView.smoothScrollToPosition(0)
                    },80)
                }
                return true
            }
            R.id.menuSortByTip -> {
                if (sortBy != ORDER_TIP) {
                    sortBy = ORDER_TIP
                    listenerRegistration?.remove()
                    registerListener()
                    Handler().postDelayed({
                        recyclerView.smoothScrollToPosition(0)
                    },80)
                }
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }


    }
}
