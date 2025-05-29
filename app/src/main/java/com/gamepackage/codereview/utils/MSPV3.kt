package com.gamepackage.codereview.utils

import android.content.Context
import android.content.SharedPreferences


class MSPV3 private constructor(context: Context) {

    private val spFileName = "MySpFile"
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(spFileName, Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var instance: MSPV3? = null

        fun init(context: Context): MSPV3 {
            return instance ?: synchronized(this) {
                instance ?: MSPV3(context).also { instance = it }
            }
        }

        fun getInstance(): MSPV3 {
            return instance ?: throw IllegalStateException(
                "MSPV3 must be initialized by calling init(context) before use."
            )
        }
    }


}