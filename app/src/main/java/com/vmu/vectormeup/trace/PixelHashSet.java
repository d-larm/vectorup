package com.vmu.vectormeup.trace;

import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * Created by u1501624 on 05/02/18.
 */

public class PixelHashSet extends LinkedHashSet<Pixel> {
    private int color;
    public PixelHashSet(int size){
        super(size);
    }

    public int getColor(){
        return color;
    }

    public void setColor(int c){
        color = c;
    }
}
