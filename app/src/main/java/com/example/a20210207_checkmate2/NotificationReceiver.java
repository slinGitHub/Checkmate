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

        // Notification anzeigen
        NotificationHelper notificationHelper = new NotificationHelper(context);
        notificationHelper.createNotification();

        // Nächsten Alarm für morgen neu einplanen
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (!sharedPref.getBoolean(SettingsActivity.KEY_PREF_SWITCH_NOTIFICAION, true))
            return;

        String notification_time = sharedPref.getString(
                SettingsActivity.KEY_PREF_NOTIFICATION_TIME, "20:00"
        );
        String[] values = notification_time.split(":");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(values[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(values[1]));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1); // immer morgen

        Intent nextIntent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 1, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
        }
    }
}
