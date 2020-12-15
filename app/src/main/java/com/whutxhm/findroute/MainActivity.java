package com.whutxhm.findroute;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    public static Context sContext;
    private EditText startStation;
    private EditText endStation;
    private Button searchButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startStation=findViewById(R.id.star_point);
        endStation=findViewById(R.id.end_point);
        searchButton=findViewById(R.id.search_btn);

        sContext=this;
        DBUtils.testConnection(sContext);
    }

    public void startSearch(View view) {
        Intent intent =new Intent(MainActivity.this,ResultActivity.class);
        String start=startStation.getText().toString();
        String end=endStation.getText().toString();
        intent.putExtra("start",start);
        intent.putExtra("end",end);
        startActivity(intent);
    }
}