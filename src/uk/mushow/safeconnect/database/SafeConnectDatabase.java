package uk.mushow.safeconnect.database;

import uk.mushow.safeconnect.SafeConnectPlugin;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SafeConnectDatabase {

    private final String HOST;
    private final String PORT;
    private final String USERNAME;
    private final String PASSWORD;
    private final String DATABASE_NAME;
    private final String TABLE_NAME;

    public SafeConnectDatabase(final SafeConnectPlugin safeConnectPlugin) {
        this.HOST = safeConnectPlugin.getConfig("database.host");
        this.PORT = safeConnectPlugin.getConfig("database.port");
        this.USERNAME = safeConnectPlugin.getConfig("database.username");
        this.PASSWORD = safeConnectPlugin.getConfig("database.password");
        this.DATABASE_NAME = safeConnectPlugin.getConfig("database.name");
        this.TABLE_NAME = safeConnectPlugin.getConfig("database.table");
    }

    public Connection init() {
        final String url = "jdbc:mysql://"
                + this.HOST + ":"
                + this.PORT + "/";

        Connection connection = connect(url);
        if(connection != null) {
            createDatabase(connection);
            Connection databaseConnection = connect(url + this.DATABASE_NAME + "?characterEncoding=utf8");
            createTableIfNotExists(databaseConnection, this.TABLE_NAME);
            return databaseConnection;
        }
        return null;
    }

    private void createDatabase(Connection connection) {
        try {
            Statement stmt = connection.createStatement();
            String query = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, this.DATABASE_NAME);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                stmt.executeUpdate("CREATE DATABASE " + this.DATABASE_NAME);
                Logger.getLogger("SafeConnectDatabase").log(Level.INFO, "Database '" + this.DATABASE_NAME + "' created successfully.");
            }
        } catch (SQLException e) {
            Logger.getLogger("SafeConnectDatabase").log(Level.SEVERE, "Couldn't create the database", e);
        }
    }

    private Connection connect(String url) {
        try {
            return DriverManager.getConnection(url, this.USERNAME, this.PASSWORD);
        } catch (SQLException e) {
            Logger.getLogger("SafeConnectDatabase").log(Level.SEVERE, "Couldn't connect to the database");
            return null;
        }
    }

    public boolean isTableExists(Connection conn, String tableName) {
        try {
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null, null, tableName, null);
            return tables.next();
        } catch (SQLException e) {
            Logger.getLogger("SafeConnectDatabase").log(Level.SEVERE, "Couldn't find the table");
            return false;
        }
    }

    public void createTable(Connection conn, String tableName) {
        try {
            Statement stmt = conn.createStatement();
            String query = "CREATE TABLE " + tableName + " ("
                    + "id INT NOT NULL AUTO_INCREMENT,"
                    + "username VARCHAR(255) NOT NULL UNIQUE,"
                    + "password VARCHAR(255) NOT NULL,"
                    + "PRIMARY KEY (id)"
                    + ")";
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            Logger.getLogger("SafeConnectDatabase").log(Level.SEVERE, "Couldn't create the table");
        }
    }

    public void createTableIfNotExists(Connection conn, String tableName) {
        if (!isTableExists(conn, tableName)) {
            createTable(conn, tableName);
        }
    }

}
