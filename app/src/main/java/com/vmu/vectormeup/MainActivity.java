package com.vmu.vectormeup;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.os.Environment;
import android.os.Parcelable;
import android.os.SystemClock;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.graphics.*;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.View.OnClickListener;
import com.ipaulpro.afilechooser.utils.*;
import android.content.Intent;
import android.net.Uri;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.ActivityNotFoundException;
import android.provider.MediaStore;
import java.util.*;

import java.io.*;

import com.vmu.vectormeup.preprocesses.NeuQuant;
import com.vmu.vectormeup.threading.TracePool;
import com.vmu.vectormeup.trace.*;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CHOOSER = 1234;
    private int sampleFactor = 10;
    private int minPathSize = 1;
    private int colors = 256;
    private ImageView canvas;
    private Bitmap image;
    private int[] pixels;
    private int w = 0;
    private int h = 0;
    private NeuQuant nq;
    private Contour[] edges;
    private TracePool tp = new TracePool();
    private long mLastClickTime = 0;
    private Button startButton;
    private int blurRadius;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        setContentView(R.layout.activity_main);

        final ProgressBar progressBar =  (ProgressBar) findViewById(R.id.progressBar);
        final TextView progressText = (TextView) findViewById(R.id.progressText);


        final SeekBar seekBarSampleFactor = (SeekBar) findViewById(R.id.sampleFactor);
        final SeekBar seekBarPathSize = (SeekBar) findViewById(R.id.pathSize);
        final SeekBar seekBarBlur = (SeekBar) findViewById(R.id.blur);
        final SeekBar seekBarColours = (SeekBar) findViewById(R.id.colors);

        //Sets colours of the seekbar
        seekBarSampleFactor.getProgressDrawable().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
        seekBarSampleFactor.getThumb().setColorFilter(getResources().getCo‌​lor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        seekBarColours.getProgressDrawable().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
        seekBarColours.getThumb().setColorFilter(getResources().getCo‌​lor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        seekBarPathSize.getProgressDrawable().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
        seekBarPathSize.getThumb().setColorFilter(getResources().getCo‌​lor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        seekBarBlur.getProgressDrawable().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
        seekBarBlur.getThumb().setColorFilter(getResources().getCo‌​lor(R.color.white), PorterDuff.Mode.SRC_ATOP);

        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgressTintList(ColorStateList.valueOf(Color.WHITE));

        progressBar.setVisibility(View.INVISIBLE);
        progressText.setVisibility(View.INVISIBLE);

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

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });
//
        seekBarPathSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                TextView label = (TextView) findViewById(R.id.pathSizeValue);
                label.setText(String.valueOf(progress));
                minPathSize = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });

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

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });

        //Gets the events for the color seekbar
        seekBarBlur.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView label = (TextView) findViewById(R.id.blurValue);
                int value = progress;
                label.setText(String.valueOf(value));
                blurRadius = value;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });

        //Sets the ImageView to be clickable and allows selection of any bitmap image
        ImageView myImage = (ImageView) findViewById(R.id.myImageView);
        myImage.setClickable(true);
        myImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                     openImageIntent();
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(v.getContext(),
                            "Could not open file browser",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        startButton = (Button) findViewById(R.id.startButton);
        startButton.setEnabled(false);
        startButton.setText("");
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ColorFilter filter = canvas.getColorFilter();
                progressBar.setVisibility(View.VISIBLE);
                progressText.setVisibility(View.VISIBLE);
                progressText.setText("Quantising image colors");
                canvas.setColorFilter(Color.rgb(123, 123, 123), android.graphics.PorterDuff.Mode.MULTIPLY);

                class WorkThread implements Runnable{
                    View v;
                    public WorkThread(View v){
                        this.v = v;
                    }

                    public void run() {
                        try {
                            set(image);
                        } catch (IOException e) {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(v.getContext(),
                                            "Error: Could not quantise color",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                progressText.setText("Tracing contours");
                            }
                        });
                        Bitmap newImg = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
                        tp.setStartButton(startButton);
                        tp.setProgressTetx(progressText);
                        tp.setMainActivity(MainActivity.this);
                        Canvas vectorCanvas = new Canvas(newImg);
                        tp.setCanvas(vectorCanvas);
                        trace();
                        final Bitmap result = newImg;
                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                progressBar.setVisibility(View.INVISIBLE);
                                progressText.setVisibility(View.INVISIBLE);
                                canvas.setColorFilter(filter);
                                refreshCanvas(result);
                            }
                        });

                    }
                }

                WorkThread w = new WorkThread(v);
                Thread thread = new Thread(w);
                thread.start();

                Toast.makeText(v.getContext(),
                        "Finished on "+w+"x"+h+" image",
                        Toast.LENGTH_LONG).show();
            }

        });

    }

    public void refreshCanvas(Bitmap newImage) {
        canvas.setImageBitmap(newImage);
        TextView canvasText = (TextView) findViewById(R.id.myImageViewText);
        canvasText.setText("");
    }


    public void set(Bitmap img) throws IOException {
        Bitmap image;
        float aspectRatio = img.getHeight() > 0 ? (float)(img.getWidth())/ img.getHeight() : 1 ;
        System.out.println("aspect ratio: " + aspectRatio);
        image = Bitmap.createScaledBitmap(img,(int) (1000*aspectRatio),(int) (1000*(1/aspectRatio)),false);
//        if(img.getWidth() > 1500 && img.getWidth() <= 2000 || img.getHeight() > 1500 && img.getHeight() <= 2000)
//            image = Bitmap.createScaledBitmap(img, (int)(img.getWidth()*0.75),(int)(img.getHeight()*0.75),false);
//        else if(img.getWidth() > 2000 && img.getWidth() <= 3000 || img.getHeight() > 2000 && img.getHeight() <= 3000)
//            image = Bitmap.createScaledBitmap(img, (int)(img.getWidth()*0.5),(int)(img.getHeight()*0.5),false);
//        else if(img.getWidth() > 3000 || img.getHeight() > 3000)
//            image = Bitmap.createScaledBitmap(img, (int)(img.getWidth()*0.25),(int)(img.getHeight()*0.25),false);
//        else
//            image = Bitmap.createScaledBitmap(img,(img.getWidth()),(img.getHeight()),false);

        if(blurRadius > 0)
            image = BlurImage(image,blurRadius);

        nq = new NeuQuant(image, canvas, colors,sampleFactor);
        nq.init();
        this.w = image.getWidth();
        this.h = image.getHeight();
        System.out.println("w = " + w + ", h = " + h);

        pixels = new int[w * h];
        try {
            image.getPixels(pixels, 0, image.getWidth(), 0, 0, w, h);
        } catch (ArrayIndexOutOfBoundsException | IllegalStateException | IllegalArgumentException e) {
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this,
                            "Could not process image into array", Toast.LENGTH_LONG).show();
                    Log.e("Image Array", "Could not process bitmap array");
                }
            });

        }

        for (int i = 0; i < w*h; i++) {
            pixels[i] = nq.convert(pixels[i]);
        }
    }

    public void trace(){
        int[] map = nq.getColorMap();
        tp.setParams(pixels,w,h,map,minPathSize,0,getApplicationContext());
        tp.traceImage();
    }

    @SuppressLint("NewApi")
    Bitmap BlurImage (Bitmap input,int radius)
    {
        try
        {
            RenderScript rsScript = RenderScript.create(getApplicationContext());
            Allocation alloc = Allocation.createFromBitmap(rsScript, input);
            ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rsScript,   Element.U8_4(rsScript));
            blur.setRadius(radius);
            blur.setInput(alloc);

            Bitmap result = Bitmap.createBitmap(input.getWidth(), input.getHeight(), Bitmap.Config.ARGB_8888);
            Allocation outAlloc = Allocation.createFromBitmap(rsScript, result);

            blur.forEach(outAlloc);
            outAlloc.copyTo(result);

            rsScript.destroy();
            return result;
        }
        catch (Exception e) {
            // TODO: handle exception
            return input;
        }

    }
    private Uri outputFileUri;

    private void openImageIntent() {

        // Determine Uri of camera image to save.
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "VectorUp" + File.separator);
        root.mkdirs();
        final String fname = System.currentTimeMillis()+"_img.jpg";
        final File sdImageMainDirectory = new File(root, fname);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for(ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, REQUEST_CHOOSER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CHOOSER) {
                final boolean isCamera;
                if (data == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }
                Uri selectedImageUri;
                if (isCamera) {
                    selectedImageUri = outputFileUri;
                } else {
                    selectedImageUri = data == null ? null : data.getData();
                }

                if(selectedImageUri != null){
                    final String path = FileUtils.getPath(this, selectedImageUri);
                    try{
                        image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    }catch(IOException e){
                        Toast.makeText(MainActivity.this,
                                "Could not load image", Toast.LENGTH_LONG).show();
                    }
                    refreshCanvas(image);
                    this.w = image.getWidth();
                    this.h = image.getHeight();
                    Toast.makeText(MainActivity.this,
                            w+"x"+h+" image selected", Toast.LENGTH_LONG).show();
                    startButton.setEnabled(true);
                    startButton.setText("Vectorize");
                }
            }
        }
    }
}
