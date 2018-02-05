package com.vmu.vectormeup.spline;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.vmu.vectormeup.trace.Contour;
import com.vmu.vectormeup.trace.Pixel;
import com.vmu.vectormeup.trace.PixelHashSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by Daniel on 20/01/2018.
 */

public class SplineManager {
   Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
   Canvas canvas;
   ArrayList<SPath> paths;
   SPath edgePath;
   Pixel startPixel;
   Path currentPath = new Path();
   boolean canSetStart = true;



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
        paint.setStrokeWidth(2);

        paint.setStyle(Paint.Style.STROKE);
//        System.out.println("Contour of size " + pixels.size() + "for thread "+Thread.currentThread().getId());
        if(pixels.size() > 1){
            for(int i = 0; i < pixels.size(); i++){
                if(i >= 0){
                    Pixel pixel = pixels.get(i);

                    if(i == 0){
                        Pixel next = pixels.get(i + 1);
                        pixel.setDifferentials(((next.getX() - pixel.getX()) / 3), ((next.getY() - pixel.getY()) / 3));
                    }
                    else if(i == pixels.size() - 1){
                        Pixel prev = pixels.get(i - 1);
                        pixel.setDifferentials(((pixel.getX() - prev.getX()) / 3),((pixel.getY() - prev.getY()) / 3));
                    }
                    else{
                        Pixel next = pixels.get(i + 1);
                        Pixel prev = pixels.get(i - 1);
                        pixel.setDifferentials(((next.getX() - prev.getX()) / 3),((next.getY() - prev.getY()) / 3));
                    }
                }
            }
        }

        boolean first = true;
        for(int i = 0; i < pixels.size(); i++){
            Pixel pixel = pixels.get(i);
            if(pixel.getIndex() == startPixel.getIndex() && !first){
                currentPath.close();
                edgePath.add(currentPath);
                currentPath = new Path();

//                canvas.drawPath(currentPath, paint);
                if(i < pixels.size() - 1) {
//                    System.out.println("Reached end,change start pixel");
//                    currentPath.toggleInverseFillType();
//                    for(int k = 0; k < edgePath.size(); k++)
//                        System.out.println("Path "+ k + " empty? " +edgePath.get(k).isEmpty());
                    paint.setColor(Color.WHITE);
                    canvas.drawPoint(startPixel.getX(),startPixel.getY(), paint);
                    startPixel = pixels.get(i + 1);

                    canvas.drawPoint(startPixel.getX(),startPixel.getY(), paint);
                    currentPath.moveTo(startPixel.getX(),startPixel.getY());
                    i++;
                    paint.setColor(pixels.getColor());
                }
            }
            if(first){
                first = false;
                currentPath.moveTo(pixel.getX(), pixel.getY());
            }
            else{
                Pixel prev = pixels.get(i - 1);
                currentPath.cubicTo(prev.getX() + prev.getDx(), prev.getY() + prev.getDy(), pixel.getX() - pixel.getDx(), pixel.getY() - pixel.getDy(), pixel.getX(), pixel.getY());
            }
        }
        paths.add(edgePath);
    }


    public void draw(PixelHashSet pixels) {
        paint.setColor(pixels.getColor());
        paint.setStrokeWidth(2);

        paint.setStyle(Paint.Style.STROKE);
        Pixel currentPixel = startPixel;
        int pixelCount = pixels.size();
        Iterator<Pixel> p;
        Pixel prev = null;
        Pixel next = null;
        int i=0;
//        System.out.println("Contour of size " + pixels.size() + "for thread "+Thread.currentThread().getId());
        if(pixelCount > 1){


            for(p = pixels.iterator();p.hasNext();){
                Pixel pixel = p.next();
                if(i == 0){
                    next = p.next();
                    pixel.setDifferentials(((next.getX() - pixel.getX()) / 3), ((next.getY() - pixel.getY()) / 3));
                    prev = pixel;
                    i++;
                }
                if(!p.hasNext()){
                    pixel = next;
                    pixel.setDifferentials(((pixel.getX() - prev.getX()) / 3),((pixel.getY() - prev.getY()) / 3));
                    break;
                }
                if(prev != null && p.hasNext()){
                    prev = pixel;
                    pixel = next;
                    next = p.next();
                    pixel.setDifferentials(((next.getX() - prev.getX()) / 3),((next.getY() - prev.getY()) / 3));
                    i++;
                }
            }
        }
        i=0;
        next = null;
        prev = null;
        for(p = pixels.iterator();p.hasNext();) {
            Pixel pixel = p.next();
            if (prev != null && pixel.isStart()) {
                currentPath.close();
                edgePath.add(currentPath);
                currentPath = new Path();
                if (p.hasNext()) {
                    prev = pixel;
                    pixel = next;
                    next = p.next();
                    currentPath.moveTo(pixel.getX(), pixel.getY());
                    i++;
                }
            }
            if (prev == null)
                currentPath.moveTo(currentPixel.getX(), currentPixel.getY());
            else
                currentPath.cubicTo(prev.getX() + prev.getDx(), prev.getY() + prev.getDy(), pixel.getX() - pixel.getDx(), pixel.getY() - pixel.getDy(), pixel.getX(), pixel.getY());

            currentPixel = currentPixel.nextPixel;
            i++;
        }


        int i=0;
        currentPixel = startPixel;

        while(currentPixel != null){
            if(i!=0 && currentPixel.isStart()){
                currentPath.close();
                edgePath.add(currentPath);
                currentPath = new Path();
                if(currentPixel.nextPixel != null) {
                    paint.setColor(Color.WHITE);
                    canvas.drawPoint(startPixel.getX(),startPixel.getY(), paint);
                    currentPixel = currentPixel.nextPixel;
                    canvas.drawPoint(startPixel.getX(),startPixel.getY(), paint);
                    currentPath.moveTo(startPixel.getX(),startPixel.getY());
                    i++;
                    paint.setColor(pixels.getColor());
                }
            }

            if(i==0)
                currentPath.moveTo(currentPixel.getX(), currentPixel.getY());
            else
                currentPath.cubicTo(prev.getX() + prev.getDx(), prev.getY() + prev.getDy(), pixel.getX() - pixel.getDx(), pixel.getY() - pixel.getDy(), pixel.getX(), pixel.getY());

            currentPixel = currentPixel.nextPixel;
            i++;
        }
        paths.add(edgePath);
    }

    public boolean canSetStart(){
       return canSetStart;
    }


}
