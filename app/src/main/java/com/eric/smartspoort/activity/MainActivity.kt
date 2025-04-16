package com.eric.smartspoort.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eric.smartspoort.R
import com.eric.logsupport.logutil.SLog
import com.eric.logsupport.logutil.SmartLogImpl

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        SLog.setLogger(
            SmartLogImpl
                .Builder()
                .tag("Bee")
                .limit(Log.DEBUG)
                .folderName("Bee")
                .openLogToFile(false)
                .versionName("BuildConfig.VERSION_NAME"))

        SLog.d("- - init - -")
    }
}