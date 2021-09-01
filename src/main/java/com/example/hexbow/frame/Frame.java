package com.example.hexbow.frame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.example.hexbow.callback.Instructable;
import com.example.hexbow.lamp.Lamp;
import com.example.hexbow.lamp.Swatch;

import java.util.ArrayList;

public class Frame implements Instructable {

    private static int frames = 0;

    private ArrayList<Swatch> lampSwatches;
    private int transitionMode, delay;
    private int id;

    private FrameView curr;

    public Frame(){

        lampSwatches = new ArrayList<Swatch>();

        for(int i = 0; i < 6; i++){
            lampSwatches.add(new Swatch());
        }

        transitionMode = Lamp.TRANSITION_MODE_DEFAULT;
        delay = 2500;

        id = frames++;
    }

    public Frame(Frame other){
        lampSwatches = new ArrayList<>(other.lampSwatches);

        transitionMode = other.transitionMode;
        delay = other.delay;
        id = -1;
    }


    public void setSwatch(int lampNumber, Swatch s){
        if(curr != null)
            curr.notifyViewUpdate();
        lampSwatches.set(lampNumber - 1, s);

    }

    public Swatch getSwatch(int lampNumber){
        return lampSwatches.get(lampNumber - 1);
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getTransitionMode() {
        return transitionMode;
    }

    public void setTransitionMode(int transitionMode) {
        this.transitionMode = transitionMode;
    }

    public ArrayList<Swatch> getLampSwatches() {
        return lampSwatches;
    }

    public void setLampSwatches(ArrayList<Swatch> lampSwatches) {
        this.lampSwatches = lampSwatches;
    }

    public void setFrameView(FrameView fv){
        this.curr = fv;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        if(curr != null){
            curr.notifyViewUpdate();
        }
    }

    public String instruction(){
        String ret = "";
        for(int i = 1; i < 7; i++){
            ret += ":l " + i + " " + getSwatch(i).instruction() + " ";
        }

        ret += ":t " + transitionMode + " ";
        ret += ":d " + delay;

        return ret;

    }

    public String instruction(int lamp){
        String ret = "";

        ret += ":l " + lamp + " " + getSwatch(lamp).instruction() + " ";


        ret += ":t " + transitionMode + " ";
        ret += ":d " + delay;

        return ret;
    }
}
