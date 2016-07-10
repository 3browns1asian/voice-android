package com.voice.voice;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    private RadioGroup languageSwitch;
    private EditText transcriptView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        languageSwitch = (RadioGroup) findViewById(R.id.toggleLanguageSetting);
        transcriptView = (EditText) findViewById(R.id.transcriptEditText);
        transcriptView.setKeyListener(null);
        transcriptView.setEnabled(true);
    }


}
