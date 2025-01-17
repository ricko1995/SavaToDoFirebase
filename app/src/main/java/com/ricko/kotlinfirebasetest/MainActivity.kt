package com.ricko.kotlinfirebasetest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    private val dummyOrderDescription: List<String> =
        listOf("sir", "kava", "kifla", "piva", "vino", "petrovo vino", "kruh", "salama")
    private val dummyOrderStatus: List<String> =
        listOf("Order Made", "Order Finished", "Order Accepted")

    private lateinit var mAuth: FirebaseAuth
    private val email = "123@123.com"
    private val password = "123456"
    private lateinit var db: FirebaseFirestore
    private val orders: MutableList<OrderModel> = mutableListOf()
    private lateinit var ordersCollection: CollectionReference
    private var listenerRegistration: ListenerRegistration? = null

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this, "destroyed", Toast.LENGTH_SHORT).show()
        listenerRegistration?.remove()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        ordersCollection = db.collection("orders")

        card.setOnClickListener {
            keyboardDismiss(it)
        }


        helloBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        if (mAuth.currentUser != null) {
            listenerRegistration = ordersCollection.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                toastMessage("op op op, " + querySnapshot?.size().toString() + " new orders")
            }

        } else toastMessage("not signed in listener")

        signInBtn.setOnClickListener {
            toastMessage(mAuth.currentUser?.displayName.toString())
//            mAuth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener {
//                    toastMessage("completed")
//                }
        }

        signOutBtn.setOnClickListener {
            mAuth.signOut()
            toastMessage("Signed Out")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        addOrderBtn.setOnClickListener {
            if (mAuth.currentUser != null) {
                val newOrder = OrderModel(
                    mAuth.uid,
                    dummyOrderDescription[(Math.random() * dummyOrderDescription.size - 0.5).roundToInt()],
                    dummyOrderStatus[(Math.random() * dummyOrderStatus.size - 0.5).roundToInt()],
                    (Math.random() * 10).roundToInt(),
                    Timestamp.now(),
                    (Math.random() * 100).toFloat(),
                    mAuth.currentUser?.displayName
                )
                ordersCollection.document().set(newOrder)

            } else toastMessage("not signed in add order")
        }

        orderDetailsBtn.setOnClickListener {
            if (mAuth.currentUser != null) {

                ordersCollection.whereEqualTo("orderDescription", "petrovo vino")
                    .whereArrayContains("orderSucksBy", "sfasfgadf").get()
                    .addOnSuccessListener { result ->

                        toastMessage(result.documents[0].id)

                        for (document in result.withIndex()) {

                            orders.add(
                                document.index,
                                document.value.toObject(OrderModel::class.java)
                            )
                            orders[document.index].id = document.value.id
                            toastMessage(orders[document.index].orderDescription.toString())
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w("TAG", "Error getting documents.", exception)
                    }

//                ordersCollection.orderBy("orderTimeStamp", Query.Direction.DESCENDING).get()
//                    .addOnSuccessListener { result ->
//                        for (document in result.withIndex()) {
//
//                            orders.add(
//                                document.index,
//                                document.value.toObject(OrderModel::class.java)
//                            )
//                        }
//
//                        val currentTime = Timestamp.now().seconds
//                        val orderTime = orders[0].orderTimeStamp!!.seconds
//                        val diff = compare(currentTime, orderTime)
//                        toastMessage(diff)
//                    }
            } else toastMessage("not signed in order details")
        }
    }

    private fun compare(timeNow: Long, timeBefore: Long): String {
        return when (val timeDifference = timeNow - timeBefore) {
            in 0L..60L -> {
                val td = TimeUnit.SECONDS.toSeconds(timeDifference)
                return "$td seconds ago"
            }
            in 60L..60L * 60L -> {
                val td = TimeUnit.SECONDS.toMinutes(timeDifference)
                return "$td minutes ago"
            }
            in 60L * 60L..24L * 60L * 60L -> {
                val td = TimeUnit.SECONDS.toHours(timeDifference)
                return "$td hours ago"
            }
            in 24L * 60L * 60L..24L * 60L * 60L * 365L -> {
                val td = TimeUnit.SECONDS.toDays(timeDifference)
                return "$td days ago"
            }
            else -> "Long time ago"
        }

    }

    private fun toastMessage(msg: String) {

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    fun keyboardDismiss(view: View) {
        val imm =
            this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }
}


