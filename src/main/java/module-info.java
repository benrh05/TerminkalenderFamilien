module org.example.testmal {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.testmal to javafx.fxml;
    exports org.example.testmal;
}