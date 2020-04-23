package com.ricko.kotlinfirebasetest.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.gson.Gson
import com.ricko.kotlinfirebasetest.*
import com.ricko.kotlinfirebasetest.MainBottomNavigationActivity.Companion.HIDE_VIEWS
import com.ricko.kotlinfirebasetest.MainBottomNavigationActivity.Companion.SHOW_VIEWS
import com.ricko.kotlinfirebasetest.OrderDetailsBottomSheetFragment.Companion.ORDER_DETAILS_KEY
import com.ricko.kotlinfirebasetest.OrderDetailsBottomSheetFragment.Companion.ORDER_DETAILS_TAG
import com.ricko.kotlinfirebasetest.OrderModel.Companion.ORDER_ACCEPTED_BY
import com.ricko.kotlinfirebasetest.OrderModel.Companion.ORDER_STATUS
import com.ricko.kotlinfirebasetest.OrderModel.Companion.ORDER_TIMESTAMP
import com.ricko.kotlinfirebasetest.OrderModel.Companion.ORDER_TIP
import com.ricko.kotlinfirebasetest.OrderModel.Companion.STATUS_ORDER_MADE
import com.ricko.kotlinfirebasetest.OrdersRecyclerViewAdapter.Companion.ACCEPTED_ORDERS
import com.ricko.kotlinfirebasetest.R
import kotlinx.android.synthetic.main.fragment_accepted_orders.view.*

class AcceptedOrdersFragment : Fragment(), OrdersRecyclerViewAdapter.OrderClicksInterface {

    private lateinit var parentActivity: MainBottomNavigationActivity
    private var previousScrollDir = SHOW_VIEWS
    private var currentScrollDir = SHOW_VIEWS

