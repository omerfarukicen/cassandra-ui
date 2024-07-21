package com.github.omerfarukicen.cassandraui.ui;

import javafx.scene.Parent;
import javafx.stage.Stage;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class View {
    private final Parent view;
    private final Object controller;
    private Stage primaryStage;
}
