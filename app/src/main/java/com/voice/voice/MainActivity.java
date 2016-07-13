package com.voice.voice;

import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RadioGroup languageSwitch;
    private EditText transcriptView;
    private TextToSpeech tts;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        languageSwitch = (RadioGroup) findViewById(R.id.toggleLanguageSetting);
        transcriptView = (EditText) findViewById(R.id.transcriptEditText);
        transcriptView.setKeyListener(null);
        transcriptView.setEnabled(true);
        final Bundle bun = savedInstanceState;

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                String toSpeak = transcriptView.getText().toString();
                tts.setLanguage(Locale.US);

                tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, bun, null);
            }
        });


    }


}
