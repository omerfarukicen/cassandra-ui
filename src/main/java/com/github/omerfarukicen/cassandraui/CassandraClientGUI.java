package com.github.omerfarukicen.cassandraui;

import com.github.omerfarukicen.cassandraui.ui.*;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraReactiveDataAutoConfiguration;
import org.springframework.context.annotation.Lazy;

@Lazy
@SpringBootApplication(exclude = {CassandraAutoConfiguration.class, CassandraDataAutoConfiguration.class,
        CassandraReactiveDataAutoConfiguration.class})
public class CassandraClientGUI extends com.github.omerfarukicen.cassandraui.ui.AbstractJavaFxApplicationSupport {

    @Autowired
    private View view;

    public static void main(String[] args) {
        launchApp(CassandraClientGUI.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        view.setPrimaryStage(primaryStage);

        primaryStage.setTitle("Cassandra client");
        primaryStage.getIcons().add(new Image("cassandra_ico.png"));
        primaryStage.setScene(new Scene(view.getView()));
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
        primaryStage.show();

        ((MainController) view.getController()).onWindowLoad();
    }
}