    private var acceptedOrders: ArrayList<OrderModel> = ArrayList()
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
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_accepted_orders, container, false)
        parentActivity.showHideNavigationViewAndFab(SHOW_VIEWS)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        ordersCollection = db.collection("orders")
        recyclerView = root.acceptedOrdersRecyclerView

        registerListener()

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                currentScrollDir = if (dy > 0) HIDE_VIEWS else SHOW_VIEWS

                if (previousScrollDir != currentScrollDir) {
                    parentActivity.showHideNavigationViewAndFab(currentScrollDir)
                    previousScrollDir = currentScrollDir
                }
            }
        })

        return root
    }

    private fun registerListener() {
        if (mAuth.currentUser != null) {
            listenerRegistration = ordersCollection.whereEqualTo(ORDER_ACCEPTED_BY, mAuth.uid)
                .orderBy(sortBy, sortDirection)
                .addSnapshotListener { querySnapshot, _ ->
                    if (recyclerViewAdapter != null) {
                        if (querySnapshot != null) {
                            val newOrders: ArrayList<OrderModel> = ArrayList()
                            for (document in querySnapshot.documents.withIndex()) {
                                newOrders.add(
                                    document.index,
                                    document.value.toObject(OrderModel::class.java)!!
                                )
                                newOrders[document.index].id = document.value.id
                            }
                            when {
                                newOrders.size > acceptedOrders.size -> {
                                    updateAddedOrder(newOrders)
                                }
                                newOrders.size < acceptedOrders.size -> {
                                    updateRemovedOrder(newOrders)
                                }
                                newOrders.size == acceptedOrders.size -> {
                                    updateModifiedOrder(newOrders)
                                }
                            }
                        }
                    } else setOrdersToRecyclerView(querySnapshot)
                }
        }
    }

    private fun updateModifiedOrder(newOrders: ArrayList<OrderModel>) {
        for (order in newOrders.withIndex()) {
            if (order.value != acceptedOrders[order.index]) {
                acceptedOrders[order.index] = order.value
                recyclerViewAdapter?.notifyItemChanged(order.index)
            }
        }
    }

    private fun updateRemovedOrder(newOrders: ArrayList<OrderModel>) {
        val removedOrders = ArrayList(acceptedOrders)
        removedOrders.removeAll(newOrders)

        for (removedOrder in removedOrders) {
            val index = acceptedOrders.indexOf(removedOrder)
            acceptedOrders.removeAt(index)
            recyclerViewAdapter?.notifyItemRemoved(index)
            recyclerViewAdapter?.notifyItemRangeChanged(index, acceptedOrders.size)
        }

    }

    private fun updateAddedOrder(newOrders: ArrayList<OrderModel>) {
        newOrders.removeAll(acceptedOrders)

        for (addedOrder in newOrders) {
            acceptedOrders.add(0, addedOrder)
            if (sortDirection == Query.Direction.ASCENDING) {
                when (sortBy) {
                    ORDER_TIMESTAMP -> {
                        acceptedOrders.sortBy { it.orderTimeStamp }
                    }
                    ORDER_TIP -> {
                        acceptedOrders.sortBy { it.orderTip }
                    }
                    ORDER_STATUS -> {
                        acceptedOrders.sortBy { it.orderStatus }
                    }
                }

            } else {
                when (sortBy) {
                    ORDER_TIMESTAMP -> {
                        acceptedOrders.sortByDescending { it.orderTimeStamp }
                    }
                    ORDER_TIP -> {
                        acceptedOrders.sortByDescending { it.orderTip }
                    }
                    ORDER_STATUS -> {
                        acceptedOrders.sortByDescending { it.orderStatus }
                    }
                }
            }

            recyclerViewAdapter?.notifyItemInserted(acceptedOrders.indexOf(addedOrder))
        }

    }

    private fun setOrdersToRecyclerView(querySnapshot: QuerySnapshot? = null) {
        parentActivity.progressBarState(View.VISIBLE)
        acceptedOrders.clear()

        if (querySnapshot != null) {
            for (document in querySnapshot.withIndex()) {
                acceptedOrders.add(
                    document.index,
                    document.value.toObject(OrderModel::class.java)
                )
                acceptedOrders[document.index].id = document.value.id
            }
            updateRecyclerView()
            parentActivity.progressBarState(GONE)
        } else toastMessage("Something went wrong. Try updating app")
    }

    private fun updateRecyclerView() {
        if (!acceptedOrders.isNullOrEmpty()) {
            recyclerViewAdapter =
                OrdersRecyclerViewAdapter(acceptedOrders, this, ACCEPTED_ORDERS, parentActivity)
            recyclerView.adapter = recyclerViewAdapter
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.setHasFixedSize(true)
        }
    }


    private fun toastMessage(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onCardClick(order: OrderModel) {
        val extras = Bundle()
        extras.putString(ORDER_DETAILS_KEY, Gson().toJson(order))

        val orderDetailsBottomSheetFragment = OrderDetailsBottomSheetFragment()
        orderDetailsBottomSheetFragment.arguments = extras
        orderDetailsBottomSheetFragment.show(
            parentActivity.supportFragmentManager,
            ORDER_DETAILS_TAG
        )
    }

    override fun onPositiveBtnClick(order: OrderModel) {}

    override fun onNegativeBtnClick(order: OrderModel) {}

    override fun onNeutralBtnClick(order: OrderModel) {
        ordersCollection.document(order.id!!).update(
            mapOf(
                ORDER_STATUS to STATUS_ORDER_MADE,
                ORDER_ACCEPTED_BY to null
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

    override fun onPrepareOptionsMenu(menu: Menu) {
        val result = super.onPrepareOptionsMenu(menu)
        val timeItem = menu.findItem(R.id.menuSortByTime)
        val tipItem = menu.findItem(R.id.menuSortByTip)
        if (sortBy == ORDER_TIMESTAMP) {
            timeItem.title = "Sorted by time"
            tipItem.title = "Sort by tip"
            timeItem.setIcon(if (sortDirection == Query.Direction.DESCENDING) R.drawable.ic_arrow_drop_down_white_24dp else R.drawable.ic_arrow_drop_up_white_24dp)
            tipItem.icon = null
        } else if (sortBy == ORDER_TIP) {
            tipItem.title = "Sorted by tip"
            timeItem.title = "Sort by time"
            tipItem.setIcon(if (sortDirection == Query.Direction.DESCENDING) R.drawable.ic_arrow_drop_down_white_24dp else R.drawable.ic_arrow_drop_up_white_24dp)
            timeItem.icon = null
        }
        return result
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
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
                    activity?.invalidateOptionsMenu()
                }, 80)
                return true
            }
            R.id.menuSortByTime -> {
                if (sortBy != ORDER_TIMESTAMP) {
                    sortBy = ORDER_TIMESTAMP
                    listenerRegistration?.remove()
                    registerListener()
                    Handler().postDelayed({
                        recyclerView.smoothScrollToPosition(0)
                        activity?.invalidateOptionsMenu()
                    }, 80)
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
                        activity?.invalidateOptionsMenu()
                    }, 80)
                }
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }


    }
}
