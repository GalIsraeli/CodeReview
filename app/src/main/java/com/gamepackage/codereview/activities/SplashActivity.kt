package com.gamepackage.codereview.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        setupSplashExitAnimation(splash)
    }

    private fun setupSplashExitAnimation(splashScreen: androidx.core.splashscreen.SplashScreen) {
        splashScreen.setOnExitAnimationListener { provider: SplashScreenViewProvider ->
            animateDropIn(provider.iconView, provider)
        }
    }

    private fun animateDropIn(icon: View, provider: SplashScreenViewProvider) {
        // Start off-screen above and at half-size
        icon.translationY = -resources.displayMetrics.heightPixels.toFloat()
        icon.scaleX = 0.5f
        icon.scaleY = 0.5f

        icon.animate()
            .translationY(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setInterpolator(DecelerateInterpolator(2f))   // slow down at the end
            .setDuration(1200)                              // 1.2s for a slower feel
            .withEndAction {
                provider.remove()
                launchAuth()
            }
            .start()
    }


    private fun launchAuth() {
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }
}
