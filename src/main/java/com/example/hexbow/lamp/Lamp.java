package com.example.hexbow.lamp;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.hexbow.frag.fragutil.SwatchAdapter;
import com.example.hexbow.serv.AppClient;

import java.util.ArrayList;

public class Lamp extends View {

    public static final int STROKE_WIDTH = 4;
    public static final int TRANSITION_MODE_SMOOTH = 1;
    public static final int TRANSITION_MODE_FLASH = 2;
    public static final int TRANSITION_MODE_DEFAULT = 3;

    public static final int SWATCH_MODE_RANDOM = 4;
    public static final int SWATCH_MODE_FLIPFLOP = 5;
    public static final int SWATCH_MODE_LOOP = 6;

    public static final int DRAW_MODE_ANIMATION = 7;
    public static final int DRAW_MODE_DIRECT = 8;
    public static final int DRAW_MODE_SYNC = 9;
    public static final int DRAW_MODE_CONTROLLED_ANIMATION = 10;

    private static int numLamps = 0;

    private Swatch dirSwatch;
    private Palette onlineSwatches;
    private SwatchAdapter sa;
    private AppClient comms;

    private int lampNumber;
    private int width, height;

    private boolean isOn, showNum, online, prevFade;

    private int swatchMode, transitionMode, drawMode;

    private int delay;

    private int textAlpha;

    public Lamp(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        lampNumber = ++numLamps;
        dirSwatch = new Swatch(0, 0, 0, 255);
        onlineSwatches = new Palette();
        isOn = false;
        showNum = true;
        online = false;

        swatchMode = SWATCH_MODE_LOOP;
        transitionMode = TRANSITION_MODE_DEFAULT;
        drawMode = DRAW_MODE_DIRECT;

        textAlpha = 0;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawHexagon(canvas, width/2,height/2, Math.min(width/2 - STROKE_WIDTH/2, height/2 - STROKE_WIDTH/2));
        drawText(canvas);
    }

