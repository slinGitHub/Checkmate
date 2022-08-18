package com.example.a20210207_checkmate2;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class SaveGlucoseData extends AsyncTask<String, Void, Boolean> {

    private final ArrayList<GlucoseEntry> glucoseDataRaw;
    public AsyncResponse delegate;
    public Context context;
    boolean worked;

    public SaveGlucoseData(AsyncResponse delegate, Context context, ArrayList<GlucoseEntry> glucoseDataRaw) {

        this.delegate = delegate;
        this.context = context;
        worked = false;
        this.glucoseDataRaw = glucoseDataRaw;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        // your background code here. Don't touch any UI components

        //-------------------------------------------------------------------------------
        // Save Glucose Data to local file for faster startup
        //-------------------------------------------------------------------------------

        try {
            FileOutputStream fos = context.openFileOutput("glucoseDataRaw.dat", Context.MODE_PRIVATE);
            //FileOutputStream fos = new FileOutputStream("glucoseDataRaw.dat");
            BufferedOutputStream buffer = new BufferedOutputStream(fos);
            ObjectOutputStream os = new ObjectOutputStream(buffer);
            os.writeObject(glucoseDataRaw);
            os.close();
            fos.close();

            worked = true;

        } catch (FileNotFoundException fileNotFoundException) {
            // Do Something
        } catch (IOException ioException) {
            // Do Something
        }

        return worked;
    }

    @Override
    protected void onPostExecute(Boolean saveSuccesfull) {
        // This is run on the UI thread so you can do as you wish here
        if (worked) {
            //Toast.makeText(context, "Data saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Error while saving data", Toast.LENGTH_SHORT).show();
        }
    }
}
