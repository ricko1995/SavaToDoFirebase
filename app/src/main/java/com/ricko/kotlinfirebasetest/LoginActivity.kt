package com.ricko.kotlinfirebasetest

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.transition.Slide
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.transition.MaterialContainerTransformSharedElementCallback
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.login_layout.*

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    companion object{
        var loginActivity:Activity? = null
    }

    override fun onDestroy() {
        super.onDestroy()
        loginActivity = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up shared element transition and disable overlay so views don't show above system bars

        // Set up shared element transition and disable overlay so views don't show above system bars
//        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
//        window.sharedElementsUseOverlay = false
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        window.exitTransition = Slide(Gravity.START)
        loginActivity=this


        mAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser != null) {
            val intent = Intent(this, MainBottomNavigationActivity::class.java)
            startActivity(intent)
            finish()
        }

        setContentView(R.layout.login_layout)
        val actionBar = supportActionBar
        actionBar?.title = "Login"

        Handler().postDelayed({
            keyboardDismiss(constraintLayout)
        }, 100)

        loginBtn.setOnClickListener {
            val areInputsValid =
                !(emailTxtInput.text.toString() == "" || passwordTxtInput.text.toString() == "" || !emailTxtInput.text.toString()
                    .isValidEmail())

            if (areInputsValid) {
                val a = mAuth.signInWithEmailAndPassword(
                    emailTxtInput.text.toString(),
                    passwordTxtInput.text.toString()
                )
                loginProgressBar.visibility = VISIBLE
                keyboardDismiss(constraintLayout)
                a.addOnCompleteListener {
                    if (a.isSuccessful) {
                        loginProgressBar.visibility = GONE
                        val intent = Intent(this, MainBottomNavigationActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        loginProgressBar.visibility = GONE
                        toastMessage("Authentication failed.")
                    }
                }
                a.addOnFailureListener { e ->
                    run {
                        toastMessage(e.message + "Error")
                        e.cause
                    }
                }

            } else {
                toastMessage("Enter valid e-mail and password!!")
                keyboardDismiss(constraintLayout)
            }
        }

        toSignUpBtn.setOnClickListener {

            val intent = Intent(this, SignUpActivity::class.java)
            val options = ActivityOptions.makeSceneTransitionAnimation(this)
            startActivity(intent, options.toBundle())
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

    private fun CharSequence?.isValidEmail() =
        !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}