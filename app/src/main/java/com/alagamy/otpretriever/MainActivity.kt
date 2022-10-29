package com.alagamy.otpretriever

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(smsReceiver, intentFilter)

        SmsRetriever.getClient(this).startSmsUserConsent(null)
            .addOnCompleteListener {
                if (it.isSuccessful)
                    Log.e(TAG, "listening..")
                else
                    Log.e(TAG, "failed..")
            }
    }

    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                Log.e(TAG, "onReceive: " + intent.action)
                if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                    val extras = intent.extras
                    val smsStatus = extras?.get(SmsRetriever.EXTRA_STATUS) as Status
                    when (smsStatus.statusCode) {
                        CommonStatusCodes.SUCCESS -> {
                            val getConsentIntent = extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                            resultLauncher.launch(getConsentIntent);

                        }
                        CommonStatusCodes.TIMEOUT -> {}
                        CommonStatusCodes.CANCELED -> {}

                    }

                }
            }
        }
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.e(TAG, ": hello" )
            val message = result.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
            val otp = message?.filter { it.isDigit() } ?: ""
            findViewById<EditText>(R.id.etEnterOTP).setText(otp)
        } else
            Log.d(TAG, "Permission Denied")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(smsReceiver)
    }
}