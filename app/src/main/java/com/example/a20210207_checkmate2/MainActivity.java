package com.example.a20210207_checkmate2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.preference.PreferenceManager;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import static com.example.a20210207_checkmate2.SettingsActivity.KEY_PREF_MEAN_DAYS;
import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity implements AsyncResponse {

    private String currentTheme;
    private SharedPreferences sharedPref;
    private Boolean initNightscoutURL;
    private GlucoseEntry lastDate;
    private Boolean isSyncServiceRunning = false;
    private ArrayList<GlucoseEntry> glucoseDataRawStore = new ArrayList<>();
    private CalcHba1c calcHba1c;
    private static ActionMenuItemView buttonSync;
    private static RotateAnimation rotateAnimation;

    //-------------------------------------------------------------------------------
    // Reload Content after getting back to Main window
    //-------------------------------------------------------------------------------
    @Override
    public void onResume() {
        super.onResume();

        // Check if Theme was changed
        String theme = sharedPref.getString(SettingsActivity.KEY_PREF_COLOR_THEME, "");
        if (!currentTheme.equals(theme))
            recreate();

        // start Program
        startProgram();
        animateRefreshButton(this);
    }

    //-------------------------------------------------------------------------------
    // Start Program
    //-------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rotateAnimation = new RotateAnimation(360, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration((long) 2 * 500);
        rotateAnimation.setRepeatCount(Animation.INFINITE);

        // Set Class for handling a crash
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this, MainActivity.class));

        initNightscoutURL = false;

        // Set Theme if changed
        // Load Default Preference Settings
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        currentTheme = sharedPref.getString("currentTheme", "darkBlue");

        String theme = sharedPref.getString(SettingsActivity.KEY_PREF_COLOR_THEME, "darkBlue");
        if (!currentTheme.equals(theme)) {
            // Update current Theme
            sharedPref.edit().putString("currentTheme", theme).apply();
        }
        setAppTheme(theme);

        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setIcon(R.drawable.ic_action_checkmateactionbarowl); // Show Icon in Toolbar

    }

    public void setAppTheme(String theme) {
        switch (theme) {
            case "blue":
                setTheme(R.style.AppTheme_Blue);
                break;
            case "green":
                setTheme(R.style.AppTheme_Green);
                break;
            case "violet":
                setTheme(R.style.AppTheme_Violet);
                break;
            default:
                break;
        }
    }

    public void startProgram() {

        //-------------------------------------------------------------------------------
        // Horizontal Screen Size Setting : Guideline Position (Default is 81 percent for Nexus5X)
        //-------------------------------------------------------------------------------
        float guidelinePosition = Float.parseFloat(sharedPref.getString(SettingsActivity.KEY_PREF_SCREEN_RATIO_GUIDELINE_GMI_HBA1C, "81"));
        Guideline guideLine = (Guideline) findViewById(R.id.guideline5);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guideLine.getLayoutParams();
        params.guidePercent = guidelinePosition / 100;
        guideLine.setLayoutParams(params);

        //-------------------------------------------------------------------------------
        // Load local copy of glucose data
        //-------------------------------------------------------------------------------
        // If local data can be retrieved load first local and then update data
        ArrayList<GlucoseEntry> glucoseDataRaw;

        try {
            FileInputStream fis;
            fis = this.openFileInput("glucoseDataRaw.dat");
            BufferedInputStream bFis = new BufferedInputStream(fis);
            ObjectInputStream is = new ObjectInputStream(bFis);
            glucoseDataRaw = (ArrayList<GlucoseEntry>) is.readObject();
            is.close();
            fis.close();

            // If local data was found -> load it
            if (glucoseDataRaw.size() > 0) {
                processFinish(glucoseDataRaw, false);
            }

        } catch (IOException | ClassNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }

        //-------------------------------------------------------------------------------
        // Goto Nightscout and download data + reload local saved data
        //-------------------------------------------------------------------------------

        // Check if Network Connection is up to Date
        if (isNetworkConnected()) {

            // Load saved settings from shared preferences
            SharedPreferences.Editor editor = sharedPref.edit();

            // Check if nightscout website is already set in preferences
            // Get the URL string from settings
            String nightscoutURLPref = sharedPref.getString(SettingsActivity.KEY_PREF_NIGHTSCOUT_URL, ""); // Does not work right now
            String nightscoutTokenPref = sharedPref.getString(SettingsActivity.KEY_PREF_NIGHTSCOUT_TOKEN, ""); // Does not work right now

            if ((nightscoutURLPref.equals("Enter your Nightscout URL here (https://YOUR_NIGHTSCOUTPAGE.herokuapp.com/)")) && !initNightscoutURL) {
                initNightscoutURL = true;
                AlertDialog.Builder alertName = new AlertDialog.Builder(this);
                final EditText editTextName1 = new EditText(this);

                alertName.setTitle("Enter your Nightscout Webpage:");
                alertName.setMessage("(https://YOURWEBPAGE.com)");
                // titles can be used regardless of a custom layout or not
                alertName.setView(editTextName1);
                LinearLayout layoutName = new LinearLayout(this);
                layoutName.setOrientation(LinearLayout.VERTICAL);
                layoutName.addView(editTextName1); // displays the user input bar
                alertName.setView(layoutName);

                alertName.setPositiveButton("Enter", (dialog, whichButton) -> {
                    editor.putString(SettingsActivity.KEY_PREF_NIGHTSCOUT_URL, editTextName1.getText().toString());
                    editor.apply();
                });

                alertName.show(); // display the dialog

                // Input Token Dialog

                AlertDialog.Builder alertName2 = new AlertDialog.Builder(this);
                final EditText editTextName2 = new EditText(this);
                alertName2.setTitle("Enter your Nightscout Access Token:");
                alertName2.setMessage("XX-XXXXXXXX");
                alertName2.setView(editTextName2);
                LinearLayout layoutName2 = new LinearLayout(this);
                layoutName2.setOrientation(LinearLayout.VERTICAL);
                layoutName2.addView(editTextName2); // displays the user input bar
                alertName2.setView(layoutName2);

                alertName2.setPositiveButton("Enter", (dialog, whichButton) -> {
                    editor.putString(SettingsActivity.KEY_PREF_NIGHTSCOUT_TOKEN, editTextName2.getText().toString());
                    editor.apply();
                    startProgram();
                });
                alertName2.setNegativeButton("Without Token", (dialog, whichButton) -> startProgram());

                alertName2.show(); // display the dialog

            } else if (!isSyncServiceRunning) {
                // Max number of data points to receive from Nightscout
                int nightscoutMaxDataPoints = parseInt(sharedPref.getString(SettingsActivity.KEY_PREF_DATA_POINTS, "30000"));
                String sinceDate = "2015-08-28";
                if (lastDate != null && nightscoutMaxDataPoints <= glucoseDataRawStore.size()) {
                    sinceDate = lastDate.dateString;
                }
                if (lastDate != null) {
                    if (new Date().getTime() - lastDate.date.getTime() < 5 * 60 * 1000) {
                        // No need to check for new data if, latest record is less than 5 minutes old
                        return;
                    }
                }

                buttonSync = this.findViewById(R.id.action_reload);
                if (buttonSync != null) {
                    buttonSync.startAnimation(rotateAnimation);
                }
                // Enter String to Nightscout Data
                // Data formation example : https://github.com/nightscout/cgm-remote-monitor/blob/master/swagger.json
                String url = nightscoutURLPref + "/api/v1/entries/sgv.csv?count=" + nightscoutMaxDataPoints + "&find[dateString][$gt]=" + sinceDate; // All Data
                // Add token if necessary
                if (!nightscoutTokenPref.equals("Enter your token here for the readable role at your nightscout page."))
                    url = url + "&token=" + nightscoutTokenPref;
                //String url = nightscoutURLPref + "/api/v1/entries/sgv.csv?count=100000&find[dateString][$gt]=2015-08-28"; // Short Dataset
                //String urlVeryShort = nightscoutURLPref + "/api/v1/entries/sgv.csv"; // Very Short Dataset

                Toast.makeText(this, "Connecting to " + nightscoutURLPref, Toast.LENGTH_SHORT).show();

                // Try to get data from Nightscout
                ReadGlucoseEntry task = new ReadGlucoseEntry(this);
                isSyncServiceRunning = true;
                task.execute(url);
            }
        }
    }

    //-------------------------------------------------------------------------------
    // Return data if possible from Nightscout and calculate Values
    //-------------------------------------------------------------------------------
    @Override
    public void processFinish(ArrayList<GlucoseEntry> glucoseDataRaw, boolean saveData) {
        if (glucoseDataRaw != null) {
            if (glucoseDataRaw.size() > 0) {
                if (saveData) {
                    glucoseDataRaw.addAll(glucoseDataRawStore);
                    glucoseDataRawStore = glucoseDataRaw;
                } else {
                    glucoseDataRawStore = glucoseDataRaw;
                }
                lastDate = glucoseDataRaw.get(0);
                //-------------------------------------------------------------------------------
                // Save Glucose Data to local file for faster startup (in separate thread)
                //-------------------------------------------------------------------------------
                if (saveData) {
                    int nightscoutMaxDataPoints = parseInt(sharedPref.getString(SettingsActivity.KEY_PREF_DATA_POINTS, "30000"));
                    while (glucoseDataRawStore.size() > nightscoutMaxDataPoints) {
                        glucoseDataRawStore.remove(glucoseDataRawStore.size() - 1);
                    }
                    SaveGlucoseData save = new SaveGlucoseData(this, this, glucoseDataRawStore);
                    save.execute();
                }

                //-------------------------------------------------------------------------------
                // Calculate HbA1c
                //-------------------------------------------------------------------------------
                calcHba1c = new CalcHba1c(glucoseDataRawStore);
                calcHba1c.CalcHba1c(this);

                // Load saved settings from shared preferences
                calcHba1c.CalcAverageHba1c(calcHba1c.getHba1cData(), parseInt(sharedPref.getString(SettingsActivity.KEY_PREF_HBA1C_DAYS, "90")));

                // Call the chart creation algorithm
                createCharts(calcHba1c);
            } else {
                Toast.makeText(this, "No new Glucose Data found..", Toast.LENGTH_SHORT).show();
            }
        }
        if (saveData) {
            isSyncServiceRunning = false;
        }
        if (buttonSync != null) {
            buttonSync.clearAnimation();
        }
    }

    public void createCharts(CalcHba1c calcHba1c) {

        //-------------------------------------------------------------------------------
        // Create Line Chart
        //-------------------------------------------------------------------------------
        LineChartClass lineChart = new LineChartClass(this, calcHba1c.getHba1cData(), calcHba1c.daysGoingBackCount);
        lineChart.createChart(calcHba1c.getHba1cData().size() - 1, true);
        lineChart.mChart.moveViewToX(calcHba1c.getHba1cData().size());
        //-------------------------------------------------------------------------------
        //Create Line Average Chart
        //-------------------------------------------------------------------------------
        LineChartAvgClass lineAvgChart = new LineChartAvgClass(this, calcHba1c.getHba1cData(), calcHba1c.getHba1cAverageData());
        lineAvgChart.createChart(false);
        // Get Y Max and Min from Line Chart
        YAxis YAxisLeft = lineChart.mChart.getAxisLeft();
        lineAvgChart.left.setAxisMaximum(YAxisLeft.mAxisMaximum);
        lineAvgChart.left.setAxisMinimum(YAxisLeft.mAxisMinimum);

        //-------------------------------------------------------------------------------
        // Create Bar Chart
        //-------------------------------------------------------------------------------
        BarChartClass barChart = new BarChartClass(this, calcHba1c.getHba1cData());
        barChart.createChart(calcHba1c.getHba1cData().size() - 1, true);
        barChart.bChart.moveViewToX(calcHba1c.getHba1cData().size());
        //-------------------------------------------------------------------------------
        // Create Bar Average Chart
        //-------------------------------------------------------------------------------
        BarChartAvgClass barAvgChart = new BarChartAvgClass(this, calcHba1c.getHba1cData(), calcHba1c.getHba1cAverageData());
        barAvgChart.createChart(calcHba1c.nDays, false);
        //Get Y Max and Min from Bar Chart
        YAxis bYAxisLeft = barChart.bChart.getAxisLeft();
        barAvgChart.bLeft.setAxisMaximum(bYAxisLeft.mAxisMaximum);
        barAvgChart.bLeft.setAxisMinimum(bYAxisLeft.mAxisMinimum);

        //-------------------------------------------------------------------------------
        // Create Day Line Chart
        //-------------------------------------------------------------------------------
        // Create Statistics (Mean and % in Range)
        CalcDayLineChartStats calcDayLineChartStats = new CalcDayLineChartStats(calcHba1c.getGlucoseData());

        int meanDays = parseInt(sharedPref.getString(KEY_PREF_MEAN_DAYS, "7"));
        calcDayLineChartStats.CalcMedian(meanDays);

        DayLineChartClass dayLineChart = new DayLineChartClass(this, calcHba1c.getHba1cData(), calcHba1c.getGlucoseData(), calcDayLineChartStats);
        dayLineChart.createChart(calcHba1c.getHba1cData().size() - 1, true, calcHba1c.sgvMin, calcHba1c.sgvMax);

        //-------------------------------------------------------------------------------
        // Synchronize line and bar chart
        //-------------------------------------------------------------------------------
        lineChart.mChart.setOnChartGestureListener(new CoupleChartGestureListener(
                lineChart.mChart, new Chart[]{barChart.bChart}));
        barChart.bChart.setOnChartGestureListener(new CoupleChartGestureListener(
                barChart.bChart, new Chart[]{lineChart.mChart}));

        // Synchronize line chart gesture
        lineChart.mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                lineChart.createChart((int) e.getX(), true);
                lineAvgChart.createChart(false);

                barChart.createChart((int) e.getX(), true);
                barAvgChart.createChart(calcHba1c.nDays, false);

                dayLineChart.createChart((int) e.getX(), true, calcHba1c.sgvMin, calcHba1c.sgvMax);
            }

            @Override
            public void onNothingSelected() {
            }
        });

        // Synchronize line chart gesture
        lineAvgChart.mAChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                lineChart.createChart((int) e.getX(), false);
                lineAvgChart.createChart(true);

                barChart.createChart((int) e.getX(), false);
                barAvgChart.createChart(calcHba1c.nDays, true);

                int meanDays = parseInt(sharedPref.getString(KEY_PREF_MEAN_DAYS, "7"));
                calcDayLineChartStats.CalcMedian(meanDays);
                dayLineChart.createMeanChart();
            }

            @Override
            public void onNothingSelected() {
            }
        });

        // Synchronize bar chart gesture
        barChart.bChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                lineChart.createChart((int) e.getX(), true);
                lineAvgChart.createChart(false);

                barChart.createChart((int) e.getX(), true);
                barAvgChart.createChart(calcHba1c.nDays, false);

                dayLineChart.createChart((int) e.getX(), true, calcHba1c.sgvMin, calcHba1c.sgvMax);
            }

            @Override
            public void onNothingSelected() {
            }
        });

        // Synchronize bar chart gesture
        barAvgChart.bAvgChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                lineChart.createChart((int) e.getX(), false);
                lineAvgChart.createChart(true);

                barChart.createChart((int) e.getX(), false);
                barAvgChart.createChart(calcHba1c.nDays, true);

                int meanDays = parseInt(sharedPref.getString(KEY_PREF_MEAN_DAYS, "7"));
                calcDayLineChartStats.CalcMedian(meanDays);
                dayLineChart.createMeanChart();
            }

            @Override
            public void onNothingSelected() {
            }
        });


        //-------------------------------------------------------------------------------
        // Start Notification Alarm
        //-------------------------------------------------------------------------------
        if (sharedPref.getBoolean(SettingsActivity.KEY_PREF_SWITCH_NOTIFICAION, true)) {
            myAlarm();
        } else {
            //Delete Notification Channels set
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                String id = "10001";
                mNotificationManager.deleteNotificationChannel(id);
            }
        }

        // Fire notification (only for testing)
        //NotificationHelper notificationHelper = new NotificationHelper(this);
        //notificationHelper.createNotification();

    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public void myAlarm() {
        int notificationId = 1;
        String notification_time = sharedPref.getString(SettingsActivity.KEY_PREF_NOTIFICATION_TIME, "20:00");
        String[] values = notification_time.split(":");


        //Calendar calendar = Calendar.getInstance();
        //SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
/*        try {
            calendar.setTime(sdf.parse(notification_time));
        } catch (ParseException e) {
            e.printStackTrace();

        }*/

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, parseInt(values[0]));
        calendar.set(Calendar.MINUTE, parseInt(values[1]));
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTime().compareTo(new Date()) < 0)
            calendar.add(Calendar.DAY_OF_MONTH, 1);

        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        }

    }

    //-------------------------------------------------------------------------------
    // Options Menu
    //-------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    //-------------------------------------------------------------------------------
    // Options Menu Selected
    //-------------------------------------------------------------------------------
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_aboutCheckmate) {
            Intent intent_settings_about = new Intent(this, SettingsAboutActivity.class);
            startActivity(intent_settings_about);
            return true;
        }
        if (item.getItemId() == R.id.action_settings) {
            Intent intent_settings = new Intent(this, SettingsActivity.class);
            startActivity(intent_settings);
            return true;
        }
        if (item.getItemId() == R.id.action_reload) {
            // User chose the "Favorite" action, mark the current item
            // as a favorite...
            startProgram();

            return true;
        }
        if (item.getItemId() == R.id.action_clear_data) {
            // Clear data storage
            SaveGlucoseData save = new SaveGlucoseData(this, this, new ArrayList<>());
            save.execute();
            recreate();
            return true;
        }
        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        startProgram();
    }

