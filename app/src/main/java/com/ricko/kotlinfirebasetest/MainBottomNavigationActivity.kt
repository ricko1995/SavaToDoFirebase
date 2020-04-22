package com.ricko.kotlinfirebasetest

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.transition.Explode
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.fragment.app.findFragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.ricko.kotlinfirebasetest.ui.AllOrdersFragment
import com.ricko.kotlinfirebasetest.ui.MyOrdersFragment
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
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        ordersFabBtn.setOnClickListener {
            val intent = Intent(this, NewOrderActivity::class.java)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        }
    }

    fun scrolling(dir: String) {
        when(dir){
            "UP"-> run {
                ordersFabBtn.animate().translationY(0f)
            }
            "DOWN"-> run {
                ordersFabBtn.animate().translationY(500f)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }

    fun progressBarState(state: Int){
        ordersProgressBar.visibility = state
    }

    private fun toastMessage(msg: String) {

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

}
