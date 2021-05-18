package com.example.a20210207_checkmate2;

import java.util.ArrayList;

public interface AsyncResponse {
    void processFinish(ArrayList<GlucoseEntry> output,boolean saveData);
}