//    public static Date gmtToLocalDate(Date date) {
//        String timeZone = Calendar.getInstance().getTimeZone().getID();
//        Date local = new Date(date.getTime() + TimeZone.getTimeZone(timeZone).getOffset(date.getTime()));
//        return local;
//    }


    public void onDaysButtonClick(View view) {

        // Days Array
        int[] daysArray = new int[]{7, 15, 30, 90};

        // Get days from Shared Preferences
        int meanDays = parseInt(sharedPref.getString(KEY_PREF_MEAN_DAYS, "90"));

        // Get next element from array
        for (int i = 0; i < daysArray.length; i++) {
            if (meanDays == daysArray[3]) {
                meanDays = daysArray[0];
                break;
            }

            if (meanDays == daysArray[i]) {
                meanDays = daysArray[i + 1];
                break;
            }
        }

        // Store Number of Days in Shared Preferences
        sharedPref.edit().putString(KEY_PREF_MEAN_DAYS, String.valueOf(meanDays)).apply();
        sharedPref.edit().apply();

        CalcDayLineChartStats calcDayLineChartStats = new CalcDayLineChartStats(calcHba1c.getGlucoseData());
        calcDayLineChartStats.CalcMedian(meanDays);

        DayLineChartClass dayLineChart = new DayLineChartClass(this, calcHba1c.getHba1cData(), calcHba1c.getGlucoseData(), calcDayLineChartStats);
        dayLineChart.createMeanChart();
    }

    public void animateRefreshButton(Activity context) {
        // https://stackoverflow.com/questions/28840815/menu-item-animation-rotate-indefinitely-its-custom-icon
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            buttonSync = context.findViewById(R.id.action_reload);
            if (buttonSync != null && isSyncServiceRunning) {
                buttonSync.startAnimation(rotateAnimation);
            } else if (buttonSync != null) {
                buttonSync.clearAnimation();
            }
        }, 100);
    }

}
