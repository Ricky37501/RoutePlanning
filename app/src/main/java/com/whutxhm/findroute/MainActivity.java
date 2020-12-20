package com.whutxhm.findroute;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    public  Context sContext;
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
        DBUtils.connect();
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                    String start = startStation.getText().toString();
                    String end = endStation.getText().toString();
                    intent.putExtra("start", start);
                    intent.putExtra("end", end);
                    startActivity(intent);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
}
