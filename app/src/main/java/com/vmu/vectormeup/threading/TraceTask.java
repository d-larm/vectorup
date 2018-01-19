package com.vmu.vectormeup.threading;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.vmu.vectormeup.trace.Contour;
import com.vmu.vectormeup.trace.Pixel;
import com.vmu.vectormeup.trace.Tracer;

import java.util.ArrayList;
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
    ArrayList<Contour> imageContours;
    Contour c;


    public TraceTask( int[] image,int c,int w,int h, ArrayList<Contour> e) {
        super();
        this.image = image;
        this.activeColor = c;
        this.w = w;
        this.h = h;
        this.imageContours = e;


    }

    @Override
    public void run() {
        traceEdges();
    }

    public void traceEdges(){
        System.out.println("Thread "+Thread.currentThread().getId()+ " working on color "+activeColor);
        Tracer tracer = new Tracer(image,w,h,activeColor);
        c =  tracer.trace();
        imageContours.add(c);



    }
}
