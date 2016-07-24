package com.voice.voice;

import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
/**
 * Created by amish on 20/07/16.
 */
public class ServerWrapper {

    private Socket mSocket;

    public ServerWrapper(String url){
        try {
            mSocket = IO.socket(url);
//            initConnection();
            initReceivers();
        } catch (Exception e) {
            Log.e("Server Error", e.getMessage());
        }
    }

    public Socket getmSocket() {
        return mSocket;
    }

    public void initConnection() {
        if (mSocket != null && !mSocket.connected()) {
            mSocket.connect();
            Log.d("YOOO", "boo");
        }
        Log.d("Connection Established", "Bruh");
    }


    public void sendArduinoData(String data) {
        if (mSocket != null && mSocket.connected()) {
            mSocket.emit("sendSensorData", data);
            Log.d("Message Sent", data);
        }
//        Log.d("Send Arduino data", data);
    }

    private void initReceivers() {

//        mSocket.on("predictedValue", new Emitter.Listener() {
//            @Override
//            public void call(Object... args) {
//                String message = String.valueOf(args[0]);
//                Log.d("Predicted Value", message);
//            }
//        });

        mSocket.on("receivedSensorData", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("Received Message back", String.valueOf(args[0]));
            }
        });
    }

}
