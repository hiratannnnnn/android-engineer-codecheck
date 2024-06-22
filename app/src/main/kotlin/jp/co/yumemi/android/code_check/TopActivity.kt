/*
 * Copyright © 2021 YUMEMI Inc. All rights reserved.
 */
package jp.co.yumemi.android.code_check

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class TopActivity : AppCompatActivity(R.layout.activity_top) {

    companion object {
        // lateinit var lastSearchDate: Date
        private const val PREFS_NAME = "app_prefs"
        private const val KEY_LAST_SEARCH_DATE = "last_search_date"
        private lateinit var sharedPreferences: SharedPreferences

        var lastSearchDate: Date get() {
            val time = sharedPreferences.getLong(KEY_LAST_SEARCH_DATE, 0L)
            return if (time != 0L) Date(time) else Date()
        } set(value) {
            sharedPreferences.edit().putLong(KEY_LAST_SEARCH_DATE,value.time).apply()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
