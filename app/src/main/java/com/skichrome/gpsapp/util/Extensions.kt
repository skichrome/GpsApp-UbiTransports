package com.skichrome.gpsapp.util

import android.app.Activity
import android.app.Service
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager

const val KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates"

fun Activity.shortToast(msgRef: Int) = Toast.makeText(this, getString(msgRef), Toast.LENGTH_SHORT).show()
fun Activity.errorLog(msg: String, e: Exception? = null) = Log.e(this.javaClass.simpleName, msg, e)
fun Fragment.errorLog(msg: String, e: Exception? = null) = Log.e(this.javaClass.simpleName, msg, e)
fun Service.errorLog(msg: String, e: Exception? = null) = Log.e(this.javaClass.simpleName, msg, e)

fun Service.isRequestingLocationUpdates() = PreferenceManager.getDefaultSharedPreferences(this)
        .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false)

fun Service.setRequestingLocationUpdates(isRequesting: Boolean) = PreferenceManager.getDefaultSharedPreferences(this)
        .edit()
        .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, isRequesting)
        .apply()
