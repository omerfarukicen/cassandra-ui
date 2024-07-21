package com.github.omerfarukicen.cassandraui.ui.window.editor.tables;

import com.github.omerfarukicen.cassandraui.util.UIUtil;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.AnchorPane;

import java.util.Optional;

class TableListView extends AnchorPane {
    private final ListView<String> tablesList;

    TableListView() {
        setPrefHeight(100);
        setPrefWidth(160);
        tablesList = list();
        getChildren().add(tablesList);
        UIUtil.fillParent(tablesList);
    }

    void onMouseClick(Runnable onSelection, Runnable onDeselection, Runnable onDoubleClick) {
        tablesList.setOnMouseClicked(event -> {
            int clickCount = event.getClickCount();
            Runnable wrappedSelectionAction = wrapSelectionAction(onSelection, onDoubleClick, clickCount);
            onClick(wrappedSelectionAction, onDeselection);
        });
    }

    private Runnable wrapSelectionAction(Runnable onSelection, Runnable onDoubleClick, int clickCount) {
        Runnable wrappedSelectionAction;
        if (clickCount == 2) {
            wrappedSelectionAction = () -> {
                onSelection.run();
                onDoubleClick.run();
            };
        } else {
            wrappedSelectionAction = onSelection;
        }
        return wrappedSelectionAction;
    }

    Optional<String> getSelectedTable() {
        return tablesList.getSelectionModel().getSelectedItems().stream().findFirst();
    }

    void showTables(ObservableList<String> tables) {
        tablesList.setItems(tables);
    }

    void clear() {
        tablesList.setItems(FXCollections.emptyObservableList());
    }

    void onNewValueSelected(ChangeListener<? super String> listener) {
        tablesList.getSelectionModel().selectedItemProperty().addListener(listener);
    }

    private ListView<String> list() {
        ListView<String> tables = new ListView<>(FXCollections.observableArrayList());
        tables.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tables.setPrefHeight(480);
        tables.setPrefWidth(256);
        return tables;
    }

    private void onClick(Runnable onSelection, Runnable onDeselection) {
        ObservableList<String> selectedItems = tablesList.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            onDeselection.run();
        } else {
            onSelection.run();
        }
    }
}
