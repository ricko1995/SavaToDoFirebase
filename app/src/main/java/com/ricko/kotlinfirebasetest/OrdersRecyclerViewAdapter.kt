package com.ricko.kotlinfirebasetest

import android.app.Activity
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.order_layout.view.*
import kotlinx.android.synthetic.main.order_layout_one_btn.view.*
import java.util.concurrent.TimeUnit

class OrdersRecyclerViewAdapter(
    private val listOfOrders: ArrayList<OrderModel>,
    private val orderClicksInterface: OrderClicksInterface,
    private val typeOfOrder: String,
    private val activity: Activity
) :
    RecyclerView.Adapter<OrdersRecyclerViewAdapter.OrdersViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
        val itemView =
            if (typeOfOrder == ALL_ORDERS)
                LayoutInflater.from(parent.context).inflate(
                    R.layout.order_layout,
                    parent, false
                ) else
                LayoutInflater.from(parent.context).inflate(
                    R.layout.order_layout_one_btn,
                    parent, false
                )


        return OrdersViewHolder(itemView, typeOfOrder)
    }

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {

        when (typeOfOrder) {
            ALL_ORDERS -> {
                holder.allOrdersBind(listOfOrders[position], orderClicksInterface)
            }
            MY_ORDERS -> {
                holder.myOrdersBind(listOfOrders[position], orderClicksInterface, activity)
            }
            ACCEPTED_ORDERS -> {
                holder.acceptedOrdersBind(listOfOrders[position], orderClicksInterface, activity)
//                holder.setIsRecyclable(false)
            }
        }
//        holder.bind(listOfOrders[position], orderClicksInterface)
//        when {
//            else -> {
//                holder.setIsRecyclable(false)
//                holder.bind(listOfOrders[position], orderClicksInterface)
//            }
//        }
    }

    override fun getItemCount() = listOfOrders.size

    override fun getItemId(position: Int): Long {
        return listOfOrders[position].hashCode().toLong()
    }

    class OrdersViewHolder(itemView: View, typeOfOrder: String) :
        RecyclerView.ViewHolder(itemView) {

        private val card: MaterialCardView =
            if (typeOfOrder == ALL_ORDERS) itemView.orderCardView else itemView.orderCardViewOneBtn
        private val iconTxt: MaterialTextView =
            if (typeOfOrder == ALL_ORDERS) itemView.iconText else itemView.iconTextOneBtn
        private val orderNickname: MaterialTextView =
            if (typeOfOrder == ALL_ORDERS) itemView.orderNicknameTxt else itemView.orderNicknameTxtOneBtn
        private val orderTime: MaterialTextView =
            if (typeOfOrder == ALL_ORDERS) itemView.orderTimeTxt else itemView.orderTimeTxtOneBtn
        private val orderDescription: MaterialTextView =
            if (typeOfOrder == ALL_ORDERS) itemView.orderDescriptionTxt else itemView.orderDescriptionTxtOneBtn
        private val orderTip: MaterialTextView =
            if (typeOfOrder == ALL_ORDERS) itemView.orderTipTxt else itemView.orderTipTxtOneBtn
        private val orderLocation: MaterialTextView =
            if (typeOfOrder == ALL_ORDERS) itemView.orderLocationTxt else itemView.orderLocationTxtOneBtn
        private val orderStatus: MaterialTextView =
            if (typeOfOrder == ALL_ORDERS) itemView.orderStatusTxt else itemView.orderStatusTxtOneBtn
        private val orderSucksBtn: MaterialButton? =
            if (typeOfOrder == ALL_ORDERS) itemView.orderNegativeBtn else null
        private val orderAcceptBtn: MaterialButton? =
            if (typeOfOrder == ALL_ORDERS) itemView.orderPositiveBtn else null
        private val orderNeutralBtn: MaterialButton? =
            if (typeOfOrder == ALL_ORDERS) null else itemView.orderNeutralBtnOneBtn
        private lateinit var mAuth: FirebaseAuth


        fun allOrdersBind(order: OrderModel, orderClicksInterface: OrderClicksInterface) {
            mAuth = FirebaseAuth.getInstance()
            iconTxt.text = order.orderByNickname?.subSequence(0, 2)
            orderNickname.text = if (order.orderByNickname?.length!! > 15) {
                (order.orderByNickname.substring(0, 11) + "...")
            } else order.orderByNickname
            orderLocation.text = order.orderLocation
            orderDescription.text = order.orderDescription
            orderTime.text = compareTime(Timestamp.now().seconds, order.orderTimeStamp!!.seconds)
            orderDescription.text = order.orderDescription
            orderTip.text = ("+%.2f".format(order.orderTip) + "kn")
            orderStatus.text = order.orderStatus
            orderAcceptBtn?.text = "Accept\norder"
            orderSucksBtn?.text = ("Order \nSucks (" + (order.orderSucksBy?.size ?: "0") + ")")


            card.setOnClickListener {
                orderClicksInterface.onCardClick(order)
            }

            orderAcceptBtn?.setOnClickListener {
                orderClicksInterface.onPositiveBtnClick(order)
            }

            orderSucksBtn?.setOnClickListener {
                orderClicksInterface.onNegativeBtnClick(order)
            }
        }

        fun myOrdersBind(
            order: OrderModel,
            orderClicksInterface: OrderClicksInterface,
            activity: Activity
        ) {
            mAuth = FirebaseAuth.getInstance()
            iconTxt.text = order.orderByNickname?.subSequence(0, 2)
            orderNickname.text = if (order.orderByNickname?.length!! > 15) {
                (order.orderByNickname.substring(0, 11) + "...")
            } else order.orderByNickname
            orderLocation.text = order.orderLocation
            orderDescription.text = order.orderDescription
            orderTime.text = compareTime(Timestamp.now().seconds, order.orderTimeStamp!!.seconds)
            orderDescription.text = order.orderDescription
            orderTip.text = ("+%.2f".format(order.orderTip) + "kn")
            orderStatus.text = order.orderStatus
            when {
                order.orderStatus.equals("Order Made") -> {
                    val errorColorType = TypedValue()
                    val onSurfaceColorType = TypedValue()
                    activity.theme.resolveAttribute(R.attr.colorError, errorColorType, true)
                    activity.theme.resolveAttribute(R.attr.colorOnSurface, onSurfaceColorType, true)
                    orderNeutralBtn?.text =
                        ("Cancel Order (" + (order.orderSucksBy?.size ?: "0") + ")")
                    orderNeutralBtn?.backgroundTintList =
                        ContextCompat.getColorStateList(activity, errorColorType.resourceId)
                    orderStatus.setTextColor(
                        ContextCompat.getColorStateList(
                            activity,
                            onSurfaceColorType.resourceId
                        )
                    )
                }
                order.orderStatus.equals("Order Accepted") -> {
                    val primaryColorType = TypedValue()
                    activity.theme.resolveAttribute(R.attr.colorPrimary, primaryColorType, true)
                    orderNeutralBtn?.text = "Mark as Finished"
                    orderNeutralBtn?.backgroundTintList =
                        ContextCompat.getColorStateList(activity, primaryColorType.resourceId)
                    orderStatus.setTextColor(Color.GREEN)
                }
                order.orderStatus.equals("Order Finished") -> {
                    val errorColorType = TypedValue()
                    val accentColorType = TypedValue()
                    activity.theme.resolveAttribute(R.attr.colorError, errorColorType, true)
                    activity.theme.resolveAttribute(R.attr.colorAccent, accentColorType, true)
                    orderNeutralBtn?.text = "Delete order from history"
                    orderNeutralBtn?.backgroundTintList =
                        ContextCompat.getColorStateList(activity, accentColorType.resourceId)
                    orderStatus.setTextColor(
                        ContextCompat.getColorStateList(
                            activity,
                            errorColorType.resourceId
                        )
                    )
                }
            }


            card.setOnClickListener {
                orderClicksInterface.onCardClick(order)
            }

            orderNeutralBtn?.setOnClickListener {
                orderClicksInterface.onNeutralBtnClick(order)
            }

        }

        fun acceptedOrdersBind(
            order: OrderModel,
            orderClicksInterface: OrderClicksInterface,
            activity: Activity
        ) {
            mAuth = FirebaseAuth.getInstance()
            iconTxt.text = order.orderByNickname?.subSequence(0, 2)
            orderNickname.text = if (order.orderByNickname?.length!! > 15) {
                (order.orderByNickname.substring(0, 11) + "...")
            } else order.orderByNickname
            orderLocation.text = order.orderLocation
            orderDescription.text = order.orderDescription
            orderTime.text = compareTime(Timestamp.now().seconds, order.orderTimeStamp!!.seconds)
            orderDescription.text = order.orderDescription
            orderTip.text = ("+%.2f".format(order.orderTip) + "kn")
            orderStatus.text = order.orderStatus

            val errorColorType = TypedValue()
            activity.theme.resolveAttribute(R.attr.colorError, errorColorType, true)
            orderNeutralBtn?.text = "Cancel Order"
            orderNeutralBtn?.backgroundTintList =
                ContextCompat.getColorStateList(activity, errorColorType.resourceId)
            orderStatus.setTextColor(Color.GREEN)

            card.setOnClickListener {
                orderClicksInterface.onCardClick(order)
            }

            orderNeutralBtn?.setOnClickListener {
                orderClicksInterface.onNeutralBtnClick(order)
            }
        }

        private fun compareTime(timeNow: Long, timeBefore: Long): String {
            return when (val timeDifference = timeNow - timeBefore) {
                in 0L..60L -> {
                    val td = TimeUnit.SECONDS.toSeconds(timeDifference)
                    return "$td sec ago"
                }
                in 60L..60L * 60L -> {
                    val td = TimeUnit.SECONDS.toMinutes(timeDifference)
                    return "$td min ago"
                }
                in 60L * 60L..60L * 60L * 24L -> {
                    val td = TimeUnit.SECONDS.toHours(timeDifference)
                    return "${td}h ago"
                }
                in 60L * 60L * 24L..60L * 60L * 24L * 7L -> {
                    val td = TimeUnit.SECONDS.toDays(timeDifference)
                    return "$td days ago"
                }
                in 60L * 60L * 24L * 7L..60L * 60L * 24L * 30L -> {
                    val td = TimeUnit.SECONDS.toDays(timeDifference)
                    return "${td / 7} weeks ago"
                }
                in 60L * 60L * 24L * 30L..60L * 60L * 24L * 365L -> {
                    val td = TimeUnit.SECONDS.toDays(timeDifference)
                    return "${td / 30} months ago"
                }
                in 60L * 60L * 24L * 365L..60L * 60L * 24L * 365L * 10L -> {
                    val td = TimeUnit.SECONDS.toDays(timeDifference)
                    return "${td / 365} years ago"
                }
                else -> "Right now"
            }

        }

    }

    interface OrderClicksInterface {
        fun onCardClick(order: OrderModel)
        fun onPositiveBtnClick(order: OrderModel)
        fun onNegativeBtnClick(order: OrderModel)
        fun onNeutralBtnClick(order: OrderModel)
    }

    companion object {
        const val ALL_ORDERS = "allOrders"
        const val MY_ORDERS = "myOrders"
        const val ACCEPTED_ORDERS = "acceptedOrders"
    }


}