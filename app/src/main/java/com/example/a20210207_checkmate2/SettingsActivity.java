package com.example.a20210207_checkmate2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;

public class SettingsActivity extends AppCompatActivity {

    public static final String KEY_PREF_NIGHTSCOUT_URL = "nightscout_url";
    public static final String KEY_PREF_GlUCOSE_UNIT = "glucose_unit";
    public static final String KEY_PREF_DATA_POINTS = "nightscout_data_points";
    public static final String KEY_PREF_COLOR_THEME = "color_preference";
    public static final String KEY_PREF_SWITCH_OWL = "switch_owl";

    public static final String KEY_PREF_HBA1C_DAYS = "hba1c_days";

    public static final String KEY_PREF_HBA1C_GOALS = "hba1c_goal";
    public static final String KEY_PREF_HBA1C_VERY_HIGH = "hba1c_veryHigh";

    public static final String KEY_PREF_IN_RANGE_GOAL = "in_range_goal";
    public static final String KEY_PREF_IN_RANGE_TOO_LOW = "in_range_too_low";

    public static final String KEY_PREF_UPPER_RANGE = "upper_range";
    public static final String KEY_PREF_LOWER_RANGE = "lower_range";

    public static final String KEY_PREF_SWITCH_NOTIFICAION = "switch_notification";
    public static final String KEY_PREF_NOTIFICATION_TIME = "notification_time";

    public static final String KEY_PREF_MEAN_DAYS = "mean_days_chart";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

    }

 /*   @Override
    protected void onDestroy() {
        super.onDestroy();

        Intent i = new Intent(this,MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        finish();
        startActivity(i);
    }*/



}