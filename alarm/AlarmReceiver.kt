package com.example.database_part_3.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class MyAlarm : BroadcastReceiver(){
    override fun onReceive(context : Context?, intent : Intent?) {
        Log.d("alarm bell","Alarm just fired")
    }
}