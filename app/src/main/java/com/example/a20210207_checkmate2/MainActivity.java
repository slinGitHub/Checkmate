package com.example.a20210207_checkmate2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.Editable;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.gson.Gson;

import org.apache.commons.lang3.ObjectUtils;

import static com.example.a20210207_checkmate2.SettingsActivity.KEY_PREF_MEAN_DAYS;

public class MainActivity extends AppCompatActivity implements AsyncResponse{

    private String currentTheme;
    private SharedPreferences sharedPref;
    private Boolean initNightscoutURL;
    CalcHba1c calcHba1c;


    //-------------------------------------------------------------------------------
    //Reload Content after getting back to Main window
    //-------------------------------------------------------------------------------
    @Override
    public void onResume(){
        super.onResume();

        //Check if Theme was changed
        String theme = sharedPref.getString(SettingsActivity.KEY_PREF_COLOR_THEME, "");
        if (!currentTheme.equals(theme))
            recreate();

        //start Program
        startProgram();
    }

    //-------------------------------------------------------------------------------
    //Start Programm
    //-------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set Class for handling a crash
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this,MainActivity.class));

        initNightscoutURL = false;

        //Set Theme if changed
        //Load Default Preference Settings
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        currentTheme = sharedPref.getString("currentTheme","darkBlue");

        String theme = sharedPref.getString(SettingsActivity.KEY_PREF_COLOR_THEME, "darkBlue");
        if (!currentTheme.equals(theme))
            //Update current Theme
            sharedPref.edit().putString("currentTheme",theme).apply();
            setAppTheme(theme);

        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setIcon(R.drawable.ic_action_checkmateactionbarowl); //Show Icon in Toolbar

        startProgram();
    }

    public void setAppTheme (String theme) {
        switch(theme) {
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

    public void startProgram(){

        //-------------------------------------------------------------------------------
        //Load local copy of glucose data
        //-------------------------------------------------------------------------------
        //If local data can be retrieved load first local and then update data
        ArrayList<GlucoseEntry> glucoseDataRaw = null;

        try {
            FileInputStream fis;
            fis = this.openFileInput("glucoseDataRaw.dat");
            BufferedInputStream bfis = new BufferedInputStream(fis);
            ObjectInputStream is = new ObjectInputStream(bfis);
            glucoseDataRaw = (ArrayList<GlucoseEntry>) is.readObject();
            is.close();
            fis.close();

            //If local data was found -> load it
            if (glucoseDataRaw.size() > 0)
                processFinish(glucoseDataRaw, false);

            } catch (FileNotFoundException fileNotFoundException){
                fileNotFoundException.printStackTrace();
            } catch (IOException ioException){
                ioException.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        //-------------------------------------------------------------------------------
        //Goto Nightscout and download data + reload local saved data
        //-------------------------------------------------------------------------------

        //Check if Network Connection is up to Date
        if(isNetworkConnected()) {

            //Load saved settings from shared preferences
            SharedPreferences.Editor editor = sharedPref.edit();

            //Check if nightscout website is already set in preferences
            //Get the URL string from settings
            String nightscoutURLPref = sharedPref.getString(SettingsActivity.KEY_PREF_NIGHTSCOUT_URL, ""); //Does not work right now

            if ((nightscoutURLPref.equals("Enter your Nightscout URL here (https://YOUR_NIGHTSCOUTPAGE.herokuapp.com/)")) && (initNightscoutURL == false)){
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

                alertName.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        EditText txt = editTextName1; // variable to collect user input
                        editor.putString(SettingsActivity.KEY_PREF_NIGHTSCOUT_URL, txt.getText().toString());
                        editor.apply();
                        startProgram();
                    }
                });

                alertName.show(); // display the dialog
            } else {

                //Max number of datapoints to receive from Nightscout
                int nightscoutMaxDataPoints = Integer.parseInt(sharedPref.getString(SettingsActivity.KEY_PREF_DATA_POINTS,"30000"));

                //Enter String to Nightscout Data
                //Data formation example : https://github.com/nightscout/cgm-remote-monitor/blob/master/swagger.json
                String url = nightscoutURLPref + "/api/v1/entries/sgv.csv?count=" + nightscoutMaxDataPoints + "&find[dateString][$gte]=2015-08-28"; //All Data
                //String url = nightscoutURLPref + "/api/v1/entries/sgv.csv?count=100000&find[dateString][$gte]=2015-08-28"; //Short Dataset
                //String urlVeryShort = nightscoutURLPref + "/api/v1/entries/sgv.csv"; //Very Short Dataset

                Toast.makeText(this, "Connecting to " + nightscoutURLPref, Toast.LENGTH_SHORT).show();

                //Try to get data from Nightscout
                ReadGlucoseEntry task = new ReadGlucoseEntry(this);
                task.execute(url);
            }
        }
    }

    //-------------------------------------------------------------------------------
    //Return data if possible from Nightscout and calculate Values
    //-------------------------------------------------------------------------------
    @Override
    public void processFinish(ArrayList<GlucoseEntry> glucoseDataRaw, boolean saveData) {

        if (glucoseDataRaw!=null){
            if(glucoseDataRaw.size() > 0) {
                //-------------------------------------------------------------------------------
                //Save Glucose Data to local file for faster startup (in separate thread)
                //-------------------------------------------------------------------------------
                if (saveData) {
                    SaveGlucoseData save = new SaveGlucoseData(this, this, glucoseDataRaw);
                    save.execute();
                }

                //-------------------------------------------------------------------------------
                //Calculate HbA1c
                //-------------------------------------------------------------------------------
                calcHba1c = new CalcHba1c(glucoseDataRaw);
                calcHba1c.CalcHba1c(this);

                //Load saved settings from shared preferences
                calcHba1c.CalcAverageHba1c(calcHba1c.getHba1cData(), Integer.valueOf(sharedPref.getString(SettingsActivity.KEY_PREF_HBA1C_DAYS,"90")));

                //Call the chart creation algorithm
                createCharts(calcHba1c);
            } else {
                Toast.makeText(this, "No Glucose Data found..", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void createCharts(CalcHba1c calcHba1c){

        //-------------------------------------------------------------------------------
        //Create Line Chart
        //-------------------------------------------------------------------------------
        LineChartClass lineChart = new LineChartClass(this, calcHba1c.getHba1cData(), calcHba1c.daysGoingBackCount);
        lineChart.createChart(calcHba1c.getHba1cData().size()-1,true);
        lineChart.mChart.moveViewToX(calcHba1c.getHba1cData().size());
        //-------------------------------------------------------------------------------
        //Create Line Average Chart
        //-------------------------------------------------------------------------------
        LineChartAvgClass lineAvgChart = new LineChartAvgClass(this, calcHba1c.getHba1cData(),calcHba1c.getHba1cAverageData());
        lineAvgChart.createChart(false);
        //Get Y Max and Min from Line Chart
        YAxis YAxisLeft = lineChart.mChart.getAxisLeft();
        lineAvgChart.left.setAxisMaximum(YAxisLeft.mAxisMaximum);
        lineAvgChart.left.setAxisMinimum(YAxisLeft.mAxisMinimum);

        //-------------------------------------------------------------------------------
        //Create Bar Chart
        //-------------------------------------------------------------------------------
        BarChartClass barChart = new BarChartClass(this,calcHba1c.getHba1cData());
        barChart.createChart(calcHba1c.getHba1cData().size()-1,true);
        barChart.bChart.moveViewToX(calcHba1c.getHba1cData().size());
        //-------------------------------------------------------------------------------
        //Create Bar Average Chart
        //-------------------------------------------------------------------------------
        BarChartAvgClass barAvgChart = new BarChartAvgClass(this, calcHba1c.getHba1cData(),calcHba1c.getHba1cAverageData());
        barAvgChart.createChart(calcHba1c.nDays, false);
        //Get Y Max and Min from Bar Chart
        YAxis bYAxisLeft = barChart.bChart.getAxisLeft();
        barAvgChart.bLeft.setAxisMaximum(bYAxisLeft.mAxisMaximum);
        barAvgChart.bLeft.setAxisMinimum(bYAxisLeft.mAxisMinimum);

        //-------------------------------------------------------------------------------
        //Create Day Line Chart
        //-------------------------------------------------------------------------------
        //Create Statistics (Mean and % in Range)
        CalcDayLineChartStats calcDayLineChartStats = new CalcDayLineChartStats(calcHba1c.getGlucoseData());

        int meanDays = Integer.valueOf(sharedPref.getString(KEY_PREF_MEAN_DAYS,"7"));
        calcDayLineChartStats.CalcMedian(meanDays);

        DayLineChartClass dayLineChart = new DayLineChartClass(this, calcHba1c.getHba1cData(), calcHba1c.getGlucoseData(), calcDayLineChartStats);
        dayLineChart.createChart(calcHba1c.getHba1cData().size()-1,true,calcHba1c.sgvMin, calcHba1c.sgvMax);

        //-------------------------------------------------------------------------------
        //Synchronize line and bar chart
        //-------------------------------------------------------------------------------
        lineChart.mChart.setOnChartGestureListener(new CoupleChartGestureListener(
                lineChart.mChart, new Chart[]{barChart.bChart}));
        barChart.bChart.setOnChartGestureListener(new CoupleChartGestureListener(
                barChart.bChart, new Chart[]{lineChart.mChart}));

        //Synchronize line chart gesture
        lineChart.mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener()
        {
            @Override
            public void onValueSelected(Entry e, Highlight h)
            {
                lineChart.createChart((int) e.getX(),true);
                lineAvgChart.createChart(false);

                barChart.createChart((int) e.getX(),true);
                barAvgChart.createChart(calcHba1c.nDays,false);

                dayLineChart.createChart((int) e.getX(),true,calcHba1c.sgvMin, calcHba1c.sgvMax);
            }

            @Override
            public void onNothingSelected()
            {
            }
        });

        //Synchronize line chart gesture
        lineAvgChart.mAChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener()
        {
            @Override
            public void onValueSelected(Entry e, Highlight h)
            {
                lineChart.createChart((int) e.getX(),false);
                lineAvgChart.createChart(true);

                barChart.createChart((int) e.getX(),false);
                barAvgChart.createChart(calcHba1c.nDays,true);

                //dayLineChart.createChart((int) e.getX(),true,calcHba1c.sgvMin, calcHba1c.sgvMax);
                dayLineChart.createMeanChart();
            }

            @Override
            public void onNothingSelected()
            {
            }
        });

        //Synchronize bar chart gesture
        barChart.bChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener()
        {
            @Override
            public void onValueSelected(Entry e, Highlight h)
            {
                lineChart.createChart((int) e.getX(),true);
                lineAvgChart.createChart(false);

                barChart.createChart((int) e.getX(),true);
                barAvgChart.createChart(calcHba1c.nDays,false);

                dayLineChart.createChart((int) e.getX(),true,calcHba1c.sgvMin, calcHba1c.sgvMax);
            }

            @Override
            public void onNothingSelected() {
            }
        });

        //Synchronize bar chart gesture
        barAvgChart.bAvgChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener()
        {
            @Override
            public void onValueSelected(Entry e, Highlight h)
            {
                lineChart.createChart((int) e.getX(),false);
                lineAvgChart.createChart(true);

                barChart.createChart((int) e.getX(),false);
                barAvgChart.createChart(calcHba1c.nDays,true);

                //dayLineChart.createChart((int) e.getX(),true,calcHba1c.sgvMin, calcHba1c.sgvMax);
                dayLineChart.createMeanChart();
            }

            @Override
            public void onNothingSelected() {
            }
        });


        //-------------------------------------------------------------------------------
        //Start Notification Alarm
        //-------------------------------------------------------------------------------
        if(sharedPref.getBoolean(SettingsActivity.KEY_PREF_SWITCH_NOTIFICAION,true)) {
            myAlarm();
        }else {
            //Delete Notification Channels set
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                String id = "10001";
                mNotificationManager.deleteNotificationChannel(id);
            }
        }


        //Fire notification (only for testing)
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
        calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(values[0]));
        calendar.set(Calendar.MINUTE, Integer.valueOf(values[1]));
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
    //Options Menue
    //-------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater ();
        inflater.inflate ( R.menu.menu,menu );
        return true;
    }

    //-------------------------------------------------------------------------------
    //Options Menue Selected
    //-------------------------------------------------------------------------------
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_aboutCheckmate:
                Intent intent_settings_about = new Intent(this, SettingsAboutActivity.class);
                startActivity(intent_settings_about);
                return true;

            case R.id.action_settings:
                Intent intent_settings = new Intent(this, SettingsActivity.class);
                startActivity(intent_settings);
                return true;

            case R.id.action_reload:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...

                startProgram();

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu){
        startProgram();
    }

    public static Date gmtToLocalDate(Date date) {

        String timeZone = Calendar.getInstance().getTimeZone().getID();
        Date local = new Date(date.getTime() + TimeZone.getTimeZone(timeZone).getOffset(date.getTime()));
        return local;
    }


    public void onDaysButtonClick(View view) {

        //Days Array
        int[] daysArray = new int[]{7,15,30,90};

        //Get days from Shared Preferences
        int meanDays = Integer.valueOf(sharedPref.getString(KEY_PREF_MEAN_DAYS,"90"));

        //Get next element from array
        for(int i=0;i<daysArray.length;i++){
            if (meanDays == daysArray[3]){
                meanDays = daysArray[0];
                break;
            }

            if (meanDays == daysArray[i]){
                meanDays = daysArray[i+1];
                break;}
        }

        //Store Number of Days in Shared Preferences
        sharedPref.edit().putString(KEY_PREF_MEAN_DAYS, String.valueOf(meanDays)).apply();
        sharedPref.edit().apply();

        CalcDayLineChartStats calcDayLineChartStats = new CalcDayLineChartStats(calcHba1c.getGlucoseData());
        calcDayLineChartStats.CalcMedian(meanDays);

        DayLineChartClass dayLineChart = new DayLineChartClass(this, calcHba1c.getHba1cData(), calcHba1c.getGlucoseData(), calcDayLineChartStats);
        dayLineChart.createMeanChart();
    }
}