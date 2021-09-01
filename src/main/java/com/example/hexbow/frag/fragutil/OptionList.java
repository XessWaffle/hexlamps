package com.example.hexbow.frag.fragutil;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;

import com.example.hexbow.R;
import com.example.hexbow.callback.CallbackInterface;
import com.example.hexbow.lamp.Swatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

public class OptionList extends View implements GestureDetector.OnGestureListener {

    public static final int DISTANCE = 100;
    public static final float MAX_FONT_SIZE = 48f;

    private LinkedHashMap<String, Integer> options;

    private String currentSelection;

    private FloatPropertyCompat yoffset;
    private int width, height, distance;
    private float fontSize;

    private Swatch color;

    private GestureDetectorCompat gdc;
    private boolean animate = false, onStart = false;

    private CallbackInterface ci;

    public OptionList(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        options = new LinkedHashMap<>();

        color = new Swatch(0,0,0,0);
        currentSelection = "";
        yoffset = new FloatPropertyCompat("Offset") {

            float offset;

            @Override
            public float getValue(Object object) {
                return offset;
            }

            @Override
            public void setValue(Object object, float value) {
                offset = value;
                invalidate();
            }
        };

        gdc = new GestureDetectorCompat(this.getContext(), this);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.OptionList,
                0, 0);

        try {
            color.fromInt(a.getColor(R.styleable.OptionList_color, Color.BLACK));
        } finally {
            a.recycle();
        }

        updateYPos();
    }

    public void setOnStart(boolean onStart){
        this.onStart = onStart;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(options.size() > 0) {
            calcFontSize();
            distance = Integer.MAX_VALUE;
            for (String key: options.keySet()) {
                drawText(canvas, key);
            }
        }
    }

    private void drawText(Canvas canvas, String text) {
        Paint textPaint = new Paint();
        textPaint.setTextAlign(Paint.Align.CENTER);

        int xPos = (width / 2);

        int yPos = (int) (this.options.get(text) + yoffset.getValue(this));

        while(yPos > DISTANCE * options.size()){
            yPos -= DISTANCE * options.size();
        }

        while(yPos < 0){
            yPos += DISTANCE * options.size();
        }

        if(yPos > height){
            return;
        }


        if(Math.abs(yPos - height/2) < Math.abs(distance)) {
            distance = yPos - height / 2;
            currentSelection = text;
        }


        int alpha = (int) ((double)(yPos)/(0.5 * height) * 255);

        if(alpha > 255) alpha = 255 - alpha;

        color.setAlpha(alpha);

        textPaint.setTextSize(fontSize > MAX_FONT_SIZE ? MAX_FONT_SIZE : fontSize);
        textPaint.setAntiAlias(true);
        textPaint.setColor(color.toInt());

        canvas.drawText(text, xPos, yPos, textPaint);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        boolean resizeWidth = widthSpecMode != MeasureSpec.EXACTLY;
        boolean resizeHeight = heightSpecMode != MeasureSpec.EXACTLY;

        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);

        width = parentWidth;
        height = parentHeight;

        setMeasuredDimension(width, height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(this.gdc.onTouchEvent(event)){
            return true;
        }

        if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_OUTSIDE || event.getAction() == MotionEvent.ACTION_CANCEL) {

            ci.onDismiss();

            int diff = height / 2 - options.get(currentSelection);

            ValueAnimator va = ValueAnimator.ofFloat(yoffset.getValue(this), diff);
            int mDuration = 1000; //in millis
            va.setDuration(mDuration);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    float offset = (Float) va.getAnimatedValue();
                    yoffset.setValue(this, offset);
                    invalidate();
                }
            });
            va.start();

            //return true;
        }

        return super.onTouchEvent(event);
    }

    public void addOption(String option){
        options.put(option, 0);
        calcFontSize();
        updateYPos();
    }

    public String getCurrentOption(){
        return currentSelection;
    }

    public void setCurrentOption(String option){
        if(options.keySet().contains(option)){
            currentSelection = option;
            Log.d("Yoff1", height + "" + options.get(option));
            yoffset.setValue(this, height/2 - options.get(option) + (onStart ? DISTANCE : 0));
            Log.d("Yoff2", yoffset.getValue(this) + "");
        }
    }

    public void removeOption(String option){
        options.remove(option);
        calcFontSize();
        updateYPos();
    }

    private void calcFontSize() {

        this.fontSize = 0;

        for (String option : options.keySet()) {

            Paint textPaint = new Paint();
            textPaint.setTextAlign(Paint.Align.CENTER);

            //((textPaint.descent() + textPaint.ascent()) / 2) is the distance from the baseline to the center.
            Rect bounds = new Rect();
            textPaint.getTextBounds(option , 0, option.length(), bounds);

            this.fontSize += 48f * Math.min(width / 6, height / 6) / (Math.max(bounds.width(), bounds.height()));
        }
        this.fontSize /= options.size();


    }

    private void updateYPos() {

        Paint textPaint = new Paint();
        textPaint.setTextAlign(Paint.Align.CENTER);

        textPaint.setTextSize(fontSize);
        int check = 0;

        for (String key: options.keySet()) {
            options.put(key, ((int) ((check++) * DISTANCE - ((textPaint.descent() + textPaint.ascent()) / 2))));
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        yoffset.setValue(this, yoffset.getValue(this) - distanceY);


        invalidate();



        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        FlingAnimation flingAnim = new FlingAnimation(this, yoffset)
                // Sets the start velocity to -2000 (pixel/s)
                .setStartVelocity(velocityY)
                .setFriction(0.5f);

        flingAnim.addEndListener(new DynamicAnimation.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd(DynamicAnimation animation, boolean canceled, float value, float velocity) {
                int diff = height / 2 - options.get(currentSelection);

                ValueAnimator va = ValueAnimator.ofFloat(yoffset.getValue(this), diff);
                int mDuration = 1000; //in millis
                va.setDuration(mDuration);
                va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float offset = (Float) va.getAnimatedValue();
                        yoffset.setValue(this, offset);
                        invalidate();
                    }
                });
                va.start();

                ci.onDismiss();
            }
        });

        flingAnim.start();

        return true;
    }

    public void setCallbackInterface(CallbackInterface ci){
        this.ci = ci;
    }
}
