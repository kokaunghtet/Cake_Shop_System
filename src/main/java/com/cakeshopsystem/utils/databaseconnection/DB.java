package com.cakeshopsystem.utils.databaseconnection;

import com.cakeshopsystem.utils.dotenv.dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {

    private static final String USERNAME = dotenv.mysql_username;
    private static final String PASSWORD = dotenv.mysql_password;
    private static final String URL = dotenv.mysql_url;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // load once
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL Driver not found", e);
        }
    }

    private DB() {}

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static void disconnect(Connection con) {
        if (con == null) return;
        try {
            if (!con.isClosed()) con.close();
        } catch (SQLException ignored) {}
    }
}
