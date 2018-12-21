package com.szlanyou.os.behavior;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class BehaviorProvider extends ContentProvider {
    private final static String TAG = "BehaviorProvider";
    // 数据库名
    private final static String DB_BEHAVIOR = "behavior";
    private final static String AUTHORITY = "com.szlanyou.os.behavior.BehaviorProvider";
    private final static Uri BASE_URI = Uri.parse("content://com.szlanyou.os.behavior.BehaviorProvider");

    private SQLiteDatabase mDatabase;
    private MyBehaviorDBOpenHelper mMyBehaviorDBOpenHelper = null;

    private static final int BEHAVIOR_INSERT_CODE = 1;  // 插入数据
    private static final int BEHAVIOR_QUERY_ALL_CODE = 2;  // 查询所有数据
    private static final int BEHAVIOR_QUERY_N_CODE = 3;  // 查询 N 条数据
    private static final int BEHAVIOR_DELETE_CODE = 4;  // 删除数据

    // 用来存放所有合法的 Uri 的容器
    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // 写入该主机名的匹配规则
    // 保存一些合法的 uri
    static {
        uriMatcher.addURI(AUTHORITY, "behavior/insert", BEHAVIOR_INSERT_CODE);
        uriMatcher.addURI(AUTHORITY, "behavior/query_all", BEHAVIOR_QUERY_ALL_CODE);
        uriMatcher.addURI(AUTHORITY, "behavior/query_n", BEHAVIOR_QUERY_N_CODE);
        uriMatcher.addURI(AUTHORITY, "behavior/delete", BEHAVIOR_DELETE_CODE);
    }

    // 对外提供的URI
    public interface URI {
        Uri BEHAVIOR_INSERT_URI = Uri.parse("content://" + AUTHORITY + "/behavior/insert");
        Uri BEHAVIOR_QUERY_ALL_URI = Uri.parse("content://" + AUTHORITY + "/behavior/query_all");
        Uri BEHAVIOR_QUERY_N_URI = Uri.parse("content://" + AUTHORITY + "/behavior/query_n");
        Uri BEHAVIOR_DELETE_URI = Uri.parse("content://" + AUTHORITY + "/behavior/delete");
    }

    private ContentResolver mContentResolver;
    private int queryDataCount = 0;

    public BehaviorProvider() {

    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
//        Log.e(TAG, " onCreate : ");
        mMyBehaviorDBOpenHelper = new MyBehaviorDBOpenHelper(getContext(), "behavior.db");
        mDatabase = mMyBehaviorDBOpenHelper.getWritableDatabase();
        mContentResolver = getContext().getContentResolver();
        return false;
    }

    /**
     * 增加数据
     * 参数一： uri          相应匹配的 uri ，执行对应操作
     * 参数二： values       数据的值
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        switch (uriMatcher.match(uri)) {
            case BEHAVIOR_INSERT_CODE:
                // 得到连接对象
                long rowId = mDatabase.insert(DB_BEHAVIOR, null, values);
                if (rowId == -1) {
                    uri = null;
                } else {
                    mContentResolver.notifyChange(BASE_URI, null);
                    Cursor cursor = this.query(URI.BEHAVIOR_QUERY_ALL_URI, null, null, null, null);
                    uri = ContentUris.withAppendedId(uri,cursor.getCount());
                }
                break;
            default:
                Log.d("GuiaShih", " insert uri 不匹配");
                break;
        }
        return uri;
    }

    /**
     * 删除数据
     * 参数一： uri
     * 参数二： selection
     * 参数三： selectionArgs
     * 参数二、三 是对要删除的数据进行条件限制
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        int number = 0;
        switch (uriMatcher.match(uri)) {
            case BEHAVIOR_DELETE_CODE:
                // 得到连接对象
                number = mDatabase.delete(DB_BEHAVIOR, selection, selectionArgs);
                mContentResolver.notifyChange(BASE_URI, null);
                break;
            default:
                Log.d("GuiaShih", " delete uri 不匹配");
                break;
        }
        return number;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
//        throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    /**
     * 查询数据
     * BEHAVIOR_QUERY_ALL_CODE    删除所有数据
     * BEHAVIOR_QUERY_N_CODE      删除指定数目的数据
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        switch (uriMatcher.match(uri)) {
            case BEHAVIOR_QUERY_ALL_CODE:
                // 得到连接对象
                mDatabase = mMyBehaviorDBOpenHelper.getReadableDatabase();
                if (mDatabase.isOpen()) {
//                    Cursor cursor = mDatabase.query(DB_BEHAVIOR, projection, selection,
//                            selectionArgs, null, null, sortOrder);
//                    cursor.setNotificationUri(mContentResolver, BASE_URI);
                    Cursor cursor = mDatabase.rawQuery("SELECT  * FROM behavior", selectionArgs);
                    return cursor;
                }
                break;
            case BEHAVIOR_QUERY_N_CODE:
                mDatabase = mMyBehaviorDBOpenHelper.getReadableDatabase();
                if (mDatabase.isOpen()) {
                    Cursor cursor = mDatabase.rawQuery("SELECT  * FROM behavior limit " + selection, selectionArgs);
                    return cursor;
                }
                break;
            default:
                Log.d("GuiaShih", " query uri 不匹配");
        }
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
