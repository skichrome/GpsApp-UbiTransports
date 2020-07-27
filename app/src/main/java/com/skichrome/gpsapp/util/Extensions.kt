package com.skichrome.gpsapp.util

import android.app.Activity
import android.widget.Toast

fun Activity.shortToast(msgRef: Int) = Toast.makeText(this, getString(msgRef), Toast.LENGTH_SHORT).show()