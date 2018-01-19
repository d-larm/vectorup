package com.vmu.vectormeup.threading;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.vmu.vectormeup.NeuQuant;
import com.vmu.vectormeup.trace.Contour;
import com.vmu.vectormeup.trace.Pixel;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Daniel on 18/01/2018.
 */

public class TracePool {
    int[] image;
    int w;
    int h;
    int[] map;
    int count;
    LinkedBlockingQueue<Runnable> traceQueue;
    ThreadPoolExecutor threadpool;

    public TracePool(int[] image,int w, int h, int[] map){
        this.image = image;
        this.w = w;
        this.h = h;
        this.map = map;
        count = map.length;
        traceQueue = new LinkedBlockingQueue<>(count);
        threadpool =
                new ThreadPoolExecutor(
                        //initial processor pool size
                        Runtime.getRuntime().availableProcessors(),
                        //Max processor pool size
                        Runtime.getRuntime().availableProcessors(),
                        //Time to Keep Alive
                        3,
                        //TimeUnit for Keep Alive
                        TimeUnit.SECONDS,
                        //Queue of Runnables
                        traceQueue
                );
    }



    public void traceImage(){
        ArrayList<Contour> edges = new ArrayList<Contour>(map.length);
        for(int i=0;i<map.length;i++){
            System.out.println(map[i]);
            TraceTask t = new TraceTask(image,map[i],w,h,edges);
            threadpool.execute(t);
        }




        System.out.println("Process complete");
        threadpool.shutdown();

        try {
            threadpool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            for(int i=0;i<image.length;i++){
                image[i] = -123;
            }

            for(int i=0;i<edges.size();i++){
                for(int j=0;j<edges.get(i).size();j++){
                    Pixel p = edges.get(i).getPixel(j);
                    image[p.getIndex(w)] = p.getColor();
                }

            }
        } catch (InterruptedException e) {
        }
    }

}
