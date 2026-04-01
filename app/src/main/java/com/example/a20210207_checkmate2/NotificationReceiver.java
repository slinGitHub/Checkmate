package com.example.a20210207_checkmate2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.preference.PreferenceManager;

import java.util.Calendar;


public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        android.util.Log.d("CheckmateNotification", "NotificationReceiver:Alarm wurde ausgelöst!");
//        goAsync() wird seit SDK31 nicht mehr empfohlen, kann leicht gekillt werden vom System.
//        final PendingResult result = goAsync(); // Android warten lassen
//
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
//
//        // Nächsten Alarm einplanen
//        if (sharedPref.getBoolean(SettingsActivity.KEY_PREF_SWITCH_NOTIFICAION, true)) {
//            AlarmScheduler.schedule(context, sharedPref);
//        }
//
//        // Notification anzeigen – result.finish() wird im Callback aufgerufen
//        NotificationHelper notificationHelper = new NotificationHelper(context);
//        notificationHelper.createNotification(() -> {
//            result.finish(); // Erst jetzt darf Android den Prozess beenden
//        });

        // Just start the Worker and finish immediately
        androidx.work.OneTimeWorkRequest workRequest = new androidx.work.OneTimeWorkRequest.Builder(NotificationWorker.class).build();
        androidx.work.WorkManager.getInstance(context).enqueue(workRequest);

        // Re-schedule alarm if needed
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPref.getBoolean(SettingsActivity.KEY_PREF_SWITCH_NOTIFICAION, true)) {
            AlarmScheduler.schedule(context, sharedPref);
        }


    }
}
