package com.asg.yer.youzi;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.asg.yer.youzi.Others.ScreenBroadcastListener;
import com.asg.yer.youzi.manager.YzWindowManager;
import com.asg.yer.youzi.screenshot.ScreenShotActivity;
import com.asg.yer.youzi.service.CaptureService;
import com.asg.yer.youzi.service.YzWindowService;
import com.asg.yer.youzi.utils.Logger;

import static java.lang.Thread.sleep;

/**
 * Created by YER on 17-12-12.
 */
public class MainActivity extends AppCompatActivity {

    private Intent intent;
    private MediaProjectionManager mMpMngr;
    private static final int REQUEST_MEDIA_PROJECTION = 1; //询问授权   用静态变量保存  保需求一次授权
    private Intent mResultIntent = null;
    private int mResultCode = 0;
    public static final String TAG = "MainAc";

    private Button btnStart,btnStop;
    private boolean isScreenOn = true;
    private boolean isSceenCapture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMpMngr = (MediaProjectionManager) getApplicationContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mResultIntent = ((YouZiApplication) getApplication()).getResultIntent();
        mResultCode = ((YouZiApplication) getApplication()).getResultCode();

        btnStart = (Button)findViewById(R.id.start_btn);
        btnStop = (Button)findViewById(R.id.stop_btn);
        intent = new Intent(MainActivity.this, YzWindowService.class);
//        startService(intent);

        final YzWindowManager screenManager = YzWindowManager.getInstance(MainActivity.this);
        ScreenBroadcastListener listener = new ScreenBroadcastListener(this);
        listener.registerListener(new ScreenBroadcastListener.ScreenStateListener() {
            @Override
            public void onScreenOn() {
                screenManager.finishActivity();
//                Log.e("YER", "onScreenOn: 屏被唤醒了"+Thread.currentThread().getName());
                Logger.e("onScreenOn: 屏被唤醒了"+Thread.currentThread().getName());
//                startFloat(null);

                startScreenShot(null);
            }

            @Override
            public void onScreenOff() {
                screenManager.startActivity();
                Logger.e("onScreenOn: 屏被熄灭了"+Thread.currentThread().getName());
//                Log.e("YER", "onScreenOn: 屏被熄灭了"+Thread.currentThread().getName());
//                stopFloat(null);

                screenShotStop(null);

                System.gc();
            }
        });

    }


    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, "onRestart: onRestart onRestart onRestart on Restart on Restart  " );
        //TODO  if service is stopped  you can restart it
    }

    /*
         *开启浮窗
         *
         *
            参数为false代表只有当前activity是task根，指应用启动的第一个activity时，才有效;
            如果为true则忽略这个限制，任何activity都可以有效。
            moveTaskToBack调用后，task中activity的顺序不会发生变化，例如A启动B，B中调用此方法退到后台，
            重新启动应用会调用B中的onRestart-onStart-onResume方法，不会重新调用onCreate，而且在B中按下back键返回的还是A，这就是退到后台的功能。
            另外在activity中按下back键，实际是调用了finish方法，应用退出。虽然应用已经退出，但进程没有被杀死，android中一个应用运行于独立的一个虚拟机实例中，
            所以在重新启动应用时一个类中的静态对象还保持着运行时的状态，注意在合适位置复位这些状态。

         */
    public void startFloat(){
        startService(intent);
//        moveTaskToBack(true);
    }

    /*
     *关闭浮窗
     */
    public void stopFloat(View view){
        YzWindowManager.removeBigWindow(this);
        YzWindowManager.removeSmallWindow(this);
        stopService(intent);
    }

    /*
     * 连续截图
     * @param view
     */
    public void startScreenShot(View view){
        if(!isSceenCapture) {
            startFloat();

            if (mResultIntent != null && mResultCode != 0) {
                startService(new Intent(getApplicationContext(), CaptureService.class));
            } else {
                startActivityForResult(mMpMngr.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    moveTaskToBack(true);
                }
            }).start();
        }else {
            Toast.makeText(this, getResources().getString(R.string.is_capture_ing),Toast.LENGTH_SHORT).show();
        }
        isSceenCapture = true;
    }

    /*
     * 截图一张图
     * @param view
     */
    public void screenOneShot(View view){
        startActivity(new Intent(MainActivity.this, ScreenShotActivity.class));
    }

    /*
     * 停止截屏服务
     */
    public void screenShotStop(View view){

        stopFloat(null);

        stopService(new Intent(getApplicationContext(),  CaptureService.class ));

        isSceenCapture = false;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK) {
                Logger.e("get capture permission success!");
                mResultCode = resultCode;
                mResultIntent = data;
                ((YouZiApplication) getApplication()).setResultCode(resultCode);
                ((YouZiApplication) getApplication()).setResultIntent(data);
                ((YouZiApplication) getApplication()).setMpmngr(mMpMngr);
                startService(new Intent(getApplicationContext(),CaptureService.class));
            }
        }
    }


    @Override
    public void onBackPressed() {
//        moveTaskToBack(true);
        this.finish();
    }


    /*
     * 控制保存图片   保存地址 /storage/emulated/0/screenshort/
     */
    public void doMath(View view){
        CaptureService.boolSaveBitmap = !CaptureService.boolSaveBitmap;
    }




}
