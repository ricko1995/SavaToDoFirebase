package com.ricko.kotlinfirebasetest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.sign_up_layout.*

class SignUpActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onSupportNavigateUp(): Boolean {
        finishAfterTransition()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        setContentView(R.layout.sign_up_layout)
        val actionBar = supportActionBar

        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = "Create new account"

        toLoginBtn.setOnClickListener {
            finishAfterTransition()
        }

        signUpBtn.setOnClickListener {

            val areInputsValid = !(nicknameTxtInput.text.toString() == "" ||
                    signUpEmailTxtInput.text.toString() == "" ||
                    !signUpEmailTxtInput.text.toString().isValidEmail() ||
                    signUpPasswordTxtInput.text.toString() == "" ||
                    rePasswordTxtInput.text.toString() != signUpPasswordTxtInput.text.toString() ||
                    signUpPasswordTxtInput.text.toString().length < 4)


            if (areInputsValid) {
                signUpProgressBar.visibility = VISIBLE
                keyboardDismiss(constraintLayout)
                val a = mAuth.createUserWithEmailAndPassword(
                    signUpEmailTxtInput.text.toString(),
                    signUpPasswordTxtInput.text.toString()
                )
                a.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = mAuth.currentUser
                        updateUser(user)
                    } else {
                        toastMessage("Authentication failed.")
                    }
                }
            }
        }
    }

    private fun updateUser(user: FirebaseUser?) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(nicknameTxtInput.text.toString())
            .build()

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    toastMessage("User Created Successfully")
                    signUpProgressBar.visibility = GONE
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    signUpProgressBar.visibility = GONE
                    toastMessage("Account created but nickname is not saved")
                }
            }
    }

    fun keyboardDismiss(view: View) {
        val imm =
            this@SignUpActivity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

    private fun toastMessage(msg: String) {
        Toast.makeText(this@SignUpActivity, msg, Toast.LENGTH_SHORT).show()
    }

    private fun CharSequence?.isValidEmail() =
        !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}