package com.vmu.vectormeup.spline;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.widget.SeekBar;

import com.vmu.vectormeup.R;
import com.vmu.vectormeup.trace.Contour;
import com.vmu.vectormeup.trace.Pixel;

import java.util.ArrayList;
import java.util.logging.Handler;

/**
 * Created by Daniel on 20/01/2018.
 */

public class SplineManager {
   Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
   Canvas canvas;
   ArrayList<SPath> paths;
   SPath edgePath;
   Pixel startPixel;
   CountedPath currentPath = new CountedPath();
   boolean canSetStart = true;
   int minPathSize = 0;
//   final SeekBar seekBarMaxPoints2 = (SeekBar) findViewById(R.id.maxPoints2);


   public SplineManager(int size,Canvas canvas){
       edgePath = new SPath(size);
       edgePath.setPaint(paint);
       this.canvas = canvas;
   }

    public SplineManager(int size,Canvas canvas,ArrayList<SPath> paths){
        edgePath = new SPath(size);
        edgePath.setPaint(paint);
        this.canvas = canvas;
        this.paths = paths;
    }


   public SplineManager(){
            paths = new ArrayList<>(1000);
        }

    public void setMinPathSize(int val){
       minPathSize = val;
    }

    public void setCanvas(Canvas c){
        this.canvas = canvas;
        paths = new ArrayList<>(100);
    }

    public void setStart(Pixel p){
        startPixel = p;
        canSetStart = false;
    }

    public void draw(Contour pixels) {
        paint.setColor(pixels.getColor());
        paint.setStrokeWidth(3 );
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
//        System.out.println("Contour of size " + pixels.size() + "for thread "+Thread.currentThread().getId());
        if (pixels.size() > 1) {
            for (int i = 0; i < pixels.size(); i++) {
                if (i >= 0) {
                    Pixel pixel = pixels.get(i);
                    if (i == 0) {
                        Pixel next = pixels.get(i + 1);
                        pixel.setDifferentials(((next.getX() - pixel.getX()) / 3), ((next.getY() - pixel.getY()) / 3));
                    } else if (i == pixels.size() - 1) {
                        Pixel prev = pixels.get(i - 1);
                        pixel.setDifferentials(((pixel.getX() - prev.getX()) / 3), ((pixel.getY() - prev.getY()) / 3));
                    } else {
                        Pixel next = pixels.get(i + 1);
                        Pixel prev = pixels.get(i - 1);
                        pixel.setDifferentials(((next.getX() - prev.getX()) / 3), ((next.getY() - prev.getY()) / 3));
                    }
                }
            }
        }
        boolean start = true;
        int pointCount = 0;
        currentPath.setPaint(paint);
        currentPath.startPathText();
        for (int i = 0; i < pixels.size(); i++) {
            Pixel pixel = pixels.get(i);
            if (!pixel.isStart() && i%2 == 0){
                pixels.remove(i);
                pixel = pixels.get(i);
            }

                if (pixel.isStart() && start == false) { //Checks if the pixel is a start pixel and not the first pixel in the path
                currentPath.close();
                if (pointCount >= minPathSize){ //Adds the path is it is larger than the minimum path size
                    currentPath.setCount(pointCount);
                    currentPath.completePathText();
                    edgePath.add(currentPath);
                }
                //Resets path to begin new path at next pixel
                pointCount = 0;
                currentPath = new CountedPath();
                currentPath.setPaint(paint);
                currentPath.startPathText();
                i++;

                if (i < pixels.size() - 1) { //Selects a new start pixel if not the final pixel
                    start = true;
                    continue;
                }
            }
            if (start == true) { //Checks if pixel is a start pixel and begins the path
                start = false;
                currentPath.moveTo(pixel.getX(), pixel.getY());
                currentPath.addToPathText("M "+pixel.getX() + " " + pixel.getY());
                pointCount++;
            } else if (i < pixels.size() - 1) { //Draws a cubic spline using previous and next pixels as anchor points
                Pixel prev = pixels.get(i - 1);
                Pixel next = pixels.get(i + 1);
                currentPath.cubicTo(prev.getX(), prev.getY(), pixel.getX(), pixel.getY(), next.getX(), next.getY());
                currentPath.addToPathText("C "+prev.getX() + " " + prev.getY()+ " " + pixel.getX()+ " " + pixel.getY()+ " " + next.getX()+ " " + next.getY());
                pointCount++;
                i++;
            }
        }
        System.out.println(edgePath.size());
        paths.add(edgePath);
    }


    public boolean canSetStart(){
       return canSetStart;
    }


}
