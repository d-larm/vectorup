package com.vmu.vectormeup.spline;

import android.graphics.Paint;
import android.graphics.Path;

import java.util.ArrayList;

/**
 * Created by Daniel on 21/01/2018.
 */

public class SPath extends ArrayList<Path> {

    private Paint paint;

    public SPath(){
        super();
    }

    public SPath(Paint p){
        super();
        this.paint = p;

    }

    public SPath(int count,Paint p){
        super(count);
        this.paint = p;

    }

    public SPath(int count){
        super(count);
    }
    public Paint getPaint(){
        return paint;
    }

    public void setPaint(Paint p){
        this.paint = p;
    }
}
