package com.example.myapplication.receivera

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.view.Gravity
import android.widget.Toast

class CallReceiver : BroadcastReceiver() {
    private var lastState = TelephonyManager.EXTRA_STATE_IDLE
    private var lastIncomingNumber = ""

    override fun onReceive(context: Context?, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        if (state == TelephonyManager.EXTRA_STATE_RINGING) {
            // Incoming call started
            showToastMessage(context!!, "Incoming call ")
        }
        else if (state == TelephonyManager.EXTRA_STATE_OFFHOOK) {
            // Outgoing call started or incoming call answered
            showToastMessage(context!!, "On Incoming Call Started")
        }
        else if (state == TelephonyManager.EXTRA_STATE_IDLE) {
            if (lastState.equals(TelephonyManager.EXTRA_STATE_RINGING) &&
                !incomingNumber.equals(lastIncomingNumber)) {
                // Missed call
                showToastMessage(context!!, "On Missed call ")
            }
            else if (lastState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                // Call ended
                showToastMessage(context!!, "On Call ended")
            }
        }

        lastState = state
        lastIncomingNumber = incomingNumber ?: ""
    }

    private fun showToastMessage(context: Context, msg: String) {
        val toast = Toast.makeText(context, msg, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }
}
