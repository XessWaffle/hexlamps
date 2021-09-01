package com.example.hexbow.lamp;

import androidx.annotation.Nullable;

import com.example.hexbow.callback.Instructable;

public class Swatch implements Instructable {
    private int red, green, blue, alpha;

    public Swatch(){
        red = 0;
        green = 0;
        blue = 0;
        alpha = 0;
    }

    public Swatch(int red, int  green, int blue, int alpha){
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public Swatch(Swatch other){
        this.red = other.red;
        this.green = other.green;
        this.blue = other.blue;
        this.alpha = other.alpha;
    }

    public int getRed() {
        return red;
    }

    public Swatch setRed(int red) {
        this.red = red;
        return this;
    }

    public int getGreen() {
        return green;
    }

    public Swatch setGreen(int green) {
        this.green = green;
        return this;
    }

    public int getBlue() {
        return blue;
    }

    public Swatch setBlue(int blue) {
        this.blue = blue;
        return this;
    }

    public int getAlpha() {
        return alpha;
    }

    public Swatch setAlpha(int alpha) {
        this.alpha = alpha;
        return this;
    }

    public int toInt() {

        //System.out.println(String.format("Alpha 0x%08X, Red 0x%08X, Green 0x%08X, Blue 0x%08X, Sum 0x%08x\n", alpha<<24, red<<16, green<<8, blue, (alpha << 24) | (red << 16) | (green << 8) | blue));

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    public void fromInt(int color){
        alpha = (color & 0xFF000000) >> 24;
        red = (color & 0x00FF0000) >> 16;
        green = (color & 0x0000FF00) >> 8;
        blue = color & 0x000000FF;
    }

    public Swatch invert(){
        return new Swatch(255 - red, 255 - green, 255 - blue, alpha);
    }

    public Swatch grayScale(){

        int gray = (red + blue + green)/3;

        return new Swatch(gray, gray, gray, alpha);
    }

    public Swatch swatch(double factor){
        int nred = factor * red > 255 ? 255 : (int) (factor * red);
        int ngreen = factor * green > 255 ? 255 : (int) (factor * green);
        int nblue = factor * blue > 255 ? 255 : (int) (factor * blue);

        return new Swatch(nred, ngreen, nblue, alpha);

    }

    public Swatch swatch(int dColor){
        int nred = dColor + red;
        int ngreen = dColor + green;
        int nblue = dColor + blue;

        if(nred < 0) nred = 0;
        if(nred > 255) nred = 255;

        if(ngreen < 0) ngreen = 0;
        if(ngreen > 255) ngreen = 255;

        if(nblue < 0) nblue = 0;
        if(nblue > 255) nblue = 255;

        return new Swatch(nred, ngreen, nblue, alpha);
    }

    public String instruction(){
        return this.toInt() + "";
    }

    @Override
    public String toString(){
        return red + "," + green + "," + blue;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        Swatch other = (Swatch) obj;

        return other.red == red && other.green == green && other.blue == blue && other.alpha == alpha;
    }

    public static Swatch random(){
        return new Swatch((int)(Math.random() * 255),(int)(Math.random() * 255),(int)(Math.random() * 255),255);
    }

    public static Swatch black(){
        return new Swatch(0,0,0,255);
    }

    public static Swatch white(){
        return new Swatch(255,255,255,255);
    }
}
