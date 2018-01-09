package com.asg.yer.youzi.service;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.asg.yer.youzi.Others.UpPecentEvent;
import com.asg.yer.youzi.Others.UpdateUiEvent;
import com.asg.yer.youzi.manager.YzWindowManager;
import com.asg.yer.youzi.utils.Caculation;
import com.asg.yer.youzi.utils.Logger;
import com.asg.yer.youzi.window.SusDetailWindow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

/**
 * Created by YER on 17-12-12.
 */
public class YzWindowService extends Service {

    /**
     * 用于在线程中创建或移除悬浮窗。
     */
    private Handler handler = new Handler();

    /**
     * 定时器，定时进行检测当前应该创建还是移除悬浮窗。
     */
//    private Timer timer;

    private Caculation caculation;

    private int playersNum;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        EventBus.getDefault().register(this);

        caculation  = Caculation.getInstant(this);
        playersNum =  SusDetailWindow.MUN_PERS;

        caculation.initCaculation(playersNum);
//        // 开启定时器，每隔0.5秒刷新一次
//        if (timer == null) {
//            timer = new Timer();
//            timer.scheduleAtFixedRate(new RefreshTask(), 0, 500);
//
//        }

//        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }


    class RefreshTask extends TimerTask {

        @Override
        public void run() {
            // 当前界面是桌面，且没有悬浮窗显示，则创建悬浮窗。
            if (isHome() && !YzWindowManager.isWindowShowing()) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        YzWindowManager.createSmallWindow(getApplicationContext());
                    }
                });
            }
            // 当前界面不是桌面，且有悬浮窗显示，则移除悬浮窗。
//            else if (!isHome() && YzWindowManager.isWindowShowing()) {
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        YzWindowManager.removeSmallWindow(getApplicationContext());
//                        YzWindowManager.removeBigWindow(getApplicationContext());
//                    }
//                });
//            }

            // 当前界面是桌面，且有悬浮窗显示，则更新内存数据。
            else if (isHome() && YzWindowManager.isWindowShowing()) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
//                        YzWindowManager.updateUsedPercent(getApplicationContext());
                    }
                });
            }

//            Intent intent = new Intent(getApplicationContext(), ScreenShotActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
        }

    }

    /**
     * 判断当前界面是否是桌面
     */
    private boolean isHome() {
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
        return getHomes().contains(rti.get(0).topActivity.getPackageName());
    }

    /**
     * 获得属于桌面的应用的应用包名称
     *
     * @return 返回包含所有包名的字符串列表
     */
    private List<String> getHomes() {
        List<String> names = new ArrayList<>();
        PackageManager packageManager = this.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        return names;
    }


    int [] curCards;
    @Subscribe
    public void onEvent(final UpdateUiEvent uiEvent) {
        int []intCards = uiEvent.cards;

//        String cardArr = uiEvent.strCards;

        if(!Arrays.equals(curCards,intCards)&&null!=intCards){
            curCards = intCards;
            for (int i = 0; i < curCards.length; i++) {
                Logger.e(i+"我要计算=============================="+curCards[i]);
            }
            caculation.setIntTotalPlayers(SusDetailWindow.MUN_PERS);
            caculation.doCalculation(curCards);
        }

        if(isHome() && !YzWindowManager.isWindowShowing()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    YzWindowManager.createSmallWindow(getApplicationContext());
                }
            });
        }

    }

    @Subscribe
    public void onEvent(final UpPecentEvent pecentEvent) {

        if (YzWindowManager.isWindowShowing()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Logger.e("UpPecentEvent", "onPostExecute: ==============111111111111==="+ pecentEvent.perent);
                    YzWindowManager.updateUsedPercent(getApplicationContext(), pecentEvent.perent);
                }
            });
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // Service被终止的同时也停止定时器继续运行
//        if (null != timer) {
//            timer.cancel();
//            timer = null;
//        }
        EventBus.getDefault().unregister(this);
    }
}
