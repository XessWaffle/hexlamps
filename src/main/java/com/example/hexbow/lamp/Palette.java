package com.example.hexbow.lamp;

import com.example.hexbow.callback.Instructable;

import java.util.ArrayList;
import java.util.Objects;

public class Palette implements Instructable {
    private ArrayList<Swatch> palette;

    public Palette(){
        palette = new ArrayList<>();
    }

    public Palette(Palette swatches){
        palette = new ArrayList<>(swatches.palette);
    }

    public void addSwatch(Swatch swatch){
        palette.add(swatch);
    }

    public void addSwatch(int loc, Swatch s){
        palette.add(loc, s);
    }

    public Swatch removeSwatch(int index){
        return palette.remove(index);
    }

    public void removeSwatch(Swatch s){
        palette.remove(s);
    }

    public boolean containsSwatch(Swatch s){ return palette.contains(s); }

    public ArrayList<Swatch> getPalette(){
        return palette;
    }

    public int size(){
        return palette.size();
    }

    public String instruction(){
        String instruction = "";
        for(int i = 0; i < palette.size(); i++){
            instruction += palette.get(i).toInt() + ",";
        }

        return instruction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Palette palette1 = (Palette) o;

        return palette1.palette.equals(palette);

    }

    @Override
    public int hashCode() {
        return Objects.hash(palette);
    }
}
