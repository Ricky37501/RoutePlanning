package com.whutxhm.findroute;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultActivity extends AppCompatActivity {

    private String mStartStation;
    private String mEndStation;
    private Context rContext=this;
    private boolean bThreadDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rContext = this;
        setContentView(R.layout.activity_result);
        Intent intent = getIntent();
        mStartStation = intent.getStringExtra("start");
        mEndStation = intent.getStringExtra("end");

        //执行数据库查询
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = handler.obtainMessage();
                ArrayList<ArrayList<String>> routeOption = new ArrayList<>();//每个元素存放线路ID，始发站，终点站, 路线名(参考行驶方向)
                Connection connection = DBUtils.getConn("db_traffic");
                findRoute(connection,routeOption);
                message.obj = routeOption;
                handler.sendMessage(message);
            }
        }).start();

    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ArrayList<ArrayList<String>> routeOption = (ArrayList<ArrayList<String>>) msg.obj;
            //设置输出TextView的布局参数
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 20, 0, 0);
            //分割线布局设置
            LinearLayout.LayoutParams viewLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 3);
            viewLayoutParams.setMargins(0, 20, 0, 0);

            LinearLayout container = findViewById(R.id.container);
            //输出直达路径
            for (int i = 0; i < routeOption.size(); i++) {
                TextView tv = new TextView(rContext);
                tv.setLayoutParams(layoutParams);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                String output = routeOption.get(i).get(0) + "   " + routeOption.get(i).get(3);
                tv.setText(output);

                View v = new View(rContext);
                v.setLayoutParams(viewLayoutParams);
                v.setBackgroundColor(Color.parseColor("#303F9F"));

                container.addView(tv);
                container.addView(v);
            }
        }
    };
    public void findRoute(Connection connection,ArrayList<ArrayList<String>> routeOption) {
        try {
            String sql = "SELECT DISTINCT r.routeID,s1.stationName as routeStart,s2.stationName as routeEnd " +
                    "FROM tb_route r " +
                    "JOIN tb_station s1 ON s1.stationID=r.startStation " +
                    "JOIN tb_station s2 ON s2.stationID=r.endStation " +
                    "WHERE EXISTS( " +
                    "  SELECT * " +
                    "  FROM tb_sequence sec1 " +
                    "  WHERE stationID in( " +
                    "    SELECT stationID " +
                    "    FROM tb_station " +
                    "    WHERE stationName= ? " +
                    "  )" +
                    "  AND sec1.routeID=r.routeID " +
                    ")" +
                    "AND EXISTS( " +
                    "  SELECT *" +
                    "  FROM tb_sequence sec2 " +
                    "  WHERE stationID in( " +
                    "    SELECT stationID " +
                    "    FROM tb_station " +
                    "    WHERE stationName= ? " +
                    "  ) " +
                    "  AND sec2.routeID=r.routeID " +
                    ") ";
            if (connection != null) {    //成功与数据库建立连接
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                if (preparedStatement != null) {
                    preparedStatement.setString(1, mStartStation);
                    preparedStatement.setString(2, mEndStation);
                    //执行sql查询语句并返回结果集
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet != null) {     //查询结果不为空
                        while (resultSet.next()) {
                            ArrayList<String> temp = new ArrayList<>();
                            temp.add(resultSet.getString("routeID"));
                            temp.add(resultSet.getString("routeStart"));
                            temp.add(resultSet.getString("routeEnd"));
                            routeOption.add(temp);
                        }
                        resultSet.close();
                        preparedStatement.close();
                        //connection.close();
                    } else {
                        ToastUtils.show(rContext, "查询结果为空");
                    }
                } else {
                    ToastUtils.show(rContext, "执行语句准备出错");
                }
            } else {
                ToastUtils.show(rContext, "数据库连接失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.show(rContext, "发生异常");
        }

        for (ArrayList<String> arr : routeOption) {
            if (connection != null) {
                int direction = getDirection(connection, arr.get(0));
                String routeName;
                if (direction == 1) {
                    routeName = arr.get(1) + "往" + arr.get(2);
                } else {
                    routeName = arr.get(2) + "往" + arr.get(1);
                }
                arr.add(routeName);
            } else {
                arr.add("NAN");
            }
        }

    }

    public int getDirection(Connection connection, String routeID) {
        String sql = "SELECT s1.orderNumber-s2.orderNumber as direction " +
                "FROM tb_sequence s1 " +
                "JOIN tb_sequence s2 ON ( " +
                "s2.routeID=s1.routeID " +
                "AND " +
                "s2.stationID in( " +
                "  SELECT stationID " +
                "  FROM tb_station " +
                "  WHERE stationName= ?  " +
                "  ) " +
                ") " +
                "WHERE s1.routeID= ? " +
                "AND s1.stationID in( " +
                "  SELECT stationID " +
                "  FROM tb_station " +
                "  WHERE stationName= ?  " +
                "  )";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            if (preparedStatement != null) {
                preparedStatement.setString(1, mStartStation);
                preparedStatement.setString(2, routeID);
                preparedStatement.setString(3, mEndStation);
                //执行sql查询语句并返回结果集
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet != null) {     //查询结果不为空
                    int direction = 0;
                    while (resultSet.next()) {
                        direction = resultSet.getInt("direction");
                    }
                    return direction < 0 ? -1 : 1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}