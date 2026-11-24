package com.example.a20210207_checkmate2;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
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
    public static final String KEY_PREF_NIGHTSCOUT_TOKEN = "nightscout_token";

    public static final String KEY_PREF_DATA_POINTS = "nightscout_data_points";
    public static final String KEY_PREF_COLOR_THEME = "color_preference";
    public static final String KEY_PREF_SWITCH_GLUCOSE_MOL = "switch_to_mol";
    public static final String KEY_PREF_SWITCH_OWL = "switch_owl";

    public static final String KEY_PREF_HBA1C_DAYS = "hba1c_days";

    public static final String KEY_PREF_HBA1C_GOALS = "hba1c_goal";
    public static final String KEY_PREF_HBA1C_GOALS_MOL = "hba1c_goal_mol";
    public static final String KEY_PREF_HBA1C_VERY_HIGH = "hba1c_veryHigh";
    public static final String KEY_PREF_HBA1C_VERY_HIGH_MOL = "hba1c_veryHigh_mol";

    public static final String KEY_PREF_IN_RANGE_GOAL = "in_range_goal";
    public static final String KEY_PREF_IN_RANGE_TOO_LOW = "in_range_too_low";

    public static final String KEY_PREF_UPPER_RANGE = "upper_range";
    public static final String KEY_PREF_UPPER_RANGE_MOL = "upper_range_mol";
    public static final String KEY_PREF_LOWER_RANGE = "lower_range";
    public static final String KEY_PREF_LOWER_RANGE_MOL = "lower_range_mol";

    public static final String KEY_PREF_SWITCH_NOTIFICAION = "switch_notification";
    public static final String KEY_PREF_NOTIFICATION_TIME = "notification_time";

    public static final String KEY_PREF_MEAN_DAYS = "mean_days_chart";

    public static final String KEY_PREF_SCREEN_VISIBLE_BUBBLEBARS = "screen_visible_bubblesBars";
    public static final String KEY_PREF_SCREEN_RATIO_GUIDELINE_GMI_HBA1C = "screen_ratio_GMI_HbA1c";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = sharedPref.getString(SettingsActivity.KEY_PREF_COLOR_THEME, "bright");
        setAppTheme(theme);
        boolean isDark = "dark".equals(theme);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(!isDark);

        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings_layout), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings_container, new SettingsFragment())
                    .commit();
        }

    }

    public void setAppTheme(String theme) {
        switch (theme) {
            case "bright":
                setTheme(R.style.AppTheme_bright);
                break;
            case "dark":
                setTheme(R.style.AppTheme_dark);
                break;
            default:
                setTheme(R.style.AppTheme_bright);
                break;
        }
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
