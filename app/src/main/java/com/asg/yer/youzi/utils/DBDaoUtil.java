package com.asg.yer.youzi.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.asg.yer.youzi.gen.CardGameDao;
import com.asg.yer.youzi.gen.DaoMaster;
import com.asg.yer.youzi.gen.DaoSession;

import org.greenrobot.greendao.database.Database;

/**
 * Created by apple on 2017/12/18.
 */

public class DBDaoUtil {
    private static DaoSession daoSession;
    private static DaoSession getDaoSession(Context context) {
        if (daoSession==null) {
            DaoMaster.OpenHelper openHelper = new DaoMaster.OpenHelper(context, "student"){
                @Override
                public void onUpgrade(Database db, int oldVersion, int newVersion) {
                    switch (oldVersion) {
                        case 1:
                            db.beginTransaction();
                            db.execSQL("...");
                            db.setTransactionSuccessful();
                            db.endTransaction();
                            break;
                    }
                }
            };
            SQLiteDatabase db = openHelper.getWritableDatabase();
            DaoMaster daoMaster = new DaoMaster(db);
            daoSession = daoMaster.newSession();
        }
        return daoSession;
    }

    public static CardGameDao getUserDao(Context context) {
        return getDaoSession(context).getCardGameDao();
    }
}
