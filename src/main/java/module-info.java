module org.example.projectclient {

    requires javafx.fxml;
    requires javafx.swing;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires freetts;
    requires java.sql;
    requires java.mail;
    requires com.google.gson;
//    requires javafx.web;
    requires jdk.jsobject;
    requires javafx.media;
    requires org.bytedeco.javacv;
    requires org.bytedeco.opencv;
    requires org.bytedeco.ffmpeg;
    requires java.desktop;
    requires vosk;
    requires org.json;


    opens loginFlashcard to javafx.fxml;
    exports loginFlashcard;

    opens FlashCard to javafx.fxml;
    exports FlashCard;

    opens Review to javafx.fxml;
    exports Review;

    opens Write to javafx.fxml;
    exports Write;

    opens Practice to javafx.fxml;
    exports Practice;

    opens statistical to javafx.fxml;
    exports statistical;

    opens Chat to javafx.fxml;
    exports Chat;

    opens Read to javafx.fxml;
    exports Read;
}
