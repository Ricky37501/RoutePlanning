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
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    private String mStartStation;
    private String mEndStation;
    private Context rContext = this;
    private Connection mConnection;
    @SuppressLint("HandlerLeak")
    private Handler handler1 = new Handler() {
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
    @SuppressLint("HandlerLeak")
    private Handler handler2 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ArrayList<ArrayList<String>> transRouteOption = (ArrayList<ArrayList<String>>) msg.obj;
            LinearLayout container = findViewById(R.id.container);
            //设置输出TextView的布局参数
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 20, 0, 0);
            //分割线布局设置
            LinearLayout.LayoutParams viewLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 3);
            viewLayoutParams.setMargins(0, 20, 0, 0);
            //输出一次转乘路径
            for (int i = 0; i < transRouteOption.size(); i++) {
                TextView tv = new TextView(rContext);
                tv.setLayoutParams(layoutParams);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                String output = transRouteOption.get(i).get(0) + "\n在 " + transRouteOption.get(i).get(1) + "站 转乘\n" + transRouteOption.get(i).get(2);
                tv.setText(output);

                View v = new View(rContext);
                v.setLayoutParams(viewLayoutParams);
                v.setBackgroundColor(Color.parseColor("#303F9F"));

                container.addView(tv);
                container.addView(v);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rContext = this;
        setContentView(R.layout.activity_result);
        Intent intent = getIntent();
        mStartStation = intent.getStringExtra("start");
        mEndStation = intent.getStringExtra("end");
        Bundle bundle = intent.getBundleExtra("bundle");
        //执行数据库查询
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message1 = handler1.obtainMessage();
                Message message2 = handler2.obtainMessage();
                ArrayList<ArrayList<String>> routeOption = new ArrayList<>();//每个元素存放线路ID，始发站，终点站, 路线名(参考行驶方向)
                ArrayList<ArrayList<String>> transRouteOption = new ArrayList<>();//第一个路线名 中转站 第二个路线名
                mConnection = DBUtils.getConnection();
                findDirectRoute(routeOption);
                findOneTransRoute(transRouteOption);
                message1.obj = routeOption;
                message2.obj = transRouteOption;
                handler1.sendMessage(message1);
                handler2.sendMessage(message2);
            }
        }).start();
    }

    //查询直达路线
    public void findDirectRoute(ArrayList<ArrayList<String>> routeOption) {
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
            PreparedStatement preparedStatement = mConnection.prepareStatement(sql);
            preparedStatement.setString(1, mStartStation);
            preparedStatement.setString(2, mEndStation);
            //执行sql查询语句并返回结果集
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ArrayList<String> temp = new ArrayList<>();
                temp.add(resultSet.getString("routeID"));
                temp.add(resultSet.getString("routeStart"));
                temp.add(resultSet.getString("routeEnd"));
                routeOption.add(temp);
            }
            resultSet.close();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.show(rContext, "发生异常");
        }
        for (ArrayList<String> arr : routeOption) {
            if (mConnection != null) {
                int direction = getDirection(arr.get(0), mStartStation, mEndStation);
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

    //查询行驶方向
    public int getDirection(String routeID, String startStation, String endStation) {
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
            PreparedStatement preparedStatement = mConnection.prepareStatement(sql);
            if (preparedStatement != null) {
                preparedStatement.setString(1, startStation);
                preparedStatement.setString(2, routeID);
                preparedStatement.setString(3, endStation);
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

    //查询一次转乘路线
    public void findOneTransRoute(ArrayList<ArrayList<String>> transRouteOption) {
        String sql = "SELECT DISTINCT r1.routeID as first,r1.startStation as ss1,r1.endStation as es1,r2.routeID as second,r2.startStation as ss2,r2.endStation as es2 " +
                "FROM tb_route r1 " +
                "JOIN tb_route r2 ON r1.routeID != r2.routeID " +
                "WHERE EXISTS( " +
                "  SELECT * " +
                "  FROM tb_sequence " +
                "  WHERE routeID=r1.routeID " +
                "  AND stationID in ( " +
                "      SELECT stationID " +
                "      FROM tb_station " +
                "      WHERE stationName= ?  " +
                "      )" +
                ")" +
                "AND NOT EXISTS(" +
                "  SELECT * " +
                "  FROM tb_sequence " +
                "  WHERE routeID=r1.routeID " +
                "  AND stationID in ( " +
                "      SELECT stationID " +
                "      FROM tb_station " +
                "      WHERE stationName= ? " +
                "      ) " +
                ") " +
                "AND EXISTS( " +
                "  SELECT * " +
                "  FROM tb_sequence " +
                "  WHERE routeID=r2.routeID " +
                "  AND stationID in ( " +
                "      SELECT stationID " +
                "      FROM tb_station " +
                "      WHERE stationName = ?  " +
                "      ) " +
                ") " +
                "AND NOT EXISTS( " +
                "  SELECT * " +
                "  FROM tb_sequence " +
                "  WHERE routeID=r2.routeID " +
                "  AND stationID in ( " +
                "      SELECT stationID " +
                "      FROM tb_station " +
                "      WHERE stationName= ? " +
                "      ) " +
                ") " +
                "AND EXISTS( " +
                "  SELECT * " +
                "  FROM tb_sequence sec1 " +
                "  JOIN tb_sequence sec2 ON " +
                "  sec1.stationID=sec2.stationID " +
                "  WHERE sec1.routeID=r1.routeID " +
                "  AND sec2.routeID=r2.routeID " +
                ")";
        ArrayList<ArrayList<String>> rawRouteInfo = new ArrayList<>();//第一个路线ID,始发站ID，终点站ID 第二个路线ID,始发站ID，终点站ID
        try {
            PreparedStatement preparedStatement = mConnection.prepareStatement(sql);
            if (preparedStatement != null) {
                preparedStatement.setString(1, mStartStation);
                preparedStatement.setString(2, mEndStation);
                preparedStatement.setString(3, mEndStation);
                preparedStatement.setString(4, mStartStation);
                //执行sql查询语句并返回结果集
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    ArrayList<String> temp = new ArrayList<>();
                    temp.add(resultSet.getString("first"));
                    temp.add(resultSet.getString("ss1"));
                    temp.add(resultSet.getString("es1"));
                    temp.add(resultSet.getString("second"));
                    temp.add(resultSet.getString("ss2"));
                    temp.add(resultSet.getString("es2"));
                    rawRouteInfo.add((temp));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < rawRouteInfo.size(); i++) {
            String middleName = findMiddleStationName(rawRouteInfo.get(i).get(0), rawRouteInfo.get(i).get(3));
            String ss1_Name = findStationNameByID(rawRouteInfo.get(i).get(1));
            String es1_Name = findStationNameByID(rawRouteInfo.get(i).get(2));
            String ss2_Name = findStationNameByID(rawRouteInfo.get(i).get(4));
            String es2_Name = findStationNameByID(rawRouteInfo.get(i).get(5));
            int firstDirection = getDirection(rawRouteInfo.get(i).get(0), mStartStation, middleName);
            int secondDirection = getDirection(rawRouteInfo.get(i).get(3), middleName, mEndStation);
            String firstStationName;
            String secondStationName;
            if (firstDirection == 1) {
                firstStationName = rawRouteInfo.get(i).get(0) + "   " + ss1_Name + "往" + es1_Name;
            } else {
                firstStationName = rawRouteInfo.get(i).get(0) + "   " + es1_Name + "往" + ss1_Name;
            }
            if (secondDirection == 1) {
                secondStationName = rawRouteInfo.get(i).get(3) + "   " + ss2_Name + "往" + es2_Name;
            } else {
                secondStationName = rawRouteInfo.get(i).get(3) + "   " + es2_Name + "往" + ss2_Name;
            }
            ArrayList<String> temp = new ArrayList<>();
            temp.add(firstStationName);
            temp.add(middleName);
            temp.add(secondStationName);
            transRouteOption.add(temp);
        }
    }

    //查询中转站
    public String findMiddleStationName(String routeID1, String routeID2) {
        String routeName = "";
        String sql = "SELECT stationName " +
                "FROM tb_station s " +
                "WHERE EXISTS( " +
                "SELECT * " +
                "FROM tb_sequence " +
                "WHERE stationID=s.stationID " +
                "AND routeID= ? " +
                ") " +
                "AND EXISTS( " +
                "SELECT * " +
                "FROM tb_sequence " +
                "WHERE stationID=s.stationID " +
                "AND routeID= ? " +
                ") " +
                "LIMIT 0,1";
        try {
            PreparedStatement preparedStatement = mConnection.prepareStatement(sql);
            if (preparedStatement != null) {
                preparedStatement.setString(1, routeID1);
                preparedStatement.setString(2, routeID2);
                //执行sql查询语句并返回结果集
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    routeName = resultSet.getString("stationName");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return routeName;
    }

    //由站点id取得站点名
    public String findStationNameByID(String stationID) {
        String name = "";
        String sql = "SELECT stationName " +
                "FROM tb_station " +
                "WHERE stationID = ? ";
        try {
            PreparedStatement preparedStatement = mConnection.prepareStatement(sql);
            if (preparedStatement != null) {
                preparedStatement.setString(1, stationID);
                //执行sql查询语句并返回结果集
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    name = resultSet.getString("stationName");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return name;
    }
}
