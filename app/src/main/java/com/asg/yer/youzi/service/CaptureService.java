package com.asg.yer.youzi.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.asg.yer.youzi.MainActivity;
import com.asg.yer.youzi.Others.UpdateUiEvent;
import com.asg.yer.youzi.R;
import com.asg.yer.youzi.YouZiApplication;
import com.asg.yer.youzi.utils.Logger;
import com.yxsj.yz.e10.CC01;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;

import static java.lang.Thread.sleep;

/**
 * 截图Service
 * Created by YER on 17/12/14.
 */
public class CaptureService extends Service {

    private static final String TAG = "CService";

    private MediaProjectionManager mMpmngr;
    private MediaProjection mMpj;
    private ImageReader mImageReader;
    private String mImageName;
    private String mImagePath;
    public static boolean boolSaveBitmap = false;
    private int screenDensity;
    private int windowWidth;
    private int windowHeight;
    private VirtualDisplay mVirtualDisplay;
    private WindowManager wm;
    private Timer sceenCaptureTimer;

    private CC01 cc01 = new CC01();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        createNotitycation();
        createEnvironment();

        boolean isInit = cc01.useAssetsInit(this);
        if (isInit) {
            Logger.e("加载assets成功");
        } else {
            Logger.e("加载assets失败");
        }
        cc01.debugSet(1);

        createSceenShot();
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

//    @Override
//    public void onCreate() {
//        super.onCreate();
//        createNotitycation();
//        createEnvironment();
//        createSceenShot();
//    }


    private void createNotitycation() {
        Notification notification = new Notification(R.mipmap.ic_launcher, "zhou",
                System.currentTimeMillis());
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        if (Build.VERSION.SDK_INT < 16) {
            Class clazz = notification.getClass();
            try {
                Method m2 = clazz.getDeclaredMethod("setLatestEventInfo", Context.class, CharSequence.class, CharSequence.class, PendingIntent.class);
                m2.invoke(notification, getApplication(), "标题", "内容", pendingIntent);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        } else {
            notification = new Notification.Builder(getApplication())
                    .setAutoCancel(true)
                    .setContentTitle("标题")
                    .setContentText("内容")
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setWhen(System.currentTimeMillis())
                    .build();
        }
        startForeground(0x1982, notification);
    }


    private void createEnvironment() {
        mImagePath = Environment.getExternalStorageDirectory().getPath() + "/screenshort/";
        mMpmngr = ((YouZiApplication) getApplication()).getMpmngr();
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowWidth = wm.getDefaultDisplay().getWidth();
        windowHeight = wm.getDefaultDisplay().getHeight();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        screenDensity = displayMetrics.densityDpi;
        mImageReader = ImageReader.newInstance(windowWidth, windowHeight, 0x1, 2);

    }

    private void createSceenShot() {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startVirtual();
            }
        }, 0);
        sceenCaptureTimer = new Timer();
        sceenCaptureTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                try {
                    sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                startCapture();

            }
        }, 0, 500/* 表示0毫秒之後，每隔200毫秒執行一次 */);

    }

    private void stopVirtual() {
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
    }

    Bitmap bitmap;
    int[] pix;
    int[] myCards;  //從jni 里面取到有用參數  取牌

    private void startCapture() {
        mImageName = System.currentTimeMillis() + ".png";
        Logger.e("image name is : " + mImageName);
        Image image = mImageReader.acquireLatestImage();
        if (image != null) {
            int width = image.getWidth();
            int height = image.getHeight();
            Image.Plane[] planes = image.getPlanes();
            ByteBuffer buffer = planes[0].getBuffer();

            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;

            bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
            if (pix == null) {
                pix = new int[width * height];
            }
            bitmap.getPixels(pix, 0, width, 0, 0, width, height);
            int[] resultPixels = CC01.picMatch(pix, width, height);
            Logger.e("我要计算==============================");

            if (null != resultPixels && resultPixels.length > 2) {
                myCards = new int[resultPixels[1] * 2];
                for (int i = 0; i < myCards.length; i++) {
                    myCards[i] = resultPixels[i + 2];
                    Logger.e(i+"我要计算=============================="+resultPixels[i + 2]);
                }
                EventBus.getDefault().post(new UpdateUiEvent(myCards));
            } else {
                //没牌
                EventBus.getDefault().post(new UpdateUiEvent(1, ""));
            }
            bitmap.isRecycled();
            image.close();
        }
        if (bitmap != null && boolSaveBitmap) {
            try {
                File fileFolder = new File(mImagePath);
                if (!fileFolder.exists())
                    fileFolder.mkdirs();
                File file = new File(mImagePath, mImageName);
                if (!file.exists()) {
                    Log.e(TAG, "file create success "+mImagePath);
                    file.createNewFile();
                }
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
                Logger.e("file save success ! are you ok");
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }
        }
    }

    private void startVirtual() {
        if (mMpj != null) {
            virtualDisplay();
        } else {
            setUpMediaProjection();
            virtualDisplay();
        }
    }

    private void setUpMediaProjection() {
        int resultCode = ((YouZiApplication) getApplication()).getResultCode();
        Intent data = ((YouZiApplication) getApplication()).getResultIntent();
        if (mMpmngr == null) { //时间跑久了  mMpmngr会被回收
            mMpmngr = ((YouZiApplication) getApplication()).getMpmngr();
        }
        mMpj = mMpmngr.getMediaProjection(resultCode, data);
    }

    private void virtualDisplay() {
        mVirtualDisplay = mMpj.createVirtualDisplay("capture_screen", windowWidth, windowHeight, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mImageReader.getSurface(), null, null);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopVirtual();
        if (mMpj != null) {
            mMpj.stop();
            mMpj = null;
        }
        if (null != sceenCaptureTimer) {
            sceenCaptureTimer.cancel();
            sceenCaptureTimer = null;
            Logger.e("onDestroy: ===== service被取消了");
        }

    }

}
