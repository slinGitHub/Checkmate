package com.example.a20210207_checkmate2;

import static com.example.a20210207_checkmate2.SettingsActivity.*;
import static com.example.a20210207_checkmate2.Utils.*;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import android.util.Log;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        updateVisibility();
    }

    @Override
    public void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this.getContext())
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(this.getContext())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePrefSummary(findPreference(key));
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey().equals(KEY_PREF_HBA1C_GOALS)) {
            Log.i("TAG", "onPreferenceTreeClick: ");
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void updateVisibility() {
        boolean useMol = ((SwitchPreference) findPreference(KEY_PREF_SWITCH_GLUCOSE_MOL)).isChecked();
        updateVisibilityItem(KEY_PREF_HBA1C_VERY_HIGH_MOL, KEY_PREF_HBA1C_VERY_HIGH, useMol);
        updateVisibilityItem(KEY_PREF_HBA1C_GOALS_MOL, KEY_PREF_HBA1C_GOALS, useMol);
        updateVisibilityItem(KEY_PREF_UPPER_RANGE_MOL, KEY_PREF_UPPER_RANGE, useMol);
        updateVisibilityItem(KEY_PREF_LOWER_RANGE_MOL, KEY_PREF_LOWER_RANGE, useMol);
    }

    private void updateVisibilityItem(String from, String to, boolean useMol) {
        EditTextPreference perMol = (EditTextPreference) findPreference(from);
        EditTextPreference perPercent = (EditTextPreference) findPreference(to);
        perMol.setVisible(useMol);
        perPercent.setVisible(!useMol);
    }

    private void updatePrefSummary(Preference pref) {
        boolean useMol = ((SwitchPreference) findPreference(KEY_PREF_SWITCH_GLUCOSE_MOL)).isChecked();
        convertUnitsHb(pref, KEY_PREF_HBA1C_GOALS_MOL, KEY_PREF_HBA1C_GOALS, useMol);
        convertUnitsHb(pref, KEY_PREF_HBA1C_VERY_HIGH_MOL, KEY_PREF_HBA1C_VERY_HIGH, useMol);
        convertUnitsBg(pref, KEY_PREF_LOWER_RANGE_MOL, KEY_PREF_LOWER_RANGE, useMol);
        convertUnitsBg(pref, KEY_PREF_UPPER_RANGE_MOL, KEY_PREF_UPPER_RANGE, useMol);
        updateVisibility();
    }

    private void convertUnitsHb(Preference pref, String from, String to, boolean useMol) {
        if (pref.getKey() != null && pref.getKey().equals(from) && useMol) {
            String molValue = ((EditTextPreference) pref).getText();
            Double percentValue = molToPercent(Integer.parseInt(molValue));
            EditTextPreference percentPref = (EditTextPreference) findPreference(to);
            percentPref.setText(String.valueOf(percentValue));
        }
        if (pref.getKey() != null && pref.getKey().equals(to) && !useMol) {
            String percentValue = ((EditTextPreference) pref).getText();
            int molValue = percentToMol(Double.parseDouble(percentValue));
            EditTextPreference goalMol = (EditTextPreference) findPreference(from);
            goalMol.setText(String.valueOf(molValue));
        }
    }

    private void convertUnitsBg(Preference pref, String from, String to, boolean useMol) {
        if (pref.getKey() != null && pref.getKey().equals(from) && useMol) {
            String molValue = ((EditTextPreference) pref).getText();
            int dlValue = molToDl(Double.parseDouble(molValue));
            EditTextPreference dlPref = (EditTextPreference) findPreference(to);
            dlPref.setText(String.valueOf(dlValue));
        }
        if (pref.getKey() != null && pref.getKey().equals(to) && !useMol) {
            String dlValue = ((EditTextPreference) pref).getText();
            Double molValue = dlToMol(Integer.parseInt(dlValue));
            EditTextPreference molPref = (EditTextPreference) findPreference(from);
            molPref.setText(String.valueOf(molValue));
        }
    }


}
