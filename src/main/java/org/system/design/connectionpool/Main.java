package org.system.design.connectionpool;

import java.sql.*;

public class Main {
    static String jdbcUrl = "jdbc:postgresql://localhost:5432/postgres";
    static String userName = "postgres";
    static String password = "password";


    public static void main(String[] args) throws SQLException, InterruptedException, ClassNotFoundException {
        ConnectionPool connectionPool = new ConnectionPool(jdbcUrl,userName, password, 10);

        test(100, connectionPool);
        test(250, connectionPool);
        test(500, connectionPool);
    }

    private static void test(int numberOfRequests, ConnectionPool connectionPool) throws SQLException, InterruptedException {
        System.out.println(numberOfRequests + " Requests");
        benchMarkConnectionPool(numberOfRequests, connectionPool);

        benchMarkNonConnectionPool(numberOfRequests);
        System.out.println("===========================================================================");
    }


    public static void benchMarkNonConnectionPool(int numberOfRequests) throws SQLException {
        long startTime = System.currentTimeMillis();
        for(int i = 0;i < numberOfRequests;i++){
            Connection connection = ConnectionUtils.createNewConnection(jdbcUrl, userName, password);
            try{
                executeQuery(connection);

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connection.close();
            }
        }
        long endTime = System.currentTimeMillis();
        long timeElasped = endTime - startTime;
        System.out.println("benchMarkNonConnectionPool time taken: " + timeElasped + "ms" );
    }

    private static void executeQuery(Connection connection) throws SQLException {
        long startTime = System.currentTimeMillis();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * from public.student where id = 1");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            int age = resultSet.getInt("age");
            String email = resultSet.getString("email");
            Date enrolled = resultSet.getDate("enrolled");
            // Print the retrieved data in one line
//            System.out.println("ID: " + id + ", Name: " + name + ", Age: " + age + ", Email: " + email + ", Enrolled: " + enrolled);
        }
        long endTime = System.currentTimeMillis();
        long timeElasped = endTime - startTime;
//        System.out.println("Query executed in: " + timeElasped + "ms");


    }

    public static void benchMarkConnectionPool(int numberOfRequests, ConnectionPool connectionPool) throws SQLException, InterruptedException {
        long startTime = System.currentTimeMillis();

        for(int i = 0;i < numberOfRequests;i++){
            Connection connection = connectionPool.getConnection();
            try {
                executeQuery(connection);
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connectionPool.releaseConnection(connection);
            }
        }
        long endTime = System.currentTimeMillis();
        long timeElasped = endTime - startTime;
        System.out.println("benchMarkConnectionPool time taken: " + timeElasped + "ms" );

    }
}