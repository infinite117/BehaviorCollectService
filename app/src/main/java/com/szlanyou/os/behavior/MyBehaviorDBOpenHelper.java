package com.szlanyou.os.behavior;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by shijy on 2018/12/14.
 */

public class MyBehaviorDBOpenHelper extends SQLiteOpenHelper {
    // 数据库版本号
    private static Integer Version = 2;

    // 构造函数
    // context : 上下文对象
    // name    : 数据库名称
    // param   : factory
    // version : 当前数据库的版本，值必须是整数并且是递增的状态
    public MyBehaviorDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public MyBehaviorDBOpenHelper(Context context, String name , int version) {
        this(context, name, null, version);
    }

    public MyBehaviorDBOpenHelper(Context context, String name) {
        this(context, name, Version);
    }

    // 数据库被创建的时候调用
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建了数据并创建一个叫 behavior 的表
        // SQLite 数据创建支持的数据类型：整型数据、字符串类型、日期类型、二进制的数据类型
        String sqlCreate = "create table if not exists behavior(id integer primary key autoincrement,name varchar(20))";
        // execSQL 用于执行 SQL 语句.
        // 完成数据库的创建
        db.execSQL(sqlCreate);
        Log.d("GuiaShih", " create success! ");
        // 数据库实际上是没有被创建或者打开的，直到 getWritableDatabase( ) 或者 getReadableDatabase( )
        // 方法中的一个被调用时才会进行创建或者打开
    }

    // 数据库升级时调用
    // 如果 DATABASE_VERSION 值被改为 2 ，系统发现现有数据库版本不同，即会调用 onUpgrade( ) 方法
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        System.out.println("更新数据库版本为： " + newVersion);
    }
}
