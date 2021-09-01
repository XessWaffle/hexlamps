package com.example.hexbow.serv;

import android.util.Log;

import com.example.hexbow.lamp.Lamp;
import com.example.hexbow.lamp.Palette;
import com.example.hexbow.lamp.Swatch;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

public class LampStateRequestTask implements Runnable{

    private Lamp ref;

    private InetAddress IP;
    private final int PN = 80;

    public LampStateRequestTask(Lamp ref){
        this.ref = ref;
    }

    @Override
    public void run() {
        /*
         * areq: app request
         * lrep: lamp reply
         * -l: Lamp
         * -d: Draw mode
         * -t: Transition mode
         * -s: Swatch mode
         * -w: (Wait) Delay
         * -p: Palette
         * -c: Color
         */

        StringBuilder data = new StringBuilder("");

        try {

            IP = InetAddress.getByAddress(new byte[]{42, 42, 42, 42});
            Socket client = new Socket(IP, PN);

            DataOutputStream dos = new DataOutputStream(client.getOutputStream());

            dos.writeBytes("areq " + ref.getLampNumber() + "\r");

            BufferedReader dis = new BufferedReader(new InputStreamReader(client.getInputStream()));

            String line = "";

            while (!(line = dis.readLine()).equals("done")) {
                data.append(line);
            }

            dos.close();
            dis.close();
            client.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("RESP " + ref.getLampNumber(), data.toString());


        if(data.toString().equals("nc")){
            return;
        }

        try {
            String check = data.toString().substring(data.indexOf("lrep"));

            String[] cmds = check.split(":");
            boolean proceed = true;

            for (int i = 1; i < cmds.length; i++) {

                String cmd = cmds[i];

                String alias = cmd.substring(0, 1);
                cmd = cmd.substring(1).trim();

                Log.d("CMD " + alias, cmd);

                if (proceed) {
                    if (alias.equals("l")) {
                        if (ref.getLampNumber() != Integer.parseInt(cmd)) {
                            proceed = false;
                        }
                    } else if (alias.equals("d")) {
                        ref.setDrawMode(Integer.parseInt(cmd));
                    } else if (alias.equals("t")) {
                        ref.setTransitionMode(Integer.parseInt(cmd), false);
                    } else if (alias.equals("s")) {
                        ref.setSwatchMode(Integer.parseInt(cmd), false);
                    } else if (alias.equals("w")) {
                        ref.setDelay(Integer.parseInt(cmd), false);
                    } else if (alias.equals("p")) {

                        if(!cmd.equals("")) {
                            String[] palette = cmd.split(",");
                            Palette nPalette = new Palette();

                            for (String color : palette) {
                                Swatch s = new Swatch();
                                s.fromInt(Integer.parseInt(color));
                                nPalette.addSwatch(s);
                            }

                            ref.setOnlineSwatches(nPalette, false);
                        }


                    } else if (alias.equals("c")) {
                        Swatch curr = new Swatch();
                        curr.fromInt(Integer.parseInt(cmd));

                        ref.setDirSwatch(curr, false, false);
                    }
                }
            }

            ref.invalidate();

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("LampStateReq", "Malformed Response " + ref.getLampNumber());
        }

    }
}
