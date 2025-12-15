module Cake.Shop.System {
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;

    requires java.sql;
    requires org.kordamp.ikonli.javafx;
    requires io.github.cdimascio.dotenv.java;

    opens com.cakeshopsystem to javafx.fxml;
    opens com.cakeshopsystem.controllers to javafx.fxml;

    exports com.cakeshopsystem;
    exports com.cakeshopsystem.controllers;
}