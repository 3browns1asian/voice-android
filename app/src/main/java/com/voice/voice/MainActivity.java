package com.voice.voice;

import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RadioGroup languageSwitch;
    private EditText transcriptView;
    private Button exportTxtButton;
    private TextToSpeech tts;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        languageSwitch = (RadioGroup) findViewById(R.id.toggleLanguageSetting);
        
        transcriptView = (EditText) findViewById(R.id.transcriptEditText);
        transcriptView.setKeyListener(null);
        transcriptView.setEnabled(true);
        exportTxtButton = (Button) findViewById(R.id.exportToTxtButton);
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

        exportTxtButton.setOnClickListener(exportButtonListener);


    }

    // Export the
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
