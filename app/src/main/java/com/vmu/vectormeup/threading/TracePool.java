package com.vmu.vectormeup.threading;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Space;
import android.widget.TextView;

import com.vmu.vectormeup.MainActivity;
import com.vmu.vectormeup.spline.SPath;
import com.vmu.vectormeup.spline.SplineManager;
import com.vmu.vectormeup.trace.Contour;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    int minPathSize;
    boolean isBusy = false;
    Button start;
    LinkedBlockingQueue<Runnable> traceQueue;
    ThreadPoolExecutor threadpool;
    Canvas canvas;
    ArrayList<SPath> paths;
    TextView progressText;
    MainActivity main;
    int mode = 0;
    Context context;
    StringBuilder svgData;

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
            main.runOnUiThread(new Runnable() {
                public void run() {
                    progressText.setText("Creating Paths");
                }
            });
            svgData = new StringBuilder("<svg  xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink= \"http://www.w3.org/1999/xlink\" width=\""+w+"\" height=\""+h+"\">");

            Paint p = new Paint();
            p.setColor(Color.GREEN);
            p.setStrokeWidth(2);
            p.setStyle(Paint.Style.STROKE);
            canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);

            if(mode == 0){ //Ordered mode. Paths drawn in order of size
                SPath result = new SPath(100000);
                for(int i=0;i<paths.size();i++)
                    result.addAll(paths.get(i));

                Collections.sort(result);

                for(int i=0;i<result.size();i++){
                    canvas.drawPath(result.get(i),result.get(i).getPaint());
                    svgData.append(result.get(i).getPathText());
                }
            }else{ //Unordered mode. Paths drawn by colour
                for(int i=0;i<paths.size();i++)
                    if(paths.get(i).getPaint().getColor() == Color.WHITE && paths.get(i).size() > 0)
                        canvas.drawPath(paths.get(i).get(0),paths.get(i).getPaint());

                for(int i=0;i<paths.size();i++){
                    if(paths.get(i).getPaint().getColor() != Color.WHITE)
                        for(int j=0;j<paths.get(i).size();j++){
                            canvas.drawPath(paths.get(i).get(j),paths.get(i).getPaint());
                        }
                }
                for(int i=0;i<paths.size();i++){
                    if(paths.get(i).getPaint().getColor() == Color.WHITE)
                        for(int j=1;j<paths.get(i).size();j++){
                            canvas.drawPath(paths.get(i).get(j),paths.get(i).getPaint());
                        }
                }
            }
            svgData.append("</svg>");
            writeToFile("svgdata.svg",svgData.toString());
        }
    }

    public TracePool(int[] image,int w, int h, int[] map,int minPathSize){
        this.image = image;
        this.w = w;
        this.h = h;
        this.map = map;
        count = map.length;
        paths = new ArrayList<SPath>(count);
        traceQueue = new LinkedBlockingQueue<>(count);
        this.minPathSize = minPathSize;


    }

    public TracePool(){
        this.w = 0;
        this.h = 0;
        this.count = 0;
    }

    public void setParams(int[] image,int w, int h, int[] map,int minp,int mode,Context context){
        this.image = image;
        this.w = w;
        this.h = h;
        this.map = map;
        count = map.length;
        paths = new ArrayList<SPath>(count);
        traceQueue = new LinkedBlockingQueue<>(count);
        minPathSize = minp;
        this.mode = mode;
        this.context = context;

    }

    public void setProgressTetx(TextView t){progressText = t;}

    public void setMainActivity(MainActivity main) { this.main = main; }

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
                TraceTask t = new TraceTask(image,map[i],w,h,edges,new SplineManager(1000,canvas,paths),minPathSize);
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

    public void writeToFile(String filename,String contents){
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "VectorUp" + File.separator);
        final File svgFile = new File(root, filename);
        FileOutputStream outputStream;
        try {
            System.out.println("File: "+svgFile.getAbsolutePath());
            System.out.println(contents);
            outputStream = new FileOutputStream(svgFile);
            outputStream.write(contents.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

}
