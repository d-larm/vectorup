package com.vmu.vectormeup.spline;

import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by Daniel on 26/02/2018.
 */

public class CountedPath extends Path implements Comparable<CountedPath> {
    private int count;
    private Paint paint;
    private StringBuilder pathText;

    public void startPathText(){
        pathText = new StringBuilder("<path stroke-width=\""+paint.getStrokeWidth()+ "\" stroke=\""+String.format("#%06X",(0xFFFFFF & paint.getColor()))+"\" fill=\""+ String.format("#%06X", (0xFFFFFF & paint.getColor()))+"\" d=\"");
    }

    public void setCount(int count){ this.count = count; }

    public void setPaint(Paint paint){ this.paint = paint; }

    public Paint getPaint(){ return paint; }

    @Override
    public int compareTo(CountedPath p) {
        int pcount = p.getCount();
        return  count < pcount ? +1 : count > pcount ? -1 : 0;
    }

    public int getCount(){ return count; }

    public void addToPathText(String text) { pathText.append(text).append(" "); }

    public void completePathText(){ pathText.append("Z\"/>"+System.lineSeparator());}

    public String getPathText(){ return pathText.toString(); }
}
