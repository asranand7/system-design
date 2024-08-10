package org.system.design.airlineseats.commons;

import org.system.design.commons.ConnectionPool;

import java.sql.*;

public class QueriesUtil {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASS = "password";

    public static void prepareTable() {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // Establish connection
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Update the seats table to set all userId values to NULL
            String updateSQL = "UPDATE seats SET userId = NULL WHERE tripId = 1";
            stmt = conn.prepareStatement(updateSQL);
            int rowsUpdated = stmt.executeUpdate();

            System.out.println("Preparation complete. " + rowsUpdated + " seats have been reset.");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void printBookedSeats(ConnectionPool connectionPool) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Establish connection
            conn = connectionPool.getConnection();

            // Query to get the booked seats along with user names
            String query = "SELECT seats.seatId, users.name FROM seats " +
                    "JOIN users ON seats.userId = users.userId " +
                    "WHERE seats.tripId = 1 AND seats.userId IS NOT NULL";

            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            System.out.println("\nBooked Seats:");
            int count = 0;
            while (rs.next()) {
                count++;
                int seatId = rs.getInt("seatId");
                String userName = rs.getString("name");
                System.out.println("Seat ID: " + seatId + ", User: " + userName);
            }
            System.out.println(count + " Seats booked out of 120");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) connectionPool.releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
