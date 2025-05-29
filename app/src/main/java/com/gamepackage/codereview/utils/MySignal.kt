package com.gamepackage.codereview.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast

class MySignal private constructor(private var context: Context) {

    companion object{
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: MySignal? = null

        fun init(context: Context): MySignal{
            return instance ?: synchronized(this){
                instance?: MySignal(context).also {  instance = it }
            }
        }

        fun getInstance(): MySignal{
            return instance ?: throw IllegalStateException(
                "MySignal must be initialized by calling init(context) before use."
            )
        }
    }

    fun playSoundEffect(res: Int) {
        context.playSoundEffect(res)
    }

    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("ObsoleteSdkInt")
    fun vibrate(durationMillis: Long = 500) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                // API 31 and above: VibratorManager
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                // API 26 to 30: Vibrator with VibrationEffect
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
            }

            else -> {
                // Below API 26: Deprecated vibrate method without VibrationEffect
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMillis)
            }
        }
    }

}