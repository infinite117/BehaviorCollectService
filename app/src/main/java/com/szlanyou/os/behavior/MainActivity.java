package com.szlanyou.os.behavior;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.szlanyou.os.behavior.services.BehaviorProviderService;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import git.dzc.okhttputilslib.CacheType;
import git.dzc.okhttputilslib.JsonCallback;
import git.dzc.okhttputilslib.OKHttpUtils;
import okhttp3.Call;
import okhttp3.Response;

public class MainActivity extends Activity implements View.OnClickListener {
    private String TAG = MainActivity.this.getClass().getSimpleName();

    // author : shijy
    private Button btnInsert;           // 插入数据按钮
    private Button btnQuery;            // 查询数据按钮
    private Button btnDelete;           // 删除数据按钮
    private Button btnGetDataCount;     // 获取数据条数按钮

    private Button btnCreate;
    private Button btnQueryN;

    // author : shijy
    private TextView tvInsert;          // 插入数据结果显示文本框
    private TextView tvQuery;           // 查询数据结果显示文本框
    private TextView tvDelete;          // 删除数据结果显示文本框
    private TextView tvGetDataCount;    // 获取数据条数结果显示文本框

    private TextView tvCreate;
    private TextView tvQueryN;

    private DbBehaviorManager mDbBehaviorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDbBehaviorManager = new DbBehaviorManager(MainActivity.this);
		//启动用户数据采集服务
		Intent startIntent = new Intent(this,BehaviorProviderService.class);
        startService(startIntent);


        initItemFindViewById();
//        initEvent();
    }

    /**
     * @author: shijy
     * @description: init the item findViewById
     * @date: 2018-12-14 (create)
     * */
    private void initItemFindViewById() {
        // 绑定布局中的按钮
        btnInsert       = (Button) findViewById(R.id.insert_btn_id);
        btnQuery        = (Button) findViewById(R.id.query_btn_id);
        btnDelete       = (Button) findViewById(R.id.delete_btn_id);
        btnGetDataCount = (Button) findViewById(R.id.datacount_btn_id);

        btnCreate       = (Button) findViewById(R.id.create_btn_id);
        btnQueryN       = (Button) findViewById(R.id.query_n_btn_id);

        // 绑定布局中的文本框
        tvInsert        = (TextView) findViewById(R.id.insert_res_id);
        tvQuery         = (TextView) findViewById(R.id.query_res_id);
        tvDelete        = (TextView) findViewById(R.id.delete_res_id);
        tvGetDataCount  = (TextView) findViewById(R.id.datacount_res_id);

        tvCreate        = (TextView) findViewById(R.id.create_res_id);
        tvQueryN        = (TextView) findViewById(R.id.query_n_res_id);

        btnCreate.setOnClickListener(this);
        btnInsert.setOnClickListener(this);
        btnQuery.setOnClickListener(this);
        btnQueryN.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btnGetDataCount.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.create_btn_id:
//                mDbBehaviorManager.createBehaviorDB();
                tvCreate.setText("Create success");
                break;

            case R.id.insert_btn_id:
                Uri curUri =  mDbBehaviorManager.insertData();
                String sDataCount = curUri.toString();
                sDataCount = sDataCount.substring(sDataCount.lastIndexOf("/")+1);
                tvInsert.setText("insert data : " + "  uri  " + curUri.toString());
                Log.d("GuiaShih", " uri : " + curUri.toString() + Integer.valueOf(sDataCount));
                break;

            case R.id.query_btn_id:
                mDbBehaviorManager.queryAllData();
                tvQuery.setText("query data : 瞅你咋的！");
                break;

            case R.id.query_n_btn_id:
//                String sqlQueryN = "SELECT * FROM behavior where id in (SELECT id FROM behavior order by id limit "+ 5 +")";
//                String sqlQueryN = "SELECT * FROM behavior where rownum<10";
                mDbBehaviorManager.queryNData(100);
                tvQueryN.setText("ten data have been shown!");
                break;

            case R.id.delete_btn_id:
                mDbBehaviorManager.deleteData(100);
                tvDelete.setText("delete data : shijy你丫欠抽！");
                break;

            case R.id.datacount_btn_id:
                long dataCount = mDbBehaviorManager.getDataCount();
                tvGetDataCount.setText("get data count : 你敢试试！  " + dataCount );
                Log.d("GuiaShih", " dataCount = " + dataCount);
                break;
            default:
                break;
        }
    }
}
