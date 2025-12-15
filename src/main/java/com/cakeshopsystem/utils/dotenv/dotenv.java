package com.cakeshopsystem.utils.dotenv;

import io.github.cdimascio.dotenv.Dotenv;

public class dotenv {
    static Dotenv dotenv = Dotenv.load();

    public static String mysql_username = dotenv.get("MYSQL_USERNAME");
    public static String mysql_password = dotenv.get("MYSQL_PASSWORD");
    public static String mysql_url = dotenv.get("MYSQL_URL");
}