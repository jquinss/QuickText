module com.example.quicktext {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.jsoup;
    requires com.google.gson;


    opens com.jquinss.quicktext.app to javafx.fxml;
    opens com.jquinss.quicktext.controllers to javafx.fxml;
    opens com.jquinss.quicktext.data to com.google.gson;

    exports com.jquinss.quicktext.app;
    exports com.jquinss.quicktext.controllers;
    exports com.jquinss.quicktext.control;
}