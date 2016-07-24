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

import com.github.nkzawa.emitter.Emitter;

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
    private boolean sendData;

    private boolean aslSelected;
    private boolean connectedToGloves;
    private Timer timer;
    private String speech = "";
    private String[] speechArr;
    private ServerWrapper serverWrapper;

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
//        speechArr = speech.split(" ");


        bluetoothSetup();

        serverWrapper = new ServerWrapper("https://afternoon-lowlands-52437.herokuapp.com/");
        serverWrapper.getmSocket().on("connect", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String[] simData = simulateArduinoData();
                for (String str:simData){
//                    serverWrapper.sendArduinoData(str);
//                    bluetoothSetup();
                }
                Log.d("connected","yo");
            }
        }).on("predictedValue", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String message = String.valueOf(args[0]);
                Log.d("Predicted Value", message);
                appendNewText(message);
            }
        });
        serverWrapper.initConnection();


//        runTroughTranscript();

    }

    // TODO: Remove all this simulation stuff after
    public String[] simulateArduinoData() {
        String[] helloData = { "-8.30,1.36,5.66,0.07,-0.03,-0.05,346,358,355,337,352|0,0,0,0,0,0,0,0,0,0,0",
                "-8.89,0.85,6.21,0.01,0.10,0.19,346,357,355,336,351|0,0,0,0,0,0,0,0,0,0,0",
                "-8.23,2.46,2.84,0.46,0.28,-0.62,345,359,355,335,347|0,0,0,0,0,0,0,0,0,0,0",
                "-8.74,0.84,2.97,0.29,-0.22,-0.65,343,359,354,335,344|0,0,0,0,0,0,0,0,0,0,0",
                "-9.63,-1.81,4.16,0.14,-0.23,-0.49,345,359,356,335,347|0,0,0,0,0,0,0,0,0,0,0",
                "-9.43,-2.98,4.68,-0.15,0.07,0.06,342,359,354,335,347|0,0,0,0,0,0,0,0,0,0,0",
                "-9.40,-1.66,3.57,-0.31,0.18,0.26,344,359,355,336,345|0,0,0,0,0,0,0,0,0,0,0",
                "-10.06,-0.32,3.07,-0.65,0.18,0.04,344,360,356,336,348|0,0,0,0,0,0,0,0,0,0,0",
                "-9.04,4.12,3.17,-0.20,0.02,0.65,344,360,355,336,350|0,0,0,0,0,0,0,0,0,0,0",
                "-9.52,2.72,3.12,-0.02,-0.05,-0.07,344,359,355,336,349|0,0,0,0,0,0,0,0,0,0,0",
                "-9.60,1.50,2.94,-0.04,-0.01,-0.13,343,359,355,336,348|0,0,0,0,0,0,0,0,0,0,0",
                "END"};
        String[] byeData = { "-7.26,-1.18,6.42,-0.01,-0.03,-0.11,357,372,368,379,373|0,0,0,0,0,0,0,0,0,0,0",
                "-7.15,-1.22,6.67,-0.04,0.00,-0.06,357,371,369,379,374|0,0,0,0,0,0,0,0,0,0,0",
                "-7.19,-0.93,6.13,0.01,0.11,-0.05,354,373,360,368,359|0,0,0,0,0,0,0,0,0,0,0",
                "-7.62,-0.76,6.70,0.01,0.10,-0.02,436,379,402,437,424|0,0,0,0,0,0,0,0,0,0,0",
                "-8.10,-1.37,5.35,-0.23,0.58,-0.07,525,419,519,505,521|0,0,0,0,0,0,0,0,0,0,0",
                "-8.56,-0.91,5.18,0.03,0.09,-0.03,523,423,524,501,521|0,0,0,0,0,0,0,0,0,0,0",
                "-8.31,-1.31,5.55,-0.03,-0.03,-0.04,520,421,522,498,519|0,0,0,0,0,0,0,0,0,0,0",
                "-8.40,-1.41,5.36,-0.09,-0.00,-0.05,517,416,519,496,516|0,0,0,0,0,0,0,0,0,0,0",
                "-8.20,-1.41,5.50,-0.04,-0.00,-0.05,516,413,517,494,515|0,0,0,0,0,0,0,0,0,0,0",
                "-8.15,-1.54,5.57,-0.07,-0.00,-0.05,515,411,516,494,514|0,0,0,0,0,0,0,0,0,0,0",
                "-8.18,-1.31,5.36,-0.06,-0.00,-0.03,514,410,514,493,512|0,0,0,0,0,0,0,0,0,0,0",
                "END"};
        return byeData;
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
//                    Log.d("Data Received", message);
                    if (message.equals("END") && !sendData && bt.getConnectedDeviceAddress() != null) {
                        sendData = true;
                    }
                    if (sendData)
                        serverWrapper.sendArduinoData(message.toString());
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
//                runTroughTranscript();
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
