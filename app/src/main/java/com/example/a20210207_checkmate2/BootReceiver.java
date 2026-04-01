package com.example.a20210207_checkmate2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
//
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
//        if (sharedPref.getBoolean(SettingsActivity.KEY_PREF_SWITCH_NOTIFICAION, true)) {
//            // Wiederverwendung der gleichen Logik wie MainActivity.myAlarm()
//            // via NotificationReceiver – einmalig triggern damit der Zyklus neu startet
//            reschedule(context, sharedPref);
//        }
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPref.getBoolean(SettingsActivity.KEY_PREF_SWITCH_NOTIFICAION, true)) {
            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                AlarmScheduler.schedule(context, sharedPref);
            }
        }
    }

}
