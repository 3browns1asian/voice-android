package com.voice.voice;

import android.content.Context;
import android.graphics.Color;
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
import android.os.Vibrator;

import com.github.nkzawa.emitter.Emitter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
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
    private View statusRectangle;
    private boolean sendData;
    private boolean statusFlag = false;
    Vibrator vibrate;
    long[] pattern = {0, 100, 100, 100};


    private boolean aslSelected;
    private boolean connectedToGloves;
    private Timer timer;
    private String speech = "";
    private String[] speechArr;
    private ServerWrapper serverWrapper;

    private BluetoothSPP bt;
    private HashMap<String, String> JSLMapping = new HashMap<String, String>();

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

        statusRectangle = findViewById(R.id.rectangle_status);

        // Transcript view initialization
        transcriptView = (EditText) findViewById(R.id.transcriptEditText);
        transcriptView.setKeyListener(null);
        transcriptView.setEnabled(true);
        clearTranscriptButton = (Button) findViewById(R.id.clearTranscriptButton);
        clearTranscriptButton.setOnClickListener(clearTranscriptListener);

        // Export button initialization
        exportTxtButton = (Button) findViewById(R.id.exportToTxtButton);
        exportTxtButton.setOnClickListener(exportButtonListener);

        // JSL Character Mapping
        JSLMapping.put("sa", "サ");
        JSLMapping.put("yo", "ロ");
        JSLMapping.put("na", "ハ");
        JSLMapping.put("ra", "ワ");
        JSLMapping.put("ko", "こ");
        JSLMapping.put("n", "ん");
        JSLMapping.put("ni", "に");
        JSLMapping.put("chi", "ち");
        JSLMapping.put("wa", "は");



        bun = savedInstanceState;
        // Text To Speech Module Initializtion
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.US);
                tts.setSpeechRate(0.80f);
            }
        });

        vibrate = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);

        bluetoothSetup();

        serverWrapper = new ServerWrapper("https://afternoon-lowlands-52437.herokuapp.com/");
        serverWrapper.getmSocket().on("connect", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("Server Connected","yo");
            }
        }).on("predictedValue", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String message = String.valueOf(args[0]);
                if (!aslSelected) {
                    message = JSLMapping.get(message);
                }
                Log.d("Predicted Value", message);
                appendNewText(message);
            }
        });
        serverWrapper.initConnection();

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
                    // Marks the beginning of data transmission for a particular sign
                    if (statusFlag && connectedToGloves) {
                        statusFlag = false;
                        vibrate.vibrate(200);
                    }

                    // Vibrate and change traffic signal based on data we get from arduino
                    if (message.equals("END") && connectedToGloves && !sendData && bt.getConnectedDeviceAddress() != null) {
                        sendData = true;
                        statusRectangle.setBackgroundColor(Color.RED);
                        vibrate.vibrate(pattern, -1);
                        statusFlag = true;
                    } else if (message.equals("END") && connectedToGloves) {
                        statusRectangle.setBackgroundColor(Color.RED);
                        vibrate.vibrate(pattern, -1);
                        statusFlag = true;
                    } else if (connectedToGloves) {
                        statusRectangle.setBackgroundColor(Color.GREEN);
                    }

                    //send data to the server
                    if (sendData) {
                        serverWrapper.sendArduinoData(message.toString());
                    }

                    // make sure to stop sending the data when connection is off
                    if (!connectedToGloves && message.equals("END")) {
                        sendData = false;
                        statusRectangle.setBackgroundColor(Color.RED);
//                        vibrate.vibrate(pattern, -1);
                    }
                }
            });
        }
    }

    public void appendNewText(String t) {

        speech += t;
        speech += " ";
        final String word = t;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                transcriptView.setText(speech);
                tts.speak(word,TextToSpeech.QUEUE_FLUSH, bun, null);
            }
        });
    }

    RadioGroup.OnCheckedChangeListener languageListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            aslSelected = languageSwitch.getCheckedRadioButtonId() == ASLSetting.getId();
            if (aslSelected) {
                tts.setLanguage(Locale.US);
                Toast.makeText(getApplicationContext(), "ASL Selected", Toast.LENGTH_SHORT).show();
                serverWrapper.sendLanguageChangeNotification("ASL");
            } else {
                tts.setLanguage(Locale.JAPAN);
                Toast.makeText(getApplicationContext(), "JSL Selected", Toast.LENGTH_SHORT).show();
                serverWrapper.sendLanguageChangeNotification("JSL");
            }
            transcriptView.setText("");
            speech = "";
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
            speech = "";
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
