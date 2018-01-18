package com.vmu.vectormeup.threading;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.vmu.vectormeup.trace.Contour;
import com.vmu.vectormeup.trace.Pixel;
import com.vmu.vectormeup.trace.Tracer;

import java.util.Arrays;

import javax.xml.transform.Result;

/**
 * Created by Daniel on 18/01/2018.
 */

public class TraceTask implements Runnable {

    int[] image;
    int activeColor;
    int w;
    int h;
    Contour c;


    public TraceTask( int[] image,int c,int w,int h) {
        super();
        this.image = image;
        this.activeColor = c;
        this.w = w;
        this.h = h;

    }

    @Override
    public void run() {
        traceEdges();
    }

    public void traceEdges(){
        Tracer tracer = new Tracer(image,w,h,activeColor);
        c =  tracer.trace();

        System.out.println("Finished trace");

        for(int i=0;i<image.length;i++){
            image[i] = -123;
        }

        System.out.println("Drawing onto view");
        for(int i=0;i<c.size();i++){
            Pixel p = c.getPixel(i);
            image[p.getIndex(w)] = p.getColor();
        }

        System.out.println("Process complete");
    }
}
