package com.vmu.vectormeup.threading;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.vmu.vectormeup.NeuQuant;

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
    NeuQuant nq;
    LinkedBlockingQueue<Runnable> traceQueue = new LinkedBlockingQueue<>(nq.getColorCount());
    ThreadPoolExecutor threadpool =
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

    public TracePool(int[] image,int w, int h, NeuQuant q){
        this.image = image;
        this.w = w;
        this.h = h;
        this.nq = q;
    }



    public void traceImage(){
        int[] map = nq.getColorMap();
        for(int i=0;i<map.length;i++){
            TraceTask t = new TraceTask(image,map[i],w,h);
            threadpool.execute(t);
        }
        threadpool.shutdown();
    }

}
