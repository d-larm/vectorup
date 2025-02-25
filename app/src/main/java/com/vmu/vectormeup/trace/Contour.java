package com.vmu.vectormeup.trace;
import java.util.ArrayList;

/**
 * Created by daniel on 03/12/17.
 */

public class Contour extends ArrayList<Pixel>{

    private int color;

    public Contour(){
        super();
        color = 0;
    }

    public Contour(int color){
        super();
        this.color = color;
    }

    public Contour(int color,int width,int height){
        super((width*height)/50);
        this.color = color;
    }

    public Contour(int color,int size){
        super(size);
        this.color = color;
    }

    public int getColor(){
        return color;
    }

    public void addPixel(Pixel pixel){add(pixel);
    }

    public Pixel getPixel(int i) {return get(i); }

    public void removePixel(int i){remove(i);
    }

    public Pixel getLastPixel(){ if(this.size() > 0) return get(size()-1);else return null; }

    public Pixel[] getPixelArray(){
        return (Pixel[]) toArray();
    }

    public ArrayList<Pixel> getPixelArrayList(){
        return this;
    }


}
