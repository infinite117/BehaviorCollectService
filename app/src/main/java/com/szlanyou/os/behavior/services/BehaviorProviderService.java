package com.szlanyou.os.behavior.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.szlanyou.os.behavior.DbBehaviorManager;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import git.dzc.okhttputilslib.CacheType;
import git.dzc.okhttputilslib.JsonCallback;
import git.dzc.okhttputilslib.OKHttpUtils;
import okhttp3.Call;
import okhttp3.Response;


/**
 * Author: LiuChengXin
 * Data: 2018/12/17
 * Description: 用户行为采集服务
 */

public class BehaviorProviderService extends Service {

    private final String TAG = "BehaviorCollectService";

    private Context mContext;
    private OKHttpUtils okHttpUtils;
    private NetworkChange networkChange;
    private DbBehaviorManager mDbBehaviorManager;
    private String url = "https://vitappkf.venucia.com/iov_gw/api";

    public BehaviorProviderService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChange);
    }

    private void init() {
        okHttpUtils = new OKHttpUtils.Builder(this).build();
        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("com.szlanyou.os.behavior.MyReceiver");
        networkChange = new NetworkChange();
        registerReceiver(networkChange, filter);
        mDbBehaviorManager = new DbBehaviorManager(this);
        checkDataUpload(this);
    }

    private void checkDataUpload(Context context) {
        if (isNetworkConnected(context) || isWifiConnected(context) || isMobileConnected(context)) {
            Log.d(TAG, "checkDataUpload: 111");
            if (!isFileExist()) {
                Log.d(TAG, "checkDataUpload: 222");
                uploadData(CacheType.UPDATE_FILE);
            } else {
                Log.d(TAG, "checkDataUpload: 333");
                //获取数据库中数据的总数，将所有的数据写入文件并删除数据库中数据
                int dataCount = mDbBehaviorManager.getDataCount();
                String[] data = getContentProviderData(dataCount);
                Log.d(TAG, "checkDataUpload: 444");
                boolean a = writeFiles(data);
                Log.d(TAG, "checkDataUpload: a = " + a);
                if (a) {
                    Log.d(TAG, "checkDataUpload: 555");
                    mDbBehaviorManager.deleteData(dataCount);
                    uploadData(CacheType.UPDATE_FILE);
                }
            }
        }
    }

    /**
     * 检查从数据库读出来写下的文件是否存在
     */
    private boolean isFileExist() {
        try {
            File sceneFile = new File(String.valueOf(getFilesDir()));
            File[] files = sceneFile.listFiles();
            Log.d(TAG, "isFileExist.getFilesDir:" + String.valueOf(getFilesDir()));
            if (files != null) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * 判断是否有网络连接
     *
     * @param context
     * @return
     */
    private boolean isNetworkConnected(Context context) {
        if (context != null) {
            // 获取手机所有连接管理对象(包括对wi-fi,net等连接的管理)
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            // 获取NetworkInfo对象
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            //判断NetworkInfo对象是否为空
            if (networkInfo != null) {
                return networkInfo.isAvailable();
            }
        }
        return false;
    }


    /**
     * 判断WIFI网络是否可用
     *
     * @param context
     * @return
     */
    private boolean isWifiConnected(Context context) {
        if (context != null) {
            // 获取手机所有连接管理对象(包括对wi-fi,net等连接的管理)
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            // 获取NetworkInfo对象
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            //判断NetworkInfo对象是否为空 并且类型是否为WIFI
            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
                return networkInfo.isAvailable();
        }
        return false;
    }

    /**
     * 判断MOBILE网络是否可用
     *
     * @param context
     * @return
     */
    private boolean isMobileConnected(Context context) {
        if (context != null) {
            //获取手机所有连接管理对象(包括对wi-fi,net等连接的管理)
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            //获取NetworkInfo对象
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            //判断NetworkInfo对象是否为空 并且类型是否为MOBILE
            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE)
                return networkInfo.isAvailable();
        }
        return false;
    }

    /**
     * 获取当前网络连接的类型信息
     * 原生
     *
     * @param context
     * @return
     */
    public int getConnectedType(Context context) {
        if (context != null) {
            //获取手机所有连接管理对象
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            //获取NetworkInfo对象
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                //返回NetworkInfo的类型
                return networkInfo.getType();
            }
        }
        return -1;
    }

    /**
     * 读取ContentProvider的数据,需要考虑中文字符
     */
    public String[] getContentProviderData(int dataCount) {
        return mDbBehaviorManager.queryNData(dataCount);
    }

    /**
     * 将ContentProvider读取的数据写入文件
     */
    public boolean writeFiles(String[] msg) {
//        FileOutputStream outputStream;
//        try {
//            outputStream = openFileOutput("test.txt", Context.MODE_PRIVATE);
//            outputStream.write(msg.getBytes());
//            outputStream.flush();
//            outputStream.close();
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }

        try {
            //创建字节输出流对象
            FileOutputStream fout = new FileOutputStream(new File(getFilesDir().toString() + "/test.txt"));
            //创建字节流缓冲区，加快写出速度
            BufferedOutputStream bout = new BufferedOutputStream(fout);
            //创建字符输出流对象
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(bout, "utf-8"));

            for(String message:msg) {
                bw.write(message + "\t\n");
            }

            //注意关闭的先后顺序，先打开的后关闭，后打开的先关闭
            bw.close();
            bout.close();
            fout.close();
            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将文件上传到服务器
     */
    private void uploadData(@CacheType int cacheType) {
//        String address = Environment.getExternalStorageDirectory() + File.separator + "upload" + File.separator + "da_20180921.txt";
        if (isFileExist()) {
            File file = new File(String.valueOf(getFilesDir()) + "/test.txt");
            Map<String, Object> map = new HashMap<>();
            map.put("api", "os.iov331.carsteward.userBehaviorUpload");
            map.put("appCode", "venucia");

            okHttpUtils.post_file(url, map, file, mContext, jsonCallback);
        }
    }

    private JsonCallback<Response> jsonCallback = new JsonCallback<Response>() {
        @Override
        public void onFailure(Call call, Exception e) {
            onFail(e);
        }

        @Override
        public void onResponse(Call call, final Response response) throws IOException {
            if (response != null) {
                try {
                    File file = new File(getFilesDir() + "/test.txt");
                    file.delete();
                    Log.d(TAG, "uploadData.onResponse: " + response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void onFail(final Exception e) {
        Log.d(TAG, "uploadData.onFail: " + e.toString());
    }

    /**
     *
     */
    private class NetworkChange extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                Log.d(TAG, "onReceive:11 ");
                checkDataUpload(context);
            } else if (intent.getAction().equals("com.szlanyou.os.behavior.MyReceiver")) {
                Log.d(TAG, "onReceive:22 ");
                Log.d(TAG, "onReceive: " + Integer.valueOf(intent.getStringExtra("msg")));
                if (Integer.valueOf(intent.getStringExtra("msg")) >= 100) {
                    checkDataUpload(context);
                }
            }
        }
    }

}

