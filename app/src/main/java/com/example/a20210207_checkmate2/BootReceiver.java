package com.example.a20210207_checkmate2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPref.getBoolean(SettingsActivity.KEY_PREF_SWITCH_NOTIFICAION, true)) {
            // Wiederverwendung der gleichen Logik wie MainActivity.myAlarm()
            // via NotificationReceiver – einmalig triggern damit der Zyklus neu startet
            reschedule(context, sharedPref);
        }
    }

    private void reschedule(Context context, SharedPreferences sharedPref) {
        String notification_time = sharedPref.getString(
                SettingsActivity.KEY_PREF_NOTIFICATION_TIME, "20:00"
        );
        String[] values = notification_time.split(":");

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, Integer.parseInt(values[0]));
        calendar.set(java.util.Calendar.MINUTE, Integer.parseInt(values[1]));
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis())
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);

        Intent intent = new Intent(context, NotificationReceiver.class);
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(
                context, 1, intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
        );

        android.app.AlarmManager alarmManager =
                (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        android.app.AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        android.app.AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
        }
    }
}
