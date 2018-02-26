package com.vmu.vectormeup.threading;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.vmu.vectormeup.spline.SplineManager;
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
    SplineManager splineManager;
    ArrayList<Contour> imageContours;
    Contour c;


    public TraceTask( int[] image,int c,int w,int h, ArrayList<Contour> e,SplineManager sm,int minPathSize) {
        super();
        this.image = image;
        this.activeColor = c;
        this.w = w;
        this.h = h;
        this.imageContours = e;
        this.splineManager = sm;
        sm.setMinPathSize(minPathSize);


    }

    @Override
    public void run() {
        traceEdges();
    }

    public void traceEdges(){
        System.out.println("Thread "+Thread.currentThread().getId()+ " working on color "+activeColor);
        Tracer tracer = new Tracer(image,w,h,activeColor);
        tracer.assignSplineManager(splineManager);
        c =  tracer.trace();
        imageContours.add(c);



    }
}
