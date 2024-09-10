module com.example.dateiumbenennung {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;


    opens com.example.dateiumbenennung to javafx.fxml;
    exports com.example.dateiumbenennung;
}