    private void drawText(Canvas canvas) {
        Paint textPaint = new Paint();
        textPaint.setTextAlign(Paint.Align.CENTER);

        int xPos = (canvas.getWidth() / 2);

        //((textPaint.descent() + textPaint.ascent()) / 2) is the distance from the baseline to the center.
        Rect bounds = new Rect();
        textPaint.getTextBounds(lampNumber + "", 0, 1, bounds);

        float textSize = 48f * Math.min(width/12, height/12) / (Math.max(bounds.width(), bounds.height()));

        textPaint.setTextSize(textSize);
        textPaint.setColor(dirSwatch.swatch(1.1).setAlpha(textAlpha).grayScale().invert().toInt());
        textPaint.setAntiAlias(true);

        int yPos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2)) ;

        canvas.drawText(lampNumber + "", xPos, yPos, textPaint);

    }

    public void drawHexagon(Canvas canvas, int x, int y, int width) {

        Path path = new Path();
        path.moveTo((int)(x - width/2), (int)(y - width/2 * Math.sqrt(3))); // Top
        path.lineTo((int)(x - width/2 + width), (int)(y - width/2 * Math.sqrt(3))); // Bottom left
        path.lineTo((int)(x + width), (int)(y)); // Bottom right
        path.lineTo((int)(x - width/2 + width), (int)(y + width/2 * Math.sqrt(3)));
        path.lineTo((int)(x - width/2), (int)(y + width/2 * Math.sqrt(3)));// Back to Top
        path.lineTo((int)(x - width), (int)(y));
        path.close();

        Paint paint  = new Paint();
        paint.setStyle(Paint.Style.FILL);


        int [] colors = new int[] {isOn ? dirSwatch.toInt() : 0xFF000000, isOn ? dirSwatch.swatch(0.9).toInt() : 0xFF000000, isOn ? dirSwatch.swatch(0.8).toInt() : 0xFF000000 };
        float [] positions = new float[] { 0.5f, 0.8f, 0.9f };

        RadialGradient gradient = new RadialGradient(x, y, width, colors, positions, Shader.TileMode.CLAMP);
        paint.setDither(true);
        paint.setShader(gradient);

        canvas.drawPath(path, paint);

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(STROKE_WIDTH);
        paint.setAntiAlias(true);
        paint.setColor(dirSwatch.swatch(0.85).toInt());
        canvas.drawPath(path, paint);
    }

    private void textValueAnimator(boolean fade){
        if(fade || prevFade && !(fade && prevFade)) {
            ValueAnimator animator;

            if (fade) {
                animator = ValueAnimator.ofFloat(0f, 255f);
            } else {
                animator = ValueAnimator.ofFloat((float) textAlpha, 0f);
            }

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator updatedAnimation) {
                    // You can use the animated value in a property that uses the
                    // same type as the animation. In this case, you can use the
                    // float value in the translationX property.
                    float animatedValue = (float) updatedAnimation.getAnimatedValue();
                    textAlpha = (int) animatedValue;
                    invalidate();
                }
            });

            prevFade = fade;

            animator.setDuration(300);
            animator.start();
        }
    }

    private void syncValueAnimator(Swatch start, Swatch end){
        ValueAnimator animator = ValueAnimator.ofArgb(start.toInt(), end.toInt());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator updatedAnimation) {
                // You can use the animated value in a property that uses the
                // same type as the animation. In this case, you can use the
                // float value in the translationX property.
                dirSwatch.fromInt((Integer) updatedAnimation.getAnimatedValue());
                invalidate();
            }
        });

        animator.setDuration(300);
        animator.start();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);

        width = parentWidth;
        height = parentHeight;

        setMeasuredDimension(width, height);
    }

    public void requestInformation(){
        comms.requestInformation(this);
    }

    public AppClient getClient() {
        return comms;
    }

    public void setClient(AppClient comms) {
        comms.sendCommand("clearf " + getLampNumber());
        this.comms = comms;
    }


    public Swatch getDirSwatch() {
        return dirSwatch;
    }

    public void setDirSwatch(Swatch dirSwatch, boolean update, boolean animate) {

        if(isOnline() && !dirSwatch.equals(this.dirSwatch) && update) {
            comms.sendCommand("dir " + getLampNumber() + " " + dirSwatch.instruction());
        }

        if(animate)
            syncValueAnimator(new Swatch(this.dirSwatch), new Swatch(dirSwatch));

        this.dirSwatch = new Swatch(dirSwatch);
    }

    public ArrayList<Swatch> getOnlineSwatches() {
        return onlineSwatches.getPalette();
    }

    public void setOnlineSwatches(Palette onlineSwatches, boolean update) {
        if(isOnline() && !onlineSwatches.equals(this.onlineSwatches)) {
            for(Swatch in: onlineSwatches.getPalette()){
                if(!this.onlineSwatches.containsSwatch(in)){
                    this.addSwatch(in, update);
                }
            }

            for(int i = 0; i < this.onlineSwatches.size(); i++){

                Swatch out = this.onlineSwatches.getPalette().get(i);

                if(!onlineSwatches.containsSwatch(out)){
                    this.removeSwatch(out, update);
                }
            }
                //comms.sendCommand("resp " + getLampNumber() + " " + onlineSwatches.instruction());
        }
    }

    public void addSwatch(Swatch s, boolean update){

        if(!this.onlineSwatches.containsSwatch(s)) {
            if (isOnline() && update) {
                comms.sendCommand("addp " + getLampNumber() + " " + s.instruction());
            }

            if (sa != null)
                sa.notifyItemInserted(onlineSwatches.size());
            this.onlineSwatches.addSwatch(s);
        }
    }

    public void removeSwatch(Swatch s, boolean update){
        if(isOnline() && update) {
            comms.sendCommand("remp " + getLampNumber() + " " + s.instruction());
        }

        if(sa != null)
            sa.notifyDataSetChanged();

        this.onlineSwatches.removeSwatch(s);
    }

    public void removeSwatch(int i, boolean update){

        if(sa != null)
            sa.notifyItemRemoved(i);

        Swatch rem = this.onlineSwatches.removeSwatch(i);

        if(isOnline() && update) {
            comms.sendCommand("remp " + getLampNumber() + " " + rem.instruction());
        }
    }

    public void reorderSwatches(int from, int to, boolean update) {
        Swatch s = onlineSwatches.removeSwatch(from);

        onlineSwatches.addSwatch(to, s);

        if(sa != null){
            sa.notifyItemMoved(from, to);
        }

        if(isOnline() && update) {
            comms.sendCommand("swap " + getLampNumber() + " " + from + ":" + to);
        }
    }

    public int getLampNumber() {
        return lampNumber;
    }

    public void setLampNumber(int lampNumber) {
        this.lampNumber = lampNumber;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on, boolean update, boolean animate) {
        if(isOnline() && on != isOn && update) {
            if(on) {
                comms.sendCommand("on " + getLampNumber());
            } else {
                comms.sendCommand("off " + getLampNumber());
            }
        }

        if(animate) {
            if (isOn != on)
                textValueAnimator(on);

            if (isOn != on)
                if (on) {
                    syncValueAnimator(Swatch.black(), new Swatch(dirSwatch));
                } else {
                    syncValueAnimator(new Swatch(dirSwatch), Swatch.black());
                }
        }

        isOn = on;
    }

    public void hideNumber(){
        setShowNum(false);
    }

    public void showNumber(){
        setShowNum(true);
    }

    public boolean isShowNum() {
        return showNum;
    }

    public void setShowNum(boolean showNum) {
        if(isOn)
            textValueAnimator(showNum);

        this.showNum = showNum;
    }

    public SwatchAdapter getSwatchAdapter() {
        return sa;
    }

    public void setSwatchAdapter(SwatchAdapter sa) {
        this.sa = sa;
    }

    public int getSwatchMode() {
        return swatchMode;
    }

    public void setSwatchMode(int swatchMode, boolean update) {

        if(isOnline() && this.swatchMode != swatchMode && update){
            comms.sendCommand("swm " + getLampNumber() + " " + swatchMode);
        }
        this.swatchMode = swatchMode;
    }

    public int getTransitionMode() {
        return transitionMode;
    }

    public void setTransitionMode(int transitionMode, boolean update) {

        if(isOnline() && this.transitionMode != transitionMode && update){
            comms.sendCommand("trm " + getLampNumber() + " " + transitionMode);
        }

        this.transitionMode = transitionMode;
    }

    public int getDrawMode() {
        return drawMode;
    }

    public void setDrawMode(int drawMode) {
        this.drawMode = drawMode;
    }

    public void sync(Lamp other) {

        if (other.getLampNumber() != this.getLampNumber()) {
            this.drawMode = other.drawMode;
            this.setSwatchMode(other.swatchMode, true);
            this.setTransitionMode(other.transitionMode, true);
            this.setOn(other.isOn, true, true);
            this.setDelay(other.delay, true);
            this.setDirSwatch(new Swatch(other.dirSwatch), true, true);
            this.setOnlineSwatches(new Palette(other.onlineSwatches), true);
        }
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay, boolean update) {

        if(isOnline() && this.delay != delay && update){
            comms.sendCommand("del " + getLampNumber() + " " + delay);
        }

        this.delay = delay;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return ((Lamp) obj).getLampNumber() == this.getLampNumber();
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {

        if(online){
            requestInformation();
        }


        this.online = online;
    }

    public static String getTransitionString(int mode){
        switch (mode){
            case Lamp.TRANSITION_MODE_DEFAULT:
                return ("Linear");
            case Lamp.TRANSITION_MODE_FLASH:
                return ("Flash");
            case Lamp.TRANSITION_MODE_SMOOTH:
                return("Smooth");
        }

        return "";
    }

    public static String getSwatchString(int mode){
        switch (mode){
            case Lamp.SWATCH_MODE_FLIPFLOP:
                return ("FlipFlop");
            case Lamp.SWATCH_MODE_LOOP:
                return ("Loop");
            case Lamp.SWATCH_MODE_RANDOM:
                return("Random");
        }

        return "";
    }

    public static String getDrawString(int mode){
        switch (mode){
            case Lamp.DRAW_MODE_ANIMATION:
                return ("Animation");
            case Lamp.DRAW_MODE_DIRECT:
                return ("Direct");
            case Lamp.DRAW_MODE_SYNC:
                return("Sync");
        }

        return "";
    }

    public static int getTransitionMode(String title){
        switch (title){
            case "Linear":
                return Lamp.TRANSITION_MODE_DEFAULT;
            case "Flash":
                return Lamp.TRANSITION_MODE_FLASH;
            case "Smooth":
                return Lamp.TRANSITION_MODE_SMOOTH;
        }

        return -1;
    }

    public static int getSwatchMode(String title){
        switch (title){
            case "Random":
                return Lamp.SWATCH_MODE_RANDOM;
            case "Loop":
                return Lamp.SWATCH_MODE_LOOP;
            case "FlipFlop":
                return Lamp.SWATCH_MODE_FLIPFLOP;
        }

        return -1;

    }

    public static int getDrawMode(String title){
        switch (title){
            case "Animation":
                return Lamp.DRAW_MODE_ANIMATION;
            case "Direct":
                return Lamp.DRAW_MODE_DIRECT;
            case "Sync":
                return Lamp.DRAW_MODE_SYNC;
        }

        return -1;
    }


}
