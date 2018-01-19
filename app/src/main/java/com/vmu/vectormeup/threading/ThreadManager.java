package com.vmu.vectormeup.threading;

import android.os.Looper;
import android.os.Message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;

/**
 * Created by Daniel on 18/01/2018.
 */

public class ThreadManager {
//    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
//    private final BlockingQueue<Runnable> mDecodeWorkQueue = new LinkedBlockingQueue<Runnable>();
//    // Sets the amount of time an idle thread waits before terminating
//    private static final int KEEP_ALIVE_TIME = 1;
//    // Sets the Time Unit to seconds
//    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
//    private static ThreadPoolExecutor mDecodeThreadPool;
//    private static Handler mHandler;
//
//    static  {
//        // Creates a single static instance of ThreadManager
//       ThreadManager instance = new ThreadManager();
//    }
//    private ThreadManager(){
//                // Creates a thread pool manager
//        mDecodeThreadPool = new ThreadPoolExecutor(
//                NUMBER_OF_CORES,       // Initial pool size
//                NUMBER_OF_CORES,       // Max pool size
//                KEEP_ALIVE_TIME,
//                KEEP_ALIVE_TIME_UNIT,
//                mDecodeWorkQueue);
//        mHandler = new Handler(Looper.getMainLooper()) {
//            /*
//             * handleMessage() defines the operations to perform when
//             * the Handler receives a new Message to process.
//             */
//            @Override
//            public void handleMessage(Message inputMessage) {
//
//            }
//
//
//    }


}
