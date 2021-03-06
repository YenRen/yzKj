package com.asg.yer.youzi.manager;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import com.asg.yer.youzi.OnePxActivity;
import com.asg.yer.youzi.R;
import com.asg.yer.youzi.window.SusDetailWindow;
import com.asg.yer.youzi.window.SusWindow;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by YER on 17-12-12.
 *
 */
public class YzWindowManager {


    private Context mContext;

    private WeakReference<Activity> mActivityWref;

    public static YzWindowManager gDefualt;

    public static YzWindowManager getInstance(Context pContext) {
        if (gDefualt == null) {
            gDefualt = new YzWindowManager(pContext.getApplicationContext());
        }
        return gDefualt;
    }
    private YzWindowManager(Context pContext) {
        this.mContext = pContext;
    }

    public void setActivity(Activity pActivity) {
        mActivityWref = new WeakReference<Activity>(pActivity);
    }

    public void startActivity() {
        OnePxActivity.actionToSinglePixelActivity(mContext);
    }

    public void finishActivity() {
        //结束掉SinglePixelActivity
        if (mActivityWref != null) {
            Activity activity = mActivityWref.get();
            if (activity != null) {
                activity.finish();
            }
        }
    }


    // 小悬浮窗View的实例
    private static SusWindow smallWindow;

    // 大悬浮窗View的实例
    private static SusDetailWindow bigWindow;

    // 小悬浮窗View的参数
    private static LayoutParams smallWindowParams;

    // 大悬浮窗View的参数
    private static LayoutParams bigWindowParams;

    // 用于控制在屏幕上添加或移除悬浮窗
    private static WindowManager mWindowManager;

    // 用于获取手机可用内存
    private static ActivityManager mActivityManager;

    /**
     * 创建一个小悬浮窗。初始位置为屏幕的右部中间位置。
     */
    public static void createSmallWindow(Context context) {
        WindowManager windowManager = getWindowManager(context);
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        if (smallWindow == null) {
            smallWindow = new SusWindow(context);
            if (smallWindowParams == null) {
                smallWindowParams = new LayoutParams();
                //https://www.liaohuqiu.net/cn/posts/android-windows-manager/  type :TYPE_PHONE 这里TYPE_PHONE在6.0的oppo上没有事但在6.0的三星上报错。
                smallWindowParams.type = LayoutParams.TYPE_TOAST;
                smallWindowParams.format = PixelFormat.RGBA_8888;
                smallWindowParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
                smallWindowParams.gravity = Gravity.START | Gravity.TOP;
                smallWindowParams.width = SusWindow.viewWidth;
                smallWindowParams.height = SusWindow.viewHeight;
                smallWindowParams.x = screenWidth;
                smallWindowParams.y = screenHeight / 2;
            }
            smallWindow.setParams(smallWindowParams);
            windowManager.addView(smallWindow, smallWindowParams);
        }
    }

    /**
     * 将小悬浮窗从屏幕上移除。
     */
    public static void removeSmallWindow(Context context) {
        if (smallWindow != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(smallWindow);
            smallWindow = null;
        }
    }

    /**
     * 创建一个大悬浮窗。位置为屏幕正中间。
     */
    public static void createBigWindow(Context context) {
        WindowManager windowManager = getWindowManager(context);
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        if (bigWindow == null) {
            bigWindow = new SusDetailWindow(context);
            if (bigWindowParams == null) {
                bigWindowParams = new LayoutParams();
                bigWindowParams.x = screenWidth / 2 - SusDetailWindow.viewWidth / 2;
                bigWindowParams.y = screenHeight / 2 - SusDetailWindow.viewHeight / 2;
                bigWindowParams.type = LayoutParams.TYPE_TOAST;
                bigWindowParams.format = PixelFormat.RGBA_8888;
                bigWindowParams.gravity = Gravity.START | Gravity.TOP;
                bigWindowParams.width = SusDetailWindow.viewWidth;
                bigWindowParams.height = SusDetailWindow.viewHeight;
            }
            windowManager.addView(bigWindow, bigWindowParams);
        }
    }

    /**
     * 将大悬浮窗从屏幕上移除。
     */
    public static void removeBigWindow(Context context) {
        if (bigWindow != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(bigWindow);
            bigWindow = null;
        }
    }

    /**
     * 更新小悬浮窗的TextView上的数据，显示内存使用的百分比。
     */
    public static void updateUsedPercent(Context context,String info) {
        if (smallWindow != null) {
            TextView percentView = (TextView) smallWindow.findViewById(R.id.percent);



            percentView.setText(info);
        }
    }

    /**
     * 是否有悬浮窗(包括小悬浮窗和大悬浮窗)显示在屏幕上。
     *
     * @return 有悬浮窗显示在桌面上返回true，没有的话返回false。
     */
    public static boolean isWindowShowing() {
        return smallWindow != null || bigWindow != null;
    }

    /**
     * 如果WindowManager还未创建，则创建一个新的WindowManager返回。否则返回当前已创建的WindowManager。
     *
     * @param context 必须为应用程序的Context.
     * @return WindowManager的实例，用于控制在屏幕上添加或移除悬浮窗。
     */
    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    /**
     * 如果ActivityManager还未创建，则创建一个新的ActivityManager返回。否则返回当前已创建的ActivityManager。
     *
     * @param context 可传入应用程序上下文。
     * @return ActivityManager的实例，用于获取手机可用内存。
     */
    private static ActivityManager getActivityManager(Context context) {
        if (mActivityManager == null) {
            mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }
        return mActivityManager;
    }

    /**
     * 计算已使用内存的百分比，并返回。
     *
     * @param context 可传入应用程序上下文。
     * @return 已使用内存的百分比，以字符串形式返回。
     */
    public static String getUsedPercentValue(Context context) {
        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            br.close();
            long totalMemorySize = Integer.parseInt(subMemoryLine.replaceAll("\\D+", ""));
            long availableSize = getAvailableMemory(context) / 1024;
            int percent = (int) ((totalMemorySize - availableSize) / (float) totalMemorySize * 100);
            return percent + "%";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "悬浮窗";
    }

    /**
     * 获取当前可用内存，返回数据以字节为单位。
     *
     * @param context 可传入应用程序上下文。
     * @return 当前可用内存。
     */
    private static long getAvailableMemory(Context context) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        getActivityManager(context).getMemoryInfo(mi);
        return mi.availMem;
    }

}
