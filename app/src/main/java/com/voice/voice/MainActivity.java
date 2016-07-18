package com.voice.voice;

import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Timer;
import java.util.TimerTask;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

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
    private Bundle bun;

    private boolean aslSelected;
    private boolean connectedToGloves;
    private Timer timer;
    private String speech = "This is voice. It is my voice. It is your voice. It is voice for those that don’t have one. Voice is made to help people understand sign language. Right now, sign language translate machines are big and slow. We use machine learning and phone. Voice let people speak naturally, no matter where they are. Because it is just gloves and phone, Voice can give people without one, voice of their own.";
    private String[] speechArr;

    //TODO: Move to separate file
    private BluetoothSPP bt;

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

        bun = savedInstanceState;
        // Text To Speech Module Initializtion
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.US);
                tts.setSpeechRate(0.80f);
            }
        });

        // SetInterval to handle stream of data
        speechArr = speech.split(" ");


        bluetoothSetup();
        runTroughTranscript();

    }

    public void bluetoothSetup() {
        bt = new BluetoothSPP(getApplicationContext());
        if (!bt.isBluetoothAvailable() || !bt.isBluetoothEnabled()) {
            Log.d("Bruh", "Bluetoo is not available");
        } else {
            Log.d("Bruh", "Bluetooth is available");
            bt.setupService();
            bt.startService(BluetoothState.DEVICE_OTHER);
            final String[] pairedDevices = bt.getPairedDeviceAddress();
            Log.d("Paired Device",String.valueOf(bt.getPairedDeviceAddress().length));
            Log.d("Paired Device", bt.getPairedDeviceAddress()[0].toString());

            bt.connect(pairedDevices[0]);
            bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
                public void onDeviceConnected(String name, String address) {
                    // Do something when successfully connected
                    Log.d("Device connected", "Device Name: " + name + ", Address: " + address);
                }

                public void onDeviceDisconnected() {
                    // Do something when connection was disconnected
                }

                public void onDeviceConnectionFailed() {
                    // Do something when connection failed
                    Log.d("Device Failed", "Device Name: " );
                    bt.connect(pairedDevices[0]);
                }
            });
            bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
                public void onDataReceived(byte[] data, String message) {
                    Log.d("Data Received", message);
                }
            });
        }
    }

    public void runTroughTranscript() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask(){
            int count = 0;
            StringBuilder sent = new StringBuilder();

            @Override
            public void run(){
                if (count < speechArr.length) {
                    if (speechArr[count] == null) return;

//                    Log.d("Message", speechArr[count]);
                    tts.speak(speechArr[count],TextToSpeech.QUEUE_ADD, bun, null);
                    sent.append(speechArr[count]);
                    sent.append(" ");


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            Log.d("String", sent.toString());
                            transcriptView.setText(sent.toString());
                        }
                    });

// This is there to buffering it into a sentence before saying it out
//                    if ((speechArr[count].charAt(speechArr[count].length()-1)) == '.') {
//                        Log.d("YO", sentence);
//                        if (tts.isSpeaking()) {
//                            tts.speak(sentence, TextToSpeech.QUEUE_ADD, bun, null);
//                        } else {
//                            tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, bun, null);
//                        }
//                        sentence = "";
//                    }
                    count += 1;
                }
            }

        },0,500);
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
            if (timer != null) {
                timer.purge();
                timer.cancel();
                runTroughTranscript();
            }
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
