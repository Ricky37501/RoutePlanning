package com.whutxhm.findroute;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    private String mStartStation="友谊大道华城广场";
    private String mEndStation="武汉火车站";
    private ArrayList<String> mRouteOption=new ArrayList<>();
    private Context rContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rContext=this;
        setContentView(R.layout.activity_result);
        Intent intent=getIntent();
        //mStartStation=intent.getStringExtra("start");
        //mEndStation=intent.getStringExtra("end");

        //执行数据库查询炒作
        //DBUtils.testConnection(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(findRoute()){
                    ToastUtils.show(rContext,mRouteOption.get(0));

                }
            }
        }).start();
    }
    public boolean findRoute(){
        Connection connection = DBUtils.getConn("db_traffic");
        try{
            String sql = "SELECT DISTINCT r.routeID " +
                    "FROM tb_route r " +
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
            if(connection != null){    //成功与数据库建立连接
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                if(preparedStatement != null){
                    preparedStatement.setString(1, mStartStation);
                    preparedStatement.setString(2, mEndStation);
                    //执行sql查询语句并返回结果集
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if(resultSet != null){     //查询结果不为空
                        while(resultSet.next()){
                            String temp=resultSet.getString("routeID");
                            mRouteOption.add(temp);
                        }
                        resultSet.close();
                        preparedStatement.close();
                        connection.close();
                        return true;
                    }else{
                        ToastUtils.show(rContext,"查询结果为空");
                        return false;//查询结果为空
                    }
                }else{
                    ToastUtils.show(rContext,"执行语句准备出错");
                    return false;//执行语句准备出错
                }
            }else{
                ToastUtils.show(rContext,"数据库连接失败");
                return false;//数据库连接失败
            }
        }catch(Exception e){
            e.printStackTrace();
            ToastUtils.show(rContext,"发生异常");
            return false;//发生异常
        }
    }
}