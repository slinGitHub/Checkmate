package com.example.a20210207_checkmate2;

import android.os.AsyncTask;

import com.opencsv.CSVReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class ReadGlucoseEntry extends AsyncTask<String, ArrayList<GlucoseEntry>, Void> {

    private ArrayList<GlucoseEntry> values = new ArrayList<GlucoseEntry>();
    public AsyncResponse delegate = null;

    public ReadGlucoseEntry(AsyncResponse delegate){
        this.delegate=delegate;
    }

    @Override
    protected Void doInBackground(String... params) {

        try {
            //create url object to point to the file location on internet
            URL url = new URL(params[0]);
            //make a request to server
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            //get InputStream instance
            InputStream is = con.getInputStream();

            CSVReader reader = new CSVReader(new InputStreamReader(is));
            String[] nextLine;

            //Set Date Format
            Date date;

            //read content of the file line by line
            while ((nextLine = reader.readNext()) != null) {
                //Epoch to Java Date
                date = new Date(Long.parseLong(nextLine[1]));
                values.add(new GlucoseEntry(nextLine[0], date, Integer.valueOf(nextLine[2]), nextLine[3], nextLine[4]));
            }

        } catch (Exception e) {
            e.printStackTrace();
            //close dialog if error occurs
        }
        int i = 0;
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        delegate.processFinish(values, true);
        //MainActivity.buildGraph(values);
    }

}
