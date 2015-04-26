package uk.co.derekross.second_app.Service;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Bundle;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import retrofit.RestAdapter;
import uk.co.derekross.second_app.Utils.SharedPrefUtil;
import uk.co.derekross.second_app.retrofit.ImageData;
import uk.co.derekross.second_app.retrofit.Model;
import uk.co.derekross.second_app.retrofit.RetroFitHelper;

/**
 * Created by derek_000 on 21/04/2015.
 */
public class SlideShowService extends Service {

    public static final String PIC_IDS = "picIds";
    public static final String MESSENGER = "MESSENGER";
    public static final int BITMAP_TO_DISPLAY = 2;
    public static final int STOP_LOOP = 501;
    public static final int CONTINUE_LOOP = 502;
    private ArrayList<String> picIds = new ArrayList<String>();
    final public static int RECEIVE_ACTIVITY_MESSENGER = 500;
    private Messenger ActivityMessenger;
    private Model response;
    private CountDownLatch mLatch;
    private CountDownLatch mEmptyPicLatch;
    Bitmap mCurrentImage = null;
    final ReentrantReadWriteLock mArrayRWLock = new ReentrantReadWriteLock();
    private volatile boolean continueLoop = true;
    private int page = 0;


    class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RECEIVE_ACTIVITY_MESSENGER:
                    ActivityMessenger = (Messenger) msg.obj;
                    Log.e("Service", "handling message from activity starting slideshow");
                    startSlideShow();
                    break;
                case STOP_LOOP : continueLoop = false;
                    break;
                case CONTINUE_LOOP : continueLoop = true;
                    break;

            }
        }
    }


    final Messenger mMessenger = new Messenger(new IncomingHandler());


    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    private void getDataFromImgur(boolean l) {
        final boolean lock = l;
        //check if the id array has enough data

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("Service", "getDataFromImgur start");
                RestAdapter imgurAdapter = new RestAdapter.Builder()
                        .setEndpoint(RetroFitHelper.ImgurEndPoint)
                        .build();

                response = imgurAdapter.create(RetroFitHelper.class).getSubReditData("gonewild",page++);



                try {
                    mArrayRWLock.writeLock().lock();
                    for (ImageData d : response.getData()) {

                        picIds.add(d.getId());

                    }

                } finally {

                    mArrayRWLock.writeLock().unlock();
                }

                if (lock) {
                    mEmptyPicLatch.countDown();
                }
                Log.e("Service", "getDataFromImgur end");

            }
        }).start();
    }


    private void startSlideShow() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Log.e("Service", "startslideshow start");

                while (continueLoop) {
                    int numOfPics;
                    try {
                        mArrayRWLock.readLock().lock();
                        numOfPics = picIds.size();
                    } finally {
                        mArrayRWLock.readLock().unlock();
                    }

                    //if the array is empty get the images and wait until received before continue
                    if (numOfPics == 0) {
                        mEmptyPicLatch = new CountDownLatch(1);
                        getDataFromImgur(true);
                        try {
                            mEmptyPicLatch.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    //if there are only 4 pics then request new ones but keep going
                    if (numOfPics < 4) {
                        getDataFromImgur(false);
                    }

//get the image and make bitmap
                    try {
                        InputStream in = new URL(obtainUrl()).openStream();
                        mCurrentImage = BitmapFactory.decodeStream(in);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//display the image  imediately the first pass then use a countdown latch and countdown timer to slide show.
                    if(mLatch != null) {
                        try {
                            Log.e("Service", "Before mlatchawait");
                            mLatch.await();
                            Log.e("Service", "after mlatchawait");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    mLatch = new CountDownLatch(1);

                    Message msg = Message.obtain(null, BITMAP_TO_DISPLAY, mCurrentImage);

                    try {
                        ActivityMessenger.send(msg);
                        Log.e("Service", "bitmap sent");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    Log.e("Service", "slidecountdown before");
                    //new slideCountDown(500,100).start();
                    new timeOutThread().start();
                    Log.e("Service", "slidecountdown after");

                }


            }
        }).start();
    }

    //helper method to get url string using the first id and removing it
    //from the array
    private String obtainUrl() {
        String url = "";

        try {
            mArrayRWLock.writeLock().lock();
            url = "http://i.imgur.com/" + picIds.remove(0) + "b.jpg";
            Log.e("Service", url);
        } finally {
            mArrayRWLock.writeLock().unlock();
        }
        return url;
    }

   public class slideCountDown extends CountDownTimer {

        public slideCountDown(long start, long intervals) {
            super(start, intervals);
        }

        @Override
        public void onFinish() {
            Log.e("Service", "mlatch about to countdown. in slidecountdown");
            mLatch.countDown();

            Log.e("Service", "mlatch should have countdown. in slidecountdown");
        }

        @Override
        public void onTick(long millisUntilFinished) {
            Log.e("Service", "ontick");
        }
    }

    public class timeOutThread extends Thread{
        @Override
        public void run() {
            super.run();

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mLatch.countDown();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try{
            mArrayRWLock.writeLock().lock();
            for(String s: picIds){
                SharedPrefUtil.savePicIdArray(s,this);
            }
        } finally {
            mArrayRWLock.writeLock().unlock();
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        String[] arrayPics = SharedPrefUtil.loadPicIdArray(this);
        try{
            mArrayRWLock.writeLock().lock();
            for(String s : arrayPics){
                picIds.add(s);
        }

        }finally {
            mArrayRWLock.writeLock().unlock();
            SharedPrefUtil.nullOutPicIdArray(this);
        }
    }
}
