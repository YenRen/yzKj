package com.asg.yer.youzi.utils;

import android.util.Log;

/**
 * Created by YeR on 2017/12/26.
 */

public class Logger {
    private static boolean isLog = true;
    private static String strTag = "YER";

    public static void e(String msg){
       e(strTag,msg);
    }

    public static void e(String Tag ,String msg){
        if(isLog) Log.e(Tag, "e: "+msg);
    }

}
