package com.github.omerfarukicen.cassandraui.ui.window.editor.main.table;

import com.datastax.driver.core.TableMetadata;
import com.github.omerfarukicen.cassandraui.ui.DataObject;
import com.github.omerfarukicen.cassandraui.ui.eventhandler.TableClickEventHandler;
import com.github.omerfarukicen.cassandraui.ui.keylistener.TableCellCopyHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;

import java.util.List;

public class DataTableView extends TableView<DataObject> {
    public DataTableView() {
        setEditable(true);
        setPrefSize(200, 200);
        GridPane.setHalignment(this, HPos.CENTER);
        GridPane.setValignment(this, VPos.CENTER);
        GridPane.setRowIndex(this, 1);
        setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        TableViewSelectionModel<DataObject> selectionModel = getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.setCellSelectionEnabled(true);
    }

    public void setDataColumns(List<TableColumn<DataObject, Object>> columns) {
        getColumns().clear();
        getItems().clear();
        setEditable(true);
        getColumns().addAll(columns);
    }

    public void onTableSelected(TableMetadata tableMetadata) {
        setOnMouseClicked(new TableClickEventHandler<>(this));
    }

    public TableCellCopyHandler buildCopyHandler() {
        return new TableCellCopyHandler(this);
    }
}
