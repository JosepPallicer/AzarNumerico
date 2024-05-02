package com.example.azarnumerico.adapters

import android.app.ActivityManager
import android.content.Context
import com.example.azarnumerico.MainActivity

object MusicUtil {

    fun isAppInBackground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses ?: return true

        for (appProcess in runningAppProcesses) {
            if (appProcess.processName.equals(context.packageName, ignoreCase = true) &&
                appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            ) {
                return false
            }
        }
        return true
    }

}