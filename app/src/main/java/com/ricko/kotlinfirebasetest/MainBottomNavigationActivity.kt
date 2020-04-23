package com.ricko.kotlinfirebasetest

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.transition.Explode
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main_bottom_navigation.*

class MainBottomNavigationActivity : AppCompatActivity() {


    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        window.exitTransition = Explode()
        setContentView(R.layout.activity_main_bottom_navigation)

        mAuth = FirebaseAuth.getInstance()

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_all_orders, R.id.navigation_my_orders, R.id.navigation_accepted_orders
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        ordersFabBtn.setOnClickListener {
            val intent = Intent(this, NewOrderActivity::class.java)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        }
    }

    fun showHideNavigationViewAndFab(dir: String) {
        when(dir){
            SHOW_VIEWS-> run {
                ordersFabBtn.animate().translationX(0f)
                nav_view.animate().translationY(0f)
            }
            HIDE_VIEWS-> run {
                ordersFabBtn.animate().translationX(300f)
                nav_view.animate().translationY(300f)
            }
        }
    }


    fun progressBarState(state: Int){
        ordersProgressBar.visibility = state
    }

    private fun toastMessage(msg: String) {

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    companion object{
        const val SHOW_VIEWS = "show"
        const val HIDE_VIEWS = "hide"
    }

}
