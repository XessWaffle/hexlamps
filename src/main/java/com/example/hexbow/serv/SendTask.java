package com.example.hexbow.serv;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Callable;

public class SendTask implements Runnable {
    private String cmd;
    private InetAddress IP;
    private final int PN = 80;
    public SendTask(String cmd){
        this.cmd = cmd;
    }

    @Override
    public void run() {

        try {
            IP = InetAddress.getByAddress(new byte[]{42, 42, 42, 42});
            Socket client = new Socket(IP, PN);

            DataOutputStream dos = new DataOutputStream(client.getOutputStream());

            dos.writeBytes(cmd + "\r");

            Log.d("Sending", cmd);

            dos.close();
            client.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
