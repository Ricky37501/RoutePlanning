package com.whutxhm.findroute;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private EditText startStation;
    private EditText endStation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startStation=findViewById(R.id.star_point);
        endStation=findViewById(R.id.end_point);
        Button searchButton = findViewById(R.id.search_btn);
        Button reverseButton = findViewById(R.id.reverse_btn);
        //预载数据库
        DBUtils.connect();
        //设置按钮点击事件
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                String start = startStation.getText().toString();
                String end = endStation.getText().toString();
                intent.putExtra("start", start);
                intent.putExtra("end", end);
                startActivity(intent);
            }
        });
        reverseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempStart = startStation.getText().toString();
                String tempEnd = endStation.getText().toString();
                endStation.setText(tempStart);
                startStation.setText(tempEnd);
            }
        });
    }
}
