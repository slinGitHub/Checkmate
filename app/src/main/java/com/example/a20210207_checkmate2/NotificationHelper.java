package com.example.a20210207_checkmate2;

import static com.example.a20210207_checkmate2.Utils.getHba1c_mmol;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

class NotificationHelper implements AsyncResponse {

    private Context mContext;
    private static final String NOTIFICATION_CHANNEL_ID = "10001";
    private double Hba1c;

    NotificationHelper(Context context) {
        mContext = context;
        Hba1c = 0;
    }

    void setHba1c(double Hba1c) {
        this.Hba1c = Hba1c;
    }


    void createNotification() {
        // If local data can be retrieved load first local and then update data

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);

        String nightscoutURLPref = sharedPref.getString(SettingsActivity.KEY_PREF_NIGHTSCOUT_URL, "");
        int nightscoutMaxDataPoints = Integer.parseInt(sharedPref.getString(SettingsActivity.KEY_PREF_DATA_POINTS, "30000"));
        String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        String url = nightscoutURLPref + "/api/v1/entries/sgv.csv?count=" + nightscoutMaxDataPoints + "&find[dateString][$gte]=" + date;

        // Get Token if needed
        String nightscoutTokenPref = sharedPref.getString(SettingsActivity.KEY_PREF_NIGHTSCOUT_TOKEN, ""); // Does not work right now
        if (!nightscoutTokenPref.equals("Enter your token here for the readable role at your nightscout page."))
            url = url + "&token=" + nightscoutTokenPref;

        ReadGlucoseEntry task;
        task = new ReadGlucoseEntry(this);
        task.execute(url);
    }


/*            FileInputStream fis;
            fis = mContext.openFileInput("glucoseDataRaw.dat");
            BufferedInputStream bfis = new BufferedInputStream(fis);
            ObjectInputStream is = new ObjectInputStream(bfis);
            ArrayList<GlucoseEntry> glucoseDataRaw = (ArrayList<GlucoseEntry>) is.readObject();
            is.close();
            fis.close();

            CalcHba1c calcHba1c = new CalcHba1c(glucoseDataRaw);
            calcHba1c.CalcHba1c(mContext);

            hba1cValue = BigDecimal.valueOf(calcHba1c.getHba1cData().get(0).hba1c).setScale(1, BigDecimal.ROUND_HALF_DOWN).floatValue();
            inRange = BigDecimal.valueOf(calcHba1c.getHba1cData().get(0).inRange * 100).setScale(0, BigDecimal.ROUND_HALF_DOWN).floatValue(); //InRange

        } catch (FileNotFoundException fileNotFoundException){
            //Do Something
        } catch (IOException ioException){
            //Do Something
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }*/

    @Override
    public void processFinish(ArrayList<GlucoseEntry> glucoseDataRaw, boolean saveData) {

        int notificationId = 1;

        double hba1cValue = 0;
        float inRange = 0;

        CalcHba1c calcHba1c = new CalcHba1c(glucoseDataRaw);
        calcHba1c.CalcHba1c(mContext);

        hba1cValue = BigDecimal.valueOf(calcHba1c.getHba1cData().get(0).hba1c).setScale(1, BigDecimal.ROUND_HALF_DOWN).doubleValue();
        inRange = BigDecimal.valueOf(calcHba1c.getHba1cData().get(0).inRange * 100).setScale(0, BigDecimal.ROUND_HALF_DOWN).floatValue(); //InRange

        //Intent intent = new Intent(mContext , NotificationActivity.class);
        Intent intent = new Intent(mContext, MainActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext,
                notificationId /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID);
        mBuilder.setSmallIcon(R.drawable.notification_icon_owl2);

        //Get Values from Preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        Boolean switchToMol = sharedPref.getBoolean(SettingsActivity.KEY_PREF_SWITCH_GLUCOSE_MOL, false);
        float hba1cGoal = Float.parseFloat(sharedPref.getString(SettingsActivity.KEY_PREF_HBA1C_GOALS, "6.3"));
        float inRangeGoal = Float.parseFloat(sharedPref.getString(SettingsActivity.KEY_PREF_IN_RANGE_GOAL, "50"));

        hba1cValue = getHba1c_mmol(hba1cValue, switchToMol);

        mBuilder.setContentTitle("Your Hba1c today: " + hba1cValue + " / In Range: " + String.format("%.0f", inRange) + "%")
                .setAutoCancel(false)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(resultPendingIntent);

        if (hba1cValue <= hba1cGoal && inRange >= inRangeGoal)
            mBuilder.setContentText("You are doing great!");
        else if (hba1cValue <= hba1cGoal)
            mBuilder.setContentText("Well done! But try to stabilize your sugar level.");
        else if (inRange >= inRangeGoal)
            mBuilder.setContentText("You are on the right way! But try to lower your surf level.");
        else
            mBuilder.setContentText("This is one of these days, tomorrow will be better for sure!");

        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            assert mNotificationManager != null;
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(notificationId /* Request Code */, mBuilder.build());
    }

}
