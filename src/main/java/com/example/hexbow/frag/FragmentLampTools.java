package com.example.hexbow.frag;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hexbow.R;
import com.example.hexbow.callback.CallbackInterface;
import com.example.hexbow.frag.fragutil.OptionList;
import com.example.hexbow.frag.fragutil.SwatchAdapter;
import com.example.hexbow.lamp.Lamp;
import com.example.hexbow.lamp.Swatch;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;

public class FragmentLampTools extends DialogFragment {

    public static final int MAX_DELAY = 5000;

    private Lamp toManage;
    private ColorPickerView cpv;
    private OptionList transitionGroup, swatchGroup, delayGroup;

    private RecyclerView swatchView;
    private SwatchAdapter sAdapt;

    private CallbackInterface ci;
    private FragmentDirect resource;

    private Swatch curr;

    public FragmentLampTools(Lamp l){
        toManage = l;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.LampToolsDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView =  getActivity().getLayoutInflater().inflate(R.layout.fragment_lamp_tools, container);

        setupRecyclerList(rootView);
        setupOptionLists(rootView);

        cpv = (ColorPickerView) rootView.findViewById(R.id.cpv);

        initializeClickListeners();

        return rootView;
    }


    private void setupRecyclerList(View rootView) {
        sAdapt = new SwatchAdapter(toManage);

        swatchView = (RecyclerView) rootView.findViewById(R.id.swatchView);

        swatchView.setAdapter(sAdapt);
        swatchView.setLayoutManager(new LinearLayoutManager(rootView.getContext(), LinearLayoutManager.VERTICAL, false));

        ItemTouchHelper mIth = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        toManage.reorderSwatches(viewHolder.getAdapterPosition(), target.getAdapterPosition(), true);

                        if(resource.isSynced()){
                            resource.update(toManage);
                        }

                        return true;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        toManage.removeSwatch(viewHolder.getAdapterPosition(), true);

                        if(resource.isSynced()){
                            resource.update(toManage);
                        }
                    }

                });

        mIth.attachToRecyclerView(swatchView);

        toManage.setSwatchAdapter(sAdapt);
    }

    private void setupOptionLists(View rootView) {

        transitionGroup = (OptionList) rootView.findViewById(R.id.transOpts);
        transitionGroup.addOption("Linear");
        transitionGroup.addOption("Flash");
        transitionGroup.addOption("Smooth");

        transitionGroup.setOnStart(true);

        transitionGroup.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        transitionGroup.setCurrentOption(Lamp.getTransitionString(toManage.getTransitionMode()));

        swatchGroup = (OptionList) rootView.findViewById(R.id.swatchOpts);
        swatchGroup.addOption("Loop");
        swatchGroup.addOption("Random");
        swatchGroup.addOption("FlipFlop");

        swatchGroup.setOnStart(true);

        swatchGroup.setCurrentOption(Lamp.getSwatchString(toManage.getSwatchMode()));


        delayGroup = (OptionList) rootView.findViewById(R.id.delayOpts);
        delayGroup.setOnStart(true);
        for(int i = 1; i <= 250; i++){
            delayGroup.addOption(i * 20 + "");
        }


        delayGroup.setCurrentOption(toManage.getDelay() + "");
    }


    private void initializeClickListeners() {
        cpv.addOnColorSelectedListener(new OnColorSelectedListener() {
            @Override
            public void onColorSelected(int selectedColor) {
                curr = new Swatch();
                curr.fromInt(selectedColor);

                toManage.addSwatch(curr, true);

                if (resource.isSynced()) {
                    resource.update(toManage);
                }
            }
        });

        transitionGroup.setCallbackInterface(new CallbackInterface() {
            @Override
            public void onDismiss() {

                String mode = transitionGroup.getCurrentOption();

                toManage.setTransitionMode(Lamp.getTransitionMode(mode), true);

                if(resource.isSynced()){
                    resource.update(toManage);
                }

                Log.d("TransOptList", mode);
            }

            @Override
            public void onDismiss(int... args) {

            }
        });

        swatchGroup.setCallbackInterface(new CallbackInterface() {
            @Override
            public void onDismiss() {

                String mode = swatchGroup.getCurrentOption();

                toManage.setSwatchMode(Lamp.getSwatchMode(mode), true);

                if(resource.isSynced()){
                    resource.update(toManage);
                }

                Log.d("SwatchOptList", mode);

            }

            @Override
            public void onDismiss(int... args) {

            }
        });

        delayGroup.setCallbackInterface(new CallbackInterface() {
            @Override
            public void onDismiss() {
                toManage.setDelay(Integer.parseInt(delayGroup.getCurrentOption()), true);

                if(resource.isSynced()){
                    resource.update(toManage);
                }
            }

            @Override
            public void onDismiss(int... args) {

            }
        });

    }

    @Override
    public void onDestroyView() {
        ci.onDismiss();
        super.onDestroyView();
    }

    public void setCallback(CallbackInterface ci){
        this.ci = ci;
    }

    public FragmentDirect getResource() {
        return resource;
    }

    public void setResource(FragmentDirect resource) {
        this.resource = resource;
    }

}
