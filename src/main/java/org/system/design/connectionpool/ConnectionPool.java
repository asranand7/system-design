package org.system.design.connectionpool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.system.design.connectionpool.ConnectionUtils.createNewConnection;

public class ConnectionPool {
    private final BlockingQueue<Connection> connectionPool;

    public ConnectionPool(String url, String user, String password, int initialPoolSize) throws SQLException {
        this.connectionPool = new ArrayBlockingQueue<>(initialPoolSize);
        for (int i = 0; i < initialPoolSize; i++) {
            connectionPool.offer(createNewConnection(url, user, password));
        }
    }

    public Connection getConnection() throws InterruptedException, SQLException {
        return connectionPool.take();
    }

    public void releaseConnection(Connection connection) {
        connectionPool.offer(connection);
    }

    public int getAvailableConnectionsCount() {
        return connectionPool.size();
    }
}