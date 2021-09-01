package com.example.hexbow.serv;

import android.util.Log;

import com.example.hexbow.lamp.Lamp;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.prefs.PreferenceChangeEvent;

public class AppClient {

    private final ExecutorService sendManager;
    private final ExecutorService recieveManager;

    public AppClient(){

        sendManager = Executors.newSingleThreadExecutor();
        recieveManager = Executors.newCachedThreadPool();

    }

    public void requestInformation(Lamp l){
        recieveManager.execute(new LampStateRequestTask(l));
    }

    public void sendCommand(String cmd){
        sendManager.execute(new SendTask(cmd));
    }

}
