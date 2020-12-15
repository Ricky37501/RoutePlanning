package com.whutxhm.findroute;
import android.content.Context;
import java.sql.Connection;
import java.sql.DriverManager;


public class DBUtils {
    //MySQL驱动
    private static String driver = "org.gjt.mm.mysql.Driver";
    //用户名
    private static String user = "root";
    //密码
    private static String password = "root";

    public static void testConnection(final Context tContext){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Connection connection = getConn("medical");
                if (connection==null){
                    ToastUtils.show(tContext,"数据库连接失败");
                }
                else {
                    ToastUtils.show(tContext,"数据库连接成功");
                }
            }
        }).start();
    }

    private static Connection getConn(String dbName){

        Connection connection = null;
        try{
            Class.forName(driver);    //动态加载类
            String ip = "192.168.43.249";      //本机IP地址，每次更换网路都需做修改！！！手机和运行MySQL的电脑要处于同一网络

            //尝试建立到给定数据库URL的连接
            connection = DriverManager.getConnection("jdbc:mysql://"+ip+":3306/"+dbName+"?useSSL=true", user, password);
        }catch(Exception e){
            e.printStackTrace();
        }
        return connection;
    }
}
