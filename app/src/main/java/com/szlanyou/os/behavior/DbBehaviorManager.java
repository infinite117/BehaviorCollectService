package com.szlanyou.os.behavior;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * @author: shijy
 * @description:  对数据库进行具体操作
 * @time: 2018/12/18
 */

public class DbBehaviorManager {
    private final static String TAG = "DbBehaviorControllor";

    private Context mContext;

    private MyBehaviorDBOpenHelper dbHelper;
    private SQLiteDatabase sqLiteDatabase;

    private ContentResolver contentResolver;
    private ContentValues values;

    /**
     * 构造函数
     * */
    public DbBehaviorManager(Context context) {
        mContext = context;
    }

    /**
     * @description: 创建数据库
     * */
    public void createBehaviorDB() {
        dbHelper = new MyBehaviorDBOpenHelper(mContext, "behavior.db");
        sqLiteDatabase = dbHelper.getWritableDatabase();
    }

    /**
     * @description: 插入数据
     * 插入数据后，会发送广播，提示有数据插入
     * 插入失败返回 -1
     * 插入成功返回当前数据的总数
     * ex：
     *    第一步：private DbBehaviorControllor mDbBehaviorControllor;
     *    第二步：在 Activity 的 onCtreate（）方法中进行初始化
     *            mDbBehaviorControllor = new DbBehaviorControllor(Activity.this);
     *    第三步：Uri countUri = mDbBehaviorControllor.insertData();
     *            String sDataCount = countUri.toString();
     *            sDataCount = sDataCount.substring(sDataCount.lastIndexOf("/")+1);
     *
     * Integer.valueOf(sDataCount) 即 当前数据总数
     * */
    public Uri insertData() {
        contentResolver = mContext.getContentResolver();
        values = new ContentValues();
        values.put("name", "GuiaShih - " + System.currentTimeMillis());
        Uri insertUri = contentResolver.insert(BehaviorProvider.URI.BEHAVIOR_INSERT_URI, values);
        String sDataCount = insertUri.toString();
        sDataCount = sDataCount.substring(sDataCount.lastIndexOf("/") + 1);
//        // 发送广播
        Intent intent = new Intent();
        intent.setAction("com.szlanyou.os.behavior.MyReceiver");
        intent.putExtra("msg", sDataCount);
        mContext.sendBroadcast(intent);
        return insertUri;
    }

    /**
     * @description: 查询所有数据
     * 直接调用
     * 没有返回值
     * ex：
     *    第一步：private DbBehaviorControllor mDbBehaviorControllor;
     *    第二步：在 Activity 的 onCtreate（）方法中进行初始化
     *            mDbBehaviorControllor = new DbBehaviorControllor(Activity.this);
     *    第三步：mDbBehaviorControllor.queryAllData();
     * */
    public void queryAllData() {
        contentResolver = mContext.getContentResolver();
        Cursor cursorQueryAll= contentResolver.query(BehaviorProvider.URI.BEHAVIOR_QUERY_ALL_URI, null, null, null,null);
        if (cursorQueryAll != null && cursorQueryAll.getCount() > 0) {
            String name;
            while (cursorQueryAll.moveToNext()) {
                String curId = cursorQueryAll.getString(0);
                name = cursorQueryAll.getString(1);
                Log.d("GuiaShih", " curId = " + curId + " name : " + name);
            }
            cursorQueryAll.close();
        }
    }

    /**
     * @description: 查询 N(目前为10) 条数据
     * 直接调用，查询最前的 10 行数据
     * 没有返回值
     * ex：
     *    第一步：private DbBehaviorControllor mDbBehaviorControllor;
     *    第二步：在 Activity 的 onCtreate（）方法中进行初始化
     *            mDbBehaviorControllor = new DbBehaviorControllor(Activity.this);
     *    第三步：mDbBehaviorControllor.queryNData();
     * */
    public String[] queryNData(int dataCount) {
        String data[] = new String[dataCount];
        int count = 0;
        contentResolver = mContext.getContentResolver();
        Cursor cursorQueryN = contentResolver.query(BehaviorProvider.URI.BEHAVIOR_QUERY_N_URI, null, String.valueOf(dataCount), null, null);
        if (cursorQueryN != null && cursorQueryN.getCount() > 0) {
            String name;
            while (cursorQueryN.moveToNext()) {
                String curId = cursorQueryN.getString(0);
                name = cursorQueryN.getString(1);
//                Log.d("GuiaShih", " curId = " + curId + " name : " + name );
                data[count] = "curId = " + curId + " , name : " + name;
                count++;
            }
            cursorQueryN.close();
        }
        for (int i = 0; i<dataCount; i++){
            Log.d("GuiaShih", data[i]);
        }
        return data;
    }

    /**
     * @description: 删除数据
     * 直接调用，删除前 10 行的数据
     * 传入的参数 deleteDataCount 为要删除的数据数目
     * 没有返回值
     * ex：
     *    第一步：private DbBehaviorControllor mDbBehaviorControllor;
     *    第二步：在 Activity 的 onCtreate（）方法中进行初始化
     *            mDbBehaviorControllor = new DbBehaviorControllor(Activity.this);
     *    第三步：mDbBehaviorControllor.deleteData();
     * */
    public void deleteData(int deleteDataCount) {
        contentResolver = mContext.getContentResolver();
//        contentResolver.delete(BehaviorProvider.URI.BEHAVIOR_DELETE_URI, null, null);
        String indexData[] = new String[deleteDataCount];
        int count = 0;
        Cursor mCursor = contentResolver.query(BehaviorProvider.URI.BEHAVIOR_QUERY_ALL_URI, null, null, null, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            while (mCursor.moveToNext() && count < deleteDataCount) {
                indexData[count] = mCursor.getString(0);
                count++;
            }
        }
        mCursor.close();
        for (int i = 0; i < count; i++){
            contentResolver.delete(BehaviorProvider.URI.BEHAVIOR_DELETE_URI, "id=?",new String[]{indexData[i]});
        }
    }

    /**
     * @description: 获取数据库表中数据总数
     * 返回值 dataCount 统计数据总数
     * 计数原理是：利用 moveToNext 方法来实现计数
     *
     * 该方法会返回 数据总数 dataCount
     * 调用时候可以 定义一个 long 类型的变量来接收返回值。
     * ex：
     *    第一步：private DbBehaviorControllor mDbBehaviorControllor;
     *    第二步：在 Activity 的 onCtreate（）方法中进行初始化
     *            mDbBehaviorControllor = new DbBehaviorControllor(Activity.this);
     *    第三步：long mDataCount = mDbBehaviorControllor.getDataCount();
     * */
    public int getDataCount() {
        contentResolver = mContext.getContentResolver();
        Cursor countCursor = contentResolver.query(BehaviorProvider.URI.BEHAVIOR_QUERY_ALL_URI, null, null, null, null);
        return countCursor.getCount();
    }

}
