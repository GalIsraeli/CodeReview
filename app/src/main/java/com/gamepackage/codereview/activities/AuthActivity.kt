package com.gamepackage.codereview.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.gamepackage.codereview.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inflate via View Binding
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // 3. If a user is already signed in, go straight to HomeActivity
        auth.currentUser?.let {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        // 4. Wire up button clicks via binding
        binding.signInButton.setOnClickListener { signIn() }
        binding.registerButton.setOnClickListener { register() }
    }

    private fun signIn() {
        val email = binding.emailField.text.toString().trim()
        val password = binding.passwordField.text.toString().trim()

        if (!validateInputs(email, password)) return

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Successful sign-in → go to HomeActivity
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    Snackbar.make(
                        binding.root,
                        "Sign-in failed: ${task.exception?.localizedMessage}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun register() {
        val email = binding.emailField.text.toString().trim()
        val password = binding.passwordField.text.toString().trim()

        if (!validateInputs(email, password)) return

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Automatic sign-in on successful registration
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    Snackbar.make(
                        binding.root,
                        "Registration failed: ${task.exception?.localizedMessage}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailField.error = "Enter a valid email"
            binding.emailField.requestFocus()
            return false
        }
        if (password.length < 6) {
            binding.passwordField.error = "Password must be ≥6 characters"
            binding.passwordField.requestFocus()
            return false
        }
        return true
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null && ev.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view is android.widget.EditText) {
                val outRect = android.graphics.Rect()
                view.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    view.clearFocus()
                    val imm = getSystemService<InputMethodManager>()
                    imm?.hideSoftInputFromWindow(view.windowToken, HIDE_NOT_ALWAYS)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}
