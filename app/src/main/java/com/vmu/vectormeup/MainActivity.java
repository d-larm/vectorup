package com.vmu.vectormeup;

import android.media.Image;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.graphics.*;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.View.OnClickListener;
import com.ipaulpro.afilechooser.utils.*;
import android.content.Intent;
import android.net.Uri;
import java.io.File;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.ActivityNotFoundException;
import android.provider.MediaStore;
import java.util.*;

import java.io.*;
import java.net.URI;
import java.nio.IntBuffer;

import android.app.Activity;
import android.app.Activity;

import org.w3c.dom.Text;

import com.vmu.vectormeup.threading.TracePool;
import com.vmu.vectormeup.trace.*;
import android.graphics.Matrix;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CHOOSER = 1234;
    private int sampleFactor = 10;
    private int colors = 256;
    private ImageView canvas;
    private Uri uri;
    private Bitmap image;
    private int[] pixels;
    private boolean busyQ = false;
    private boolean busy = false;
    private boolean busyT = false;
    private int w = 0;
    private int h = 0;
    private NeuQuant nq;
    private Contour[] edges;

    class QuantiserThread implements Runnable {
        Bitmap image;
        View v;
        QuantiserThread(Bitmap img,View v) {
            image = img;
            this.v = v;
        }
        public void run() {
            if(!busyQ)
                try {
                    busyQ = true;
                    set(image);
                    busyQ = false;
                } catch (IOException e) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(v.getContext(),
                                    "Error: Could not quantise color",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                }
        }
    }

    class TracerThread implements Runnable {
        int[] image;
        View v;
        TracerThread(int[] img,View v) {
            image = img;
            this.v = v;
        }
        public void run() {
            if(!busyT)
                try {
                    busyT = true;
                    traceEdges();
                    busyT = false;
                } catch (Exception e) {
                    e.printStackTrace();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(v.getContext(),
                                    "Error: Could not trace edges",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SeekBar seekBarSampleFactor = (SeekBar) findViewById(R.id.sampleFactor);
//        final SeekBar seekBarMaxPoints = (SeekBar) findViewById(R.id.maxPoints);
        final SeekBar seekBarColours = (SeekBar) findViewById(R.id.colors);

        //Sets colours of the seekbar
        seekBarSampleFactor.getProgressDrawable().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
        seekBarSampleFactor.getThumb().setColorFilter(getResources().getCo‌​lor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        seekBarColours.getProgressDrawable().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
        seekBarColours.getThumb().setColorFilter(getResources().getCo‌​lor(R.color.white), PorterDuff.Mode.SRC_ATOP);

        //Sets the canvas as the image view of the app
        canvas = (ImageView) findViewById(R.id.myImageView);
        try {
            // Get the file path from the URI

            //Checks for read permissions before proceding
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) { //No read permissions

                Toast.makeText(MainActivity.this,
                        "Permissions not given to read files", Toast.LENGTH_LONG).show();

                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Explain to the user why we need to read the contacts

                }
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                // app-defined int constant that should be quite unique

                return;
            } else {
                //System.out.println("Permissions granted");
            }
        }catch(Exception e){
            e.printStackTrace();
        }


        //Gets the events for the sample factor seekbar
        seekBarSampleFactor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView label = (TextView) findViewById(R.id.sampleValue);
                label.setText(String.valueOf(progress + 1));
                sampleFactor = progress + 1;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });
//
//        seekBarMaxPoints.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
//                TextView label = (TextView) findViewById(R.id.pointsValue);
//                label.setText(String.valueOf(progress+1));
//            }
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                // TODO Auto-generated method stub
//            }
//        });

        //Gets the events for the color seekbar
        seekBarColours.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView label = (TextView) findViewById(R.id.colorValue);
                int value = (int) Math.pow(2, (progress + 1));
                label.setText(String.valueOf(value));
                colors = value;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });

        //Sets the ImageView to be clickable and allows selection of any bitmap image
        ImageView myImage = (ImageView) findViewById(R.id.myImageView);
        myImage.setClickable(true);
        myImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.i(SystemSettings.APP_TAG + " : " + HomeActivity.class.getName(), "Entered onClick method");
                Toast.makeText(v.getContext(),
                        "The favorite list would appear on clicking this icon",
                        Toast.LENGTH_LONG).show();
                Intent getContentIntent = FileUtils.createGetContentIntent();
                Intent intent = Intent.createChooser(getContentIntent, "Select an image");
                try {
                    startActivityForResult(intent, REQUEST_CHOOSER);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(v.getContext(),
                            "Could not open file browser",
                            Toast.LENGTH_LONG).show();
                    // The reason for the existence of aFileChooser
                }
            }
        });

        Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!busy){
                    busy = true;
                    QuantiserThread q = new QuantiserThread(image,v);
                    Thread thread = new Thread(q);
                    try {
                        thread.start();
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                Bitmap newImg = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
//                newImg.setPixels(pixels, 0, w, 0, 0, w, h);
//                TracerThread t = new TracerThread(pixels,v);
//                Thread thread2 = new Thread(t);
//                try {
//                    thread2.start();
//                    thread2.join();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                    trace();
                    Bitmap newImg = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
                    newImg.setPixels(pixels, 0, w, 0, 0, w, h);
                    refreshCanvas(newImg);
//                try {
//                    set(image);
//                } catch (IOException e) {
//                    Toast.makeText(v.getContext(),
//                            "Error: Could not quantise color",
//                            Toast.LENGTH_LONG).show();
//                }
                    Toast.makeText(v.getContext(),
                            "Beginning Trace",
                            Toast.LENGTH_LONG).show();

                    busy = false;
                }

            }

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("Request code", Integer.toString(requestCode));
        switch (requestCode) {
            case REQUEST_CHOOSER:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        this.uri = uri;
                        Log.i("Data", "Uri = " + uri.toString());
                        try {
                            // Get the file path from the URI
                            //Checks for read permissions before proceding
                            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) { //No read permissions

                                Toast.makeText(MainActivity.this,
                                        "Permissions not given to read files", Toast.LENGTH_LONG).show();

                                // Should we show an explanation?
                                if (shouldShowRequestPermissionRationale(
                                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                    // Explain to the user why we need to read the contacts

                                }
                                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        1);

                                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                                // app-defined int constant that should be quite unique

                                return;
                            } else {

                                final String path = FileUtils.getPath(this, uri);
                                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                                refreshCanvas(image);
                                Toast.makeText(MainActivity.this,
                                        "File Selected: " + path, Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Log.e("FileSelectorActivity", "File select error", e);
                        }
                    }
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void refreshCanvas(Bitmap newImage) {
        canvas.setImageBitmap(newImage);
        TextView canvasText = (TextView) findViewById(R.id.myImageViewText);
        canvasText.setText("");
    }


    public void set(Bitmap img) throws IOException {
        nq = new NeuQuant(img, canvas, colors,sampleFactor);
        nq.init();
        this.w = img.getWidth();
        this.h = img.getHeight();
        pixels = new int[w * h];
        try {
            image.getPixels(pixels, 0, image.getWidth(), 0, 0, w, h);
        } catch (ArrayIndexOutOfBoundsException | IllegalStateException | IllegalArgumentException e) {
            Toast.makeText(MainActivity.this,
                    "Could not process image into array", Toast.LENGTH_LONG).show();
            Log.e("Image Array", "Could not process bitmap array");
        }

//        if ((pg.getStatus() & java.awt.image.ImageObserver.ABORT) != 0) {
//            throw new IOException ("Image pixel grab aborted or errored");
//        }

        for (int i = 0; i < w*h; i++) {
            pixels[i] = nq.convert(pixels[i]);
        }

//        this.image = this.createImage(new MemoryImageSource(w, h, pixels, 0, w));

//        Bitmap newImg = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
//        newImg.setPixels(pixels, 0, w, 0, 0, w, h);
//        // vector is your int[] of ARGB
////        newImg.copyPixelsFromBuffer(IntBuffer.wrap(pixels));
//
//        refreshCanvas(newImg);
    }

    public void trace(){
        TracePool tp = new TracePool(pixels,w,h,nq);
        tp.traceImage();
    }

    public void traceEdges(){

        System.out.println("Image size: "+w+" x "+h);
        int[] map = nq.getColorMap();
        Tracer tracer = new Tracer(pixels,w,h);
        edges = new Contour[map.length];
        System.out.println(Arrays.toString(map));



        for(int i=0;i<map.length;i++){
            tracer.setActiveColor(map[i]);
            edges[i] = tracer.trace();
        }
        System.out.println("Finished trace");

        for(int i=0;i<pixels.length;i++){
            pixels[i] = -123;
        }

        System.out.println("Drawing onto view");
        for(int i=0;i<map.length;i++){
            for(int j=0;j<edges[i].size();j++){
                Pixel p = edges[i].getPixel(j);
                pixels[p.getIndex(w)] = p.getColor();
            }
        }
        System.out.println("Process complete");
    }

}
