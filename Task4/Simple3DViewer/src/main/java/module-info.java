module com.cgvsu {
    requires javafx.controls;
    requires javafx.fxml;
    requires vecmath;
    requires java.desktop;
    requires java.logging;
    requires org.slf4j;
    requires javafx.graphics;
    requires javafx.base;


    opens com.cgvsu to javafx.fxml;
    exports com.cgvsu;
}