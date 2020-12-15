package com.whutxhm.findroute;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Intent intent=getIntent();
        String startStation=intent.getStringExtra("start");
        String endStation=intent.getStringExtra("end");

        //执行数据库查询炒作
        DBUtils.testConnection(this);
    }
}