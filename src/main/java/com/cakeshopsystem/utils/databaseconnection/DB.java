package com.cakeshopsystem.utils.databaseconnection;

import com.cakeshopsystem.utils.dotenv.dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {
    private static final String USERNAME = dotenv.mysql_username;
    private static final String PASSWORD = dotenv.mysql_password;
    private static final String URL = dotenv.mysql_url;

    static Connection con = null;

    public static Connection connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Error :"+e.getLocalizedMessage());
        }
        return con;
    }

    public static void disconnect(){
        try {
            if(con != null && !con.isClosed()){
                con.close();
            }
        } catch (SQLException e) {
            System.out.println("Error : "+e.getLocalizedMessage());
        }

    }
}