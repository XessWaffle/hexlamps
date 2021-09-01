package com.example.hexbow.frame;

import com.example.hexbow.callback.Instructable;
import com.example.hexbow.frag.fragutil.FrameAdapter;
import com.example.hexbow.serv.AppClient;

import java.util.ArrayList;
import java.util.LinkedList;

public class Animation implements Instructable {

    private LinkedList<Frame> frames;
    private FrameAdapter reference;

    private AppClient client;

    public Animation(){
        frames = new LinkedList<>();
    }

    public int getNumFrames(){
        return frames.size();
    }

    public void addFrame(Frame newFrame){
        for(int i = 0; i < newFrame.getLampSwatches().size(); i++) {
            client.sendCommand("addf " + newFrame.instruction(i + 1));
        }

        frames.add(newFrame);
        reference.notifyItemInserted(frames.size() - 1);
    }

    public void setFrame(int loc, Frame newFrame){

        for(int i = 0; i < newFrame.getLampSwatches().size(); i++) {
            client.sendCommand("setf " + loc + " " + newFrame.instruction(i + 1));
        }

        frames.set(loc, newFrame);
    }

    public Frame removeFrame(int location){

        client.sendCommand("remf " + location);

        reference.notifyItemRemoved(location);
        return frames.remove(location);
    }

    public Frame getFrame(int position) {
        return frames.get(position);
    }

    public void reorderFrames(int from, int loc){

        client.sendCommand("swaf " + from + ":" + loc);

        Frame frame = frames.remove(from);
        frames.add(loc, frame);
        reference.notifyItemMoved(from, loc);
    }

    public void resetIds(){
        for(int i = 0; i < frames.size(); i++){
            frames.get(i).setId(i);
        }
    }


    public FrameAdapter getReference() {
        return reference;
    }

    public void setReference(FrameAdapter reference) {
        this.reference = reference;
    }

    public String instruction(){

        String ret = "anim start \n";

        for(int i = 0; i < frames.size(); i++){
            ret += frames.get(i).instruction() + "\n";
        }

        ret += "end";

        return ret;
    }


    public AppClient getClient() {
        return client;
    }

    public void setClient(AppClient client) {
        this.client = client;
    }
}
