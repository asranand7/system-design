package org.system.design.connectionpool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionUtils {
    public static Connection createNewConnection(String url, String user, String password) throws SQLException {
        long startTime = System.currentTimeMillis();
        Connection connection =  DriverManager.getConnection(url, user, password);
        long endTime = System.currentTimeMillis();
        long timeElasped = endTime - startTime;
//        System.out.println("connection creation time taken: " + timeElasped + "ms" );
        return connection;
    }

}