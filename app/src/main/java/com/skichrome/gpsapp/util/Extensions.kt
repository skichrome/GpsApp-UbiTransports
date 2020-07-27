package com.skichrome.gpsapp.util

import android.app.Activity
import android.util.Log
import android.widget.Toast

fun Activity.shortToast(msgRef: Int) = Toast.makeText(this, getString(msgRef), Toast.LENGTH_SHORT).show()
fun Activity.errorLog(msg: String, e: Exception? = null) = Log.e(this.javaClass.simpleName, msg, e)