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

    public void addPixel(Pixel pixel){add(pixel);
    }

    public Pixel getPixel(int i) {return get(i); }

    public void removePixel(int i){remove(i);
    }

    public Pixel getLastPixel(){ return get(size()-1); }

    public Pixel[] getPixelArray(){
        return (Pixel[]) toArray();
    }

    public ArrayList<Pixel> getPixelArrayList(){
        return this;
    }


}
