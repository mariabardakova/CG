module com.cgvsu {
    requires javafx.controls;
    requires javafx.fxml;
    requires vecmath;
    requires java.desktop;
    requires javafx.graphics;
    requires javafx.base;
    requires java.logging;
    requires org.slf4j;

    opens com.cgvsu to javafx.fxml;
    exports com.cgvsu;
}