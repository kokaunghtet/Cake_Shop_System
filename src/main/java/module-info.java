module Cake.Shop.System {
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;

    requires java.sql;
    requires io.github.cdimascio.dotenv.java;
    requires mysql.connector.j;

    opens com.cakeshopsystem to javafx.fxml;
    opens com.cakeshopsystem.controllers to javafx.fxml;

    exports com.cakeshopsystem;
    exports com.cakeshopsystem.controllers;
}