package com.ricko.kotlinfirebasetest

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.new_order_layout.*
import kotlin.math.roundToInt

class NewOrderActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val dummyOrderDescription: List<String> =
        listOf(
            "sir",
            "kava",
            "kifla",
            "piva",
            "vino",
            "petrovo vino",
            "kruh",
            "salama"
        )
    private val dummyOrderLocation: List<String> =
        listOf(
            "doma",
            "soba",
            "sava",
            "roko",
            "zeljeznica",
            "cvijetno",
            "negdje drugdje gdje nas nitko ne vidi",
            "na krov"
        )
    private val dummyOrderTip: List<Float> =
        listOf(5.6f, 5f, 51f, 48.6f, 12.11f, 858f, 1123.5f, 515f, 125f)

    private lateinit var ordersCollection: CollectionReference

    override fun onSupportNavigateUp(): Boolean {
        finishAfterTransition()
        return true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_order_layout)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = "New Order"

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        ordersCollection = db.collection("orders")


        Handler().postDelayed({
            keyboardDismiss(constraintLayout)
        }, 100)

        orderDescriptionInputTxt.setText(dummyOrderDescription[(Math.random() * dummyOrderDescription.size - 0.5).roundToInt()])
        orderLocationInputTxt.setText(dummyOrderLocation[(Math.random() * dummyOrderLocation.size - 0.5).roundToInt()])
        orderTipInputTxt.setText(dummyOrderTip[(Math.random() * dummyOrderTip.size - 0.5).roundToInt()].toString())

        placeOrderBtn.setOnClickListener {
            keyboardDismiss(constraintLayout)
            val isDataValid = (orderDescriptionInputTxt.text.toString() != "" &&
                    orderLocationInputTxt.text.toString() != "" &&
                    orderTipInputTxt.text.toString() != "")

            if (mAuth.currentUser != null && isDataValid) {
                newOrderProgressBar.visibility = VISIBLE
                placeOrderBtn.isEnabled = false
                val newOrder = OrderModel(
                    mAuth.uid,
                    orderDescriptionInputTxt.text.toString(),
                    "Order Made",
                    null,
                    Timestamp.now(),
                    orderTipInputTxt.text.toString().toFloat(),
                    mAuth.currentUser?.displayName,
                    orderLocationInputTxt.text.toString(),
                    if (orderRoomInputTxt.text.toString() == "") null else orderRoomInputTxt.text.toString(),
                    if (orderPavInputTxt.text.toString() == "") null else orderPavInputTxt.text.toString()
                )
                ordersCollection.document().set(newOrder).addOnCompleteListener {
                    if (it.isSuccessful) {
                        toastMessage("Order Added!")
                        newOrderProgressBar.visibility = GONE
                        placeOrderBtn.isEnabled = true
                        finishAfterTransition()
                    } else {
                        toastMessage("Error")
                        newOrderProgressBar.visibility = GONE
                        placeOrderBtn.isEnabled = true
                    }
                }

            } else toastMessage("Data is not valid")
        }
    }

    private fun toastMessage(msg: String) {

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    fun keyboardDismiss(view: View?) {
        val imm =
            this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
        view?.clearFocus()
    }


}