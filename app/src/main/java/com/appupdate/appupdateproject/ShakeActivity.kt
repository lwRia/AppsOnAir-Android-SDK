package com.appupdate.appupdateproject

import android.app.ActivityManager
import android.app.usage.StorageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.appsonair.interfaces.UpdateCallBack
import com.appsonair.services.AppsOnAirServices
import com.appsonair.services.ShakeBugService
import java.io.IOException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ShakeActivity : AppCompatActivity() {
    private val TAG = "ShakeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shake)

        AppsOnAirServices.setAppId("---------app-id-------------", true)

        ShakeBugService.shakeBug(this)

        AppsOnAirServices.checkForAppUpdate(this, object :
            UpdateCallBack {
            override fun onSuccess(response: String) {
                Log.e("mye", "" + response)
            }

            override fun onFailure(message: String) {
                Log.e("mye", "onFailure$message")
            }
        })

        findViewById<Button>(R.id.btn_create_ticket).setOnClickListener(View.OnClickListener {
            ShakeBugService.shakeBug(
                this,
                raiseNewTicket = true,
                extraPayload = mapOf(
                    "title" to "Initial Demo",
                    "city" to "Surat",
                    "state" to "Gujarat"
                )
            )
        })

        val packageInfo = packageManager.getPackageInfo(packageName, 0)

        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        val appName = packageManager.getApplicationLabel(applicationInfo) as String
        // Get version name and version code
        val versionName = packageInfo.versionName
        val versionCode = packageInfo.versionCode
        val packagename = packageInfo.packageName

        Log.d("TAG", "adapters: " + versionName)
        Log.d("TAG", "adapters: " + versionCode)
        Log.d("TAG", "adapters: " + appName)
        Log.d("TAG", "adapters: " + packagename)

        val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getResources().getConfiguration().getLocales().get(0)
        } else {
            getResources().getConfiguration().locale
        }

        Log.d(TAG, "device locale ::: " + locale.displayCountry)
        Log.d(TAG, "device locale ::: " + locale.country)

        val sdf: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.getDefault())
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
        val now: Date = Date()
        Log.d(TAG, "device locale ::: " + sdf.format(now))

        Log.d(TAG, "MODEL: " + Build.MODEL)
        Log.d(TAG, "Manufacture: " + Build.MANUFACTURER)
        Log.d(TAG, "Brand: " + Build.BRAND)
        Log.d(TAG, "Version Code: " + Build.VERSION.RELEASE)
        Log.d(TAG, "SDK INT: " + Build.VERSION.SDK_INT)

        this.registerReceiver(
            this.batteryInfoReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())

            val width = windowMetrics.bounds.width() - insets.left - insets.right
            val height = windowMetrics.bounds.height() - insets.top - insets.bottom

            Log.d(TAG, "Screen width::  $width pixels")
            Log.d(TAG, "Screen height::  $height pixels")
        } else {
            val displayMetrics = DisplayMetrics()
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val width = displayMetrics.widthPixels
            val height = displayMetrics.heightPixels

            Log.d(TAG, "Screen width:  $width pixels")
            Log.d(TAG, "Screen height:  $height pixels")
        }

        val orientation = getResources().configuration.orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d(TAG, "orientation: portrait")
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d(TAG, "orientation: landscape")
        }

        Log.d(TAG, "total-storage::::: " + getReadableStorageSize(getTotalStorageSize(this)))

        Log.d(TAG, "app-memory:::: " + getReadableStorageSize(getAvailableMemory().totalMem))
        Log.d(TAG, "app-memory:::: " + getReadableStorageSize(getAvailableMemory().availMem))

        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses

        for (processInfo in runningAppProcesses) {
            Log.d(TAG, "Memory info :" + packageName)
            if (processInfo.processName == packageName) {
                val memoryInfoArray =
                    activityManager.getProcessMemoryInfo(intArrayOf(processInfo.pid))
                val memoryInfo = memoryInfoArray[0]
                val totalPss: Int = memoryInfo.getTotalPss() // Total memory used by the app in KB
                Log.d("Memory Info", "Package: $packageName")
                Log.d("Memory Info", "Total PSS: " + getReadableStorageSize(totalPss.toLong()))
                break
            }
        }

        val libVersionName = com.appsonair.BuildConfig.VERSION_NAME
        val libVersionCode = com.appsonair.BuildConfig.VERSION_CODE

        Log.d(TAG, "sdk version: " + libVersionName)
        Log.d(TAG, "sdk version: " + libVersionCode)
    }

    private fun getAvailableMemory(): ActivityManager.MemoryInfo {
        val activityManager = this.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo
    }

    fun getReadableStorageSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (kotlin.math.log10(size.toDouble()) / kotlin.math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(
            size / Math.pow(
                1024.0,
                digitGroups.toDouble()
            )
        ) + " " + units[digitGroups]
    }

    fun getTotalStorageSize(context: Context): Long {
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val storageStatsManager =
                    context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
                val uuid = storageManager.getUuidForPath(Environment.getDataDirectory())
                val totalBytes = storageStatsManager.getTotalBytes(uuid)
                val freeBytes = storageStatsManager.getFreeBytes(uuid)
                val usedBytes = totalBytes - freeBytes
                return usedBytes
            } catch (e: IOException) {
                return 0.0.toLong()
            }
        } else {
            return 0.0.toLong()
        }
    }

    private val batteryInfoReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

            val batteryPct = level * 100 / scale.toFloat()

            Log.d(TAG, "Battery level ::: " + batteryPct)
        }
    }
}