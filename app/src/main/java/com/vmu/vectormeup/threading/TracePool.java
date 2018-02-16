package com.vmu.vectormeup.threading;

import android.app.ProgressDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.widget.Button;

import com.vmu.vectormeup.NeuQuant;
import com.vmu.vectormeup.spline.SPath;
import com.vmu.vectormeup.spline.SplineManager;
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
    boolean isBusy = false;
    Button start;
    LinkedBlockingQueue<Runnable> traceQueue;
    ThreadPoolExecutor threadpool;
    Canvas canvas;
    ArrayList<SPath> paths;

    class UpdateThread implements Runnable{
        int[] image;
        Canvas canvas;
        ArrayList<SPath> paths;
        ArrayList<Contour> edges;


        public UpdateThread(int[] img,ArrayList<Contour> e){
            image = img;
            edges = e;
        }

        public UpdateThread(Canvas c, ArrayList<SPath> p){
            canvas = c;
            paths = p;
        }

        public UpdateThread(Canvas c, ArrayList<SPath> p,ArrayList<Contour> e){
            canvas = c;
            paths = p;
            edges = e;
        }

        public void run(){
//            for(int i=0;i<image.length;i++){
//                image[i] = -1;
//            }
//            for(int i=0;i<edges.size();i++){
//                for(int j=0;j<edges.get(i).size();j++){
//                    Pixel p = edges.get(i).getPixel(j);
//                    image[p.getIndex(w)] = p.getColor();
//                }
//            }
            Paint p = new Paint();
            p.setColor(Color.GREEN);
            p.setStrokeWidth(2);
            p.setStyle(Paint.Style.STROKE);
            canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
//            for(int i=0;i<edges.size();i++){
//                for(int j=0;j<edges.get(i).size();j+=1){
//                    canvas.drawPoint(edges.get(i).get(j).getX()+ 35,edges.get(i).get(j).getY(),p);
//                }
//            }

            for(int i=0;i<paths.size();i++){
                for(int j=0;j<paths.get(i).size();j++){
                    canvas.drawPath(paths.get(i).get(j),paths.get(i).getPaint());
                }
            }


        }
    }

    public TracePool(int[] image,int w, int h, int[] map){
        this.image = image;
        this.w = w;
        this.h = h;
        this.map = map;
        count = map.length;
        paths = new ArrayList<SPath>(count);
        traceQueue = new LinkedBlockingQueue<>(count);

    }

    public TracePool(){
        this.w = 0;
        this.h = 0;
        this.count = 0;
    }

    public void setParams(int[] image,int w, int h, int[] map){
        this.image = image;
        this.w = w;
        this.h = h;
        this.map = map;
        count = map.length;
        paths = new ArrayList<SPath>(count);
        traceQueue = new LinkedBlockingQueue<>(count);
    }

    public boolean isBusy(){
        return isBusy;
    }

    public void setStartButton(Button b){
        this.start = b;
    }

    public void setCanvas(Canvas c){
        canvas = c;
    }



    public void traceImage(){
        isBusy = true;
        threadpool = new ThreadPoolExecutor(
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
        ArrayList<Contour> edges = new ArrayList<>(map.length);


        for(int i=0;i<map.length;i++){
            if(map[i] != Color.WHITE){
                System.out.println(map[i]);
                TraceTask t = new TraceTask(image,map[i],w,h,edges,new SplineManager(1000,canvas,paths));
                threadpool.execute(t);
            }

        }

        threadpool.shutdown();

        try {
            threadpool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            UpdateThread u = new UpdateThread(canvas,paths,edges);
            u.run();
            isBusy = false;
            System.out.println("Process complete");
            start.setEnabled(true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
