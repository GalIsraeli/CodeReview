package com.gamepackage.codereview.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gamepackage.codereview.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (auth.currentUser == null) {
            goToLogin()
            return
        }

        binding.startButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.historyButton.setOnClickListener {
            startActivity(Intent(this, ChatsListActivity::class.java))
        }

        binding.favoritesButton.setOnClickListener {
            startActivity(Intent(this, FavoritesListActivity::class.java))
        }

        binding.logoutButton.setOnClickListener {
            auth.signOut()
            goToLogin()
        }
    }

    private fun goToLogin() {
        Intent(this, AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(this)
        }
        finish()
    }
}
