package com.example.hexbow.frag;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hexbow.R;
import com.example.hexbow.callback.CallbackInterface;
import com.example.hexbow.frag.fragutil.FrameAdapter;
import com.example.hexbow.frame.Animation;
import com.example.hexbow.frame.Frame;
import com.example.hexbow.frag.fragutil.OptionList;
import com.example.hexbow.lamp.Lamp;
import com.example.hexbow.lamp.Swatch;
import com.example.hexbow.serv.AppClient;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.ArrayList;

public class FragmentFrame extends Fragment {

    private Animation anim;
    private Frame currFrame;

    private ArrayList<Lamp> lampModifiers;
    private OptionList delayOpts, transitionOpts;
    private Button addFrame, send;
    private FrameAdapter frameAdapter;
    private RecyclerView frameView;

    private AppClient ref;

    private boolean editing = false, playing = false;
    private int currentFrameEditing;

    public FragmentFrame(AppClient ref){
        lampModifiers = new ArrayList<Lamp>();
        currFrame = new Frame();
        anim = new Animation();

        frameAdapter = new FrameAdapter(anim);
        anim.setReference(frameAdapter);
        anim.setClient(ref);

        this.ref = ref;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_frame, container, false);

        setupLamps(rootView);
        setupButtons(rootView);
        setupOptionLists(rootView);
        setupRecycler(rootView);

        initializeClickListeners(rootView);

        return rootView;
    }

    private void setupButtons(View rootView) {
        addFrame = (Button) rootView.findViewById(R.id.addFrame);
        send = (Button) rootView.findViewById(R.id.send);
    }

    private void setupRecycler(View rootView) {

        frameView = (RecyclerView) rootView.findViewById(R.id.recyclerFrame);

        frameView.setAdapter(frameAdapter);
        frameView.setLayoutManager(new LinearLayoutManager(rootView.getContext(), LinearLayoutManager.VERTICAL, false));

        ItemTouchHelper mIth = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        anim.reorderFrames(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                        anim.resetIds();
                        recyclerView.invalidate();
                        return true;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                        if(direction == ItemTouchHelper.RIGHT) {
                            anim.removeFrame(viewHolder.getAdapterPosition());
                            anim.resetIds();
                        } else if(direction == ItemTouchHelper.LEFT) {

                            Frame copy = new Frame(anim.getFrame(viewHolder.getAdapterPosition()));
                            anim.addFrame(copy);
                            anim.resetIds();
                            frameAdapter.notifyDataSetChanged();
                        }

                    }

                });

        mIth.attachToRecyclerView(frameView);

        FrameAdapter.setCallbackInterface(new CallbackInterface() {
            @Override
            public void onDismiss() {

            }

            @Override
            public void onDismiss(int... args) {

                if(args.length == 1) {
                    currFrame = anim.getFrame(args[0]);
                    currentFrameEditing = args[0];
                    editing = true;

                    for (Lamp l : lampModifiers) {
                        l.setDirSwatch(currFrame.getSwatch(l.getLampNumber()), true, true);
                        l.invalidate();
                    }

                    Log.d("Frame", parseTransitionModes());

                    transitionOpts.setCurrentOption(parseTransitionModes());

                    delayOpts.setCurrentOption(currFrame.getDelay()  + "");

                    addFrame.setText("✓");
                }
            }
        });


    }

    private void initializeClickListeners(View rootView) {
        for(Lamp l: lampModifiers) {
            l.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Swatch color = new Swatch((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255), 255);

                    ColorPickerDialogBuilder
                            .with(rootView.getContext(), R.style.ColorPickerDialogTheme)
                            .setTitle("")
                            .initialColor(Color.WHITE)
                            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                            .density(12)
                            .setOnColorSelectedListener(new OnColorSelectedListener() {
                                @Override
                                public void onColorSelected(int selectedColor) {
                                    color.fromInt(selectedColor);

                                    Lamp addressed = (Lamp) v;
                                    addressed.setDirSwatch(new Swatch(color), false, true);
                                    addressed.invalidate();
                                }
                            })
                            .lightnessSliderOnly()
                            .build()
                            .show();


                }
            });
        }

        addFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(Lamp l : lampModifiers){
                    currFrame.setSwatch(l.getLampNumber(), l.getDirSwatch());
                    l.setDirSwatch(new Swatch(0,0,0,255), false, true);
                    l.invalidate();
                }


                if(!editing) {
                    anim.addFrame(currFrame);
                    anim.resetIds();
                    currFrame = new Frame();
                } else {
                    editing = false;
                    anim.setFrame(currentFrameEditing, currFrame);
                    currFrame = new Frame();
                    addFrame.setText("+");
                }
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //AnimationAdapter.parseAnimation
                //send to server;
                if(!playing) {
                    ref.sendCommand("anim start");
                    send.setText("Pause");
                    playing = true;
                } else {
                    ref.sendCommand("anim end");
                    send.setText("Play");
                    playing = false;
                }
            }
        });

        transitionOpts.setCallbackInterface(new CallbackInterface() {

            @Override
            public void onDismiss(int... args) {

            }

            @Override
            public void onDismiss() {
                currFrame.setTransitionMode(Lamp.getTransitionMode(transitionOpts.getCurrentOption()));
            }
        });

        delayOpts.setCallbackInterface(new CallbackInterface() {

            @Override
            public void onDismiss(int... args) {

            }

            @Override
            public void onDismiss() {
                currFrame.setDelay(Integer.parseInt(delayOpts.getCurrentOption()));
            }
        });

    }


    private void setupLamps(View rootView) {
        lampModifiers.add((Lamp) rootView.findViewById(R.id.frame_lamp1));
        lampModifiers.add((Lamp) rootView.findViewById(R.id.frame_lamp2));
        lampModifiers.add((Lamp) rootView.findViewById(R.id.frame_lamp3));
        lampModifiers.add((Lamp) rootView.findViewById(R.id.frame_lamp4));
        lampModifiers.add((Lamp) rootView.findViewById(R.id.frame_lamp5));
        lampModifiers.add((Lamp) rootView.findViewById(R.id.frame_lamp6));

        for(int i = 0; i < 6; i++){
            lampModifiers.get(i).setLampNumber(i + 1);
            lampModifiers.get(i).setOnline(false);
            lampModifiers.get(i).setOn(true, true, true);
        }

    }

    private void setupOptionLists(View rootView) {

        transitionOpts = (OptionList) rootView.findViewById(R.id.transOptsFrame);
        transitionOpts.addOption("Linear");
        transitionOpts.addOption("Flash");
        transitionOpts.addOption("Smooth");

        transitionOpts.setCurrentOption(parseTransitionModes());


        delayOpts = (OptionList) rootView.findViewById(R.id.delayOptsFrame);
        for(int i = 1; i <= 250; i++){
            delayOpts.addOption(i * 20 + "");
        }

        int delayWorkaround = currFrame.getDelay() - 60;
        if(delayWorkaround < 0){
            delayWorkaround += FragmentLampTools.MAX_DELAY;
        }

        delayOpts.setCurrentOption(delayWorkaround + "");
    }

    private String parseTransitionModes() {

        switch (currFrame.getTransitionMode()){
            case Lamp.TRANSITION_MODE_DEFAULT:
                return ("Linear");
            case Lamp.TRANSITION_MODE_FLASH:
                return ("Flash");
            case Lamp.TRANSITION_MODE_SMOOTH:
                return("Smooth");
        }

        return "";

    }
}
