package com.example.a20210207_checkmate2;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsAboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings_about);

        //Activate Hyperlink at About Checkmate Screen
        setupHyperlink();
    }

    private void setupHyperlink() {
        TextView linkTextView = findViewById(R.id.linkToCheckmate);
        linkTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}