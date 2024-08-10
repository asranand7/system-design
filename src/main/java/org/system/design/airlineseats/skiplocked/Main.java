package org.system.design.airlineseats.skiplocked;

import org.system.design.commons.ConnectionPool;

import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASS = "password";

    public static void main(String[] args) throws InterruptedException, SQLException {
        ExecutorService executorService = Executors.newFixedThreadPool(120);
        ConnectionPool connectionPool = new ConnectionPool(DB_URL,USER, PASS, 25);
        prepareTable();

        long startTime = System.currentTimeMillis();
        for (int userId = 1; userId <= 120; userId++) {
            final int id = userId;
            executorService.execute(() -> {
                try {
                    bookSeat(id, connectionPool);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }



        executorService.shutdown();

        executorService.awaitTermination(1, TimeUnit.MINUTES);
        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        printBookedSeats(connectionPool);
        System.out.println("Time taken: " + timeElapsed);

    }

    private static void bookSeat(int userId, ConnectionPool connectionPool) throws SQLException, InterruptedException {
        Connection conn = connectionPool.getConnection();
        PreparedStatement selectStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;

        try {
            // Establish connection
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            conn.setAutoCommit(false); // Begin transaction

            // Find a seat where userId is null
            String selectSQL = "SELECT seatId FROM seats WHERE userId IS NULL AND tripId = 1 ORDER BY seatId LIMIT 1 FOR UPDATE SKIP LOCKED";
            selectStmt = conn.prepareStatement(selectSQL);
            rs = selectStmt.executeQuery();

            if (rs.next()) {
                int seatId = rs.getInt("seatId");

                // Book the seat by updating the userId
                String updateSQL = "UPDATE seats SET userId = ? WHERE seatId = ?";
                updateStmt = conn.prepareStatement(updateSQL);
                updateStmt.setInt(1, userId);
                updateStmt.setInt(2, seatId);
                updateStmt.executeUpdate();

                System.out.println("User " + userId + " successfully booked Seat " + seatId);
            } else {
                System.out.println("No available seats for User " + userId);
            }

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback transaction in case of error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            try {
                if (rs != null) rs.close();
                if (selectStmt != null) selectStmt.close();
                if (updateStmt != null) updateStmt.close();
                if (conn != null) connectionPool.releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void printBookedSeats(ConnectionPool connectionPool) {
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

    private static void prepareTable() {
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
}

