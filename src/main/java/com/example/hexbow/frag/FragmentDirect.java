package com.example.hexbow.frag;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.hexbow.R;
import com.example.hexbow.callback.CallbackInterface;
import com.example.hexbow.lamp.Lamp;
import com.example.hexbow.lamp.Swatch;
import com.example.hexbow.serv.AppClient;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.ArrayList;

public class FragmentDirect extends Fragment implements CallbackInterface {

    public ArrayList<Lamp> lamps;
    public Button sync, power, refresh;
    public boolean synced, on;

    public ArrayList<FragmentLampTools> fltArray;

    private AppClient ref;

    public FragmentDirect(AppClient ref) {
        super(R.layout.fragment_direct);
        synced = false;
        lamps = new ArrayList<Lamp>();
        fltArray = new ArrayList<FragmentLampTools>();

        this.ref = ref;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_direct, container, false);

        lamps.add((Lamp) rootView.findViewById(R.id.lamp1));
        lamps.add((Lamp) rootView.findViewById(R.id.lamp2));
        lamps.add((Lamp) rootView.findViewById(R.id.lamp3));
        lamps.add((Lamp) rootView.findViewById(R.id.lamp4));
        lamps.add((Lamp) rootView.findViewById(R.id.lamp5));
        lamps.add((Lamp) rootView.findViewById(R.id.lamp6));

        for(int i = 0; i < lamps.size(); i++){
            lamps.get(i).setClient(ref);
            lamps.get(i).setLampNumber(i + 1);
            lamps.get(i).setOnline(true);


            fltArray.add(new FragmentLampTools(lamps.get(i)));
            fltArray.get(i).setCallback(this);
            fltArray.get(i).setResource(this);
        }


        sync = (Button) rootView.findViewById(R.id.sync);
        power = (Button) rootView.findViewById(R.id.power);
        refresh = (Button) rootView.findViewById(R.id.refresh);

        initializeClickListeners(rootView);

        return rootView;

    }

    private void initializeClickListeners(View rootView) {
        for(Lamp l: lamps){
            l.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Swatch color = new Swatch((int)(Math.random() * 255),(int)(Math.random() * 255), (int)(Math.random() * 255), 255);

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
                                    addressed.setDirSwatch(new Swatch(color), true, true);

                                    if(synced) {
                                        update(addressed);
                                    }

                                }
                            })
                            .lightnessSliderOnly()
                            .build()
                            .show();


                }
            });

            l.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    //switch to lamp fragment

                    for(Lamp l: lamps){
                        l.hideNumber();
                        l.invalidate();
                    }

                    DialogFragment newFragment = fltArray.get(lamps.indexOf(l));
                    newFragment.show(getFragmentManager(), "dialog");

                    return false;
                }
            });
        }

        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                synced = !synced;

                if(synced) {

                    sync.setText("DeSync");

                    update(lamps.get(0));
                } else {
                    sync.setText("Sync");
                }

            }
        });

        power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Send signal to lamp

                if(on){
                    on = false;
                    power.setText("Off");

                    for(Lamp l: lamps){
                        l.setOn(false, true, true);
                        l.invalidate();
                    }

                } else {
                    on = true;
                    power.setText("On");

                    for(Lamp l: lamps){
                        l.setOn(true, true, true);
                        l.invalidate();
                    }
                }
            }
        });

        refresh.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                for(Lamp l: lamps){
                    l.requestInformation();
                }

            }
        });

    }

    @Override
    public void onDismiss() {
        for(Lamp l: lamps){
            l.showNumber();
            l.invalidate();
        }
    }

    public void update(Lamp l){
        for (Lamp lamp : lamps) {
            lamp.sync(l);
            lamp.setDrawMode(Lamp.DRAW_MODE_SYNC);

            lamp.invalidate();
        }
    }

    public boolean isSynced(){
        return synced;
    }

    @Override
    public void onDismiss(int... args) {

    }
}
