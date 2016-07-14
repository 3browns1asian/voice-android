package com.voice.voice;

import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RadioGroup languageSwitch;
    private RadioButton ASLSetting;
    private RadioButton JSLSetting;
    private RadioGroup connectionsSwitch;
    private RadioButton connectionOnSetting;
    private RadioButton connectionOffSetting;
    private EditText transcriptView;
    private Button clearTranscriptButton;
    private Button exportTxtButton;
    private TextToSpeech tts;

    private boolean aslSelected;
    private boolean connectedToGloves;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // This initializes the Language settings
        languageSwitch = (RadioGroup) findViewById(R.id.toggleLanguageSetting);
        ASLSetting = (RadioButton) findViewById(R.id.ASLSetting);
        JSLSetting = (RadioButton) findViewById(R.id.JSLSetting);
        aslSelected = languageSwitch.getCheckedRadioButtonId() == ASLSetting.getId();
        languageSwitch.setOnCheckedChangeListener(languageListener);

        // This initializes the Connections settings
        connectionsSwitch = (RadioGroup) findViewById(R.id.toggleConnectionSetting);
        connectionOnSetting = (RadioButton) findViewById(R.id.connectionOnSetting);
        connectionOffSetting = (RadioButton) findViewById(R.id.connectionOffSetting);
        connectionsSwitch.setOnCheckedChangeListener(connectionListener);

        // Transcript view initialization
        transcriptView = (EditText) findViewById(R.id.transcriptEditText);
        transcriptView.setKeyListener(null);
        transcriptView.setEnabled(true);
        clearTranscriptButton = (Button) findViewById(R.id.clearTranscriptButton);
        clearTranscriptButton.setOnClickListener(clearTranscriptListener);

        // Export button initialization
        exportTxtButton = (Button) findViewById(R.id.exportToTxtButton);
        exportTxtButton.setOnClickListener(exportButtonListener);

        final Bundle bun = savedInstanceState;
        // Text To Speech Module Initializtion
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                String toSpeak = transcriptView.getText().toString();
                tts.setLanguage(Locale.US);

                tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, bun, null);
            }
        });

    }

    RadioGroup.OnCheckedChangeListener languageListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            aslSelected = languageSwitch.getCheckedRadioButtonId() == ASLSetting.getId();
            if (aslSelected) {
                Toast.makeText(getApplicationContext(), "ASL Selected", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "JSL Selected", Toast.LENGTH_SHORT).show();
            }
        }
    };

    RadioGroup.OnCheckedChangeListener connectionListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            connectedToGloves = connectionsSwitch.getCheckedRadioButtonId() == connectionOnSetting.getId();
            if (connectedToGloves) {
                Toast.makeText(getApplicationContext(), "Connected to the gloves", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Disconnected from the gloves", Toast.LENGTH_SHORT).show();
            }
        }
    };

    View.OnClickListener clearTranscriptListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            transcriptView.setText("");
            Toast.makeText(getApplicationContext(),"Successfully exported text file to Downloads Folder",
                    Toast.LENGTH_SHORT).show();
        }
    };


    View.OnClickListener exportButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String content = transcriptView.getText().toString();
            FileOutputStream outputStream;

            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File myFile = new File(path, "transcript.txt");

                try {
                    //Make sure Path Exists
                    path.mkdirs();

                    if (myFile.exists ()) myFile.delete ();
                    myFile.createNewFile();
                    outputStream = new FileOutputStream(myFile);
                    outputStream.write(content.getBytes());
                    outputStream.close();
                    Toast.makeText(getApplicationContext(),"Successfully exported text file to Downloads Folder",
                            Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    };


}
