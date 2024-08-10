package org.system.design.airlineseats.withoutlocks;

import org.system.design.commons.ConnectionPool;

import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.system.design.airlineseats.commons.QueriesUtil.prepareTable;
import static org.system.design.airlineseats.commons.QueriesUtil.printBookedSeats;

public class Main {

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASS = "password";

    public static void main(String[] args) throws InterruptedException, SQLException {
        ExecutorService executorService = Executors.newFixedThreadPool(120);
        ConnectionPool connectionPool = new ConnectionPool(DB_URL,USER, PASS, 25);
        prepareTable();

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
        printBookedSeats(connectionPool);
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
            String selectSQL = "SELECT seatId FROM seats WHERE userId IS NULL AND tripId = 1 ORDER BY seatId LIMIT 1";
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
}

