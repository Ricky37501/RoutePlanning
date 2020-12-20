package com.whutxhm.findroute;

import java.sql.Connection;
import java.sql.DriverManager;


public class DBUtils {
    private static String driver = "org.gjt.mm.mysql.Driver";
    private static String user = "root";
    private static String password = "root";
    private static Connection connection;

    public static void connect(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                connection = getConn("db_traffic");
                if (connection==null){
                    System.out.println("数据库连接失败");
                }
                else {
                    System.out.println("数据库连接成功");
                }
            }
        }).start();
    }

    public static Connection getConnection(){
        return connection;
    }

    public static Connection getConn(String dbName){
        Connection connection = null;
        try{
            Class.forName(driver);    //动态加载类
            String ip = "192.168.43.249";      //本机IP地址，每次更换网路都需做修改！！！手机和运行MySQL的电脑要处于同一网络

            //尝试建立到给定数据库URL的连接(preparedStatement.setString设置的是中文字符必须在url后加seUnicode=true&characterEncoding=utf8，否则传出去的是乱码)
            connection = DriverManager.getConnection("jdbc:mysql://"+ip+":3306/"+dbName+"?useSSL=true"+"&seUnicode=true&characterEncoding=utf8", user, password);
        }catch(Exception e){
            e.printStackTrace();
        }
        return connection;
    }
}
