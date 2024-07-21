package com.github.omerfarukicen.cassandraui.ui;

import com.datastax.driver.core.AbstractTableMetadata;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.TableMetadata;
import com.github.omerfarukicen.cassandraui.i18n.MessageByLocaleService;
import com.github.omerfarukicen.cassandraui.service.BackgroundTaskExecutor;
import com.github.omerfarukicen.cassandraui.service.CassandraClientAdapter;
import com.github.omerfarukicen.cassandraui.service.TableContext;
import com.github.omerfarukicen.cassandraui.service.tasks.CsvWriteBackgroundTask;
import com.github.omerfarukicen.cassandraui.ui.listener.TextFieldButtonWatcher;
import com.github.omerfarukicen.cassandraui.ui.widget.DataExportWidget;
import com.github.omerfarukicen.cassandraui.ui.window.editor.main.BackgroundTaskMonitor;
import com.github.omerfarukicen.cassandraui.ui.window.editor.main.EventLogger;
import com.github.omerfarukicen.cassandraui.ui.window.editor.main.TableDataGridPane;
import com.github.omerfarukicen.cassandraui.ui.window.editor.main.table.DdlTextArea;
import com.github.omerfarukicen.cassandraui.ui.window.editor.tables.TablePanel;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;

import static com.github.omerfarukicen.cassandraui.service.TableContext.customView;
import static com.github.omerfarukicen.cassandraui.service.TableContext.fullTable;
import static com.github.omerfarukicen.cassandraui.util.CqlUtil.getSelectTable;
import static com.github.omerfarukicen.cassandraui.util.CqlUtil.isSelect;
import static com.github.omerfarukicen.cassandraui.util.EvenMoreFutures.handleErrorIfPresent;
import static com.github.omerfarukicen.cassandraui.util.StreamUtils.toMap;
import static com.github.omerfarukicen.cassandraui.util.UIUtil.disable;
import static com.github.omerfarukicen.cassandraui.util.UIUtil.enable;
import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.input.KeyCode.C;
import static javafx.scene.input.KeyCode.SPACE;
import static javafx.scene.input.KeyCombination.CONTROL_ANY;

@Slf4j
public class MainController {

    private final int pageSize = 1000;
    @Autowired
    private BeanFactory beanFactory;
    @Autowired
    private MessageByLocaleService localeService;
    @Autowired
    private CassandraClientAdapter clientAdapter;
    @Autowired
    private BackgroundTaskExecutor backgroundTaskExecutor;
    @Autowired
    private View view;
    @Autowired
    private TablePanel tablePanel;
    @Autowired
    private EventLogger eventLogger;
    @Autowired
    private BackgroundTaskMonitor backgroundTaskMonitor;

    private Map<String, TableMetadata> tableMetadata;
    private DdlTextArea ddlTextArea;

    @FXML
    private MenuBar menu;
    @FXML
    private SplitPane mainWindow;
    @FXML
    private Button runButton;
    @FXML
    private TextField queryTextField;
    @FXML
    private AnchorPane editorAnchor;

    @Autowired
    private TableDataGridPane tableDataGridPane;
    @FXML
    private AnchorPane eventAnchor;

    @PostConstruct
    public void init() {
        Menu fileMenu = (Menu) beanFactory.getBean("fileMenu");
        Menu helpMenu = (Menu) beanFactory.getBean("helpMenu");

        mainWindow.getItems().add(0, tablePanel);

        menu.getMenus().addAll(fileMenu, helpMenu);

        queryTextField.textProperty().addListener(TextFieldButtonWatcher.wrap(runButton));
        queryTextField.setPromptText(localeService.getMessage("ui.editor.query.textbox.tooltip"));
        runButton.setTooltip(new Tooltip(localeService.getMessage("ui.editor.query.button.tooltip")));

        runButton.setOnAction(
                event -> {
                    String cqlQuery = queryTextField.getText();
                    if (isSelect(cqlQuery)) {
                        Optional<String> tableOptional = getSelectTable(cqlQuery);
                        tableOptional.ifPresent(table ->
                                showDataRows(customView(table, cqlQuery, tableMetadata.get(table), clientAdapter, pageSize)));
                    } else {
                        clientAdapter.execute(cqlQuery).whenComplete(handleErrorIfPresent(this::printError));
                    }
                }
        );
        tablePanel.setNewValueListener(table -> tableDataGridPane.updateTableMetadata(tableMetadata.get(table)));

        ddlTextArea = new DdlTextArea();
        editorAnchor.getChildren().add(ddlTextArea);
        editorAnchor.getChildren().add(tableDataGridPane);

        HBox eventBox = new HBox(eventLogger, backgroundTaskMonitor);
        eventBox.setAlignment(Pos.BASELINE_CENTER);
        eventAnchor.getChildren().add(eventBox);
        eventLogger.prefWidthProperty().bind(eventAnchor.widthProperty().multiply(0.9));
        disable(queryTextField, runButton, tablePanel);
    }

    public void onWindowLoad() {
        getAccelerators().put(new KeyCodeCombination(C, CONTROL_ANY), tableDataGridPane.buildCopyHandler());
        getAccelerators().put(new KeyCodeCombination(SPACE, CONTROL_ANY), tableDataGridPane::suggestCompletion);
    }

    public void showDDLForTable(String tableName) {
        tableDataGridPane.setVisible(false);
        ddlTextArea.showTableDDL(tableMetadata.get(tableName));
    }

    public void showDataForTable(String tableName) {
        eventLogger.fireLogEvent("Loading data for table '{}'", tableName);
        showDataRows(fullTable(tableName, tableMetadata.get(tableName), clientAdapter, pageSize));
    }

    public void exportDataForTable(String tableName) {
        eventLogger.fireLogEvent("Exporting data for table '{}'", tableName);
        DataExportWidget dataExporter = (DataExportWidget) beanFactory.getBean("dataExporter", tableName);
        dataExporter.getMetadata()
                .map(metadata -> new CsvWriteBackgroundTask(metadata, tableMetadata.get(tableName), clientAdapter))
                .map(backgroundTaskExecutor::submit)
                .map(uuid -> Mono.just(uuid)
                        .doOnNext(backgroundTaskMonitor::addTask)
                        .doOnError(throwable -> backgroundTaskMonitor.remove(uuid))
                        .doOnError(throwable -> log.error("Task {} failed", uuid, throwable)))
                .doOnSuccess(uuidMono -> eventLogger.fireLogEvent("Exported data for table '{}'", tableName))
                .subscribe();
    }

    public void loadTables(ConnectionData connection) {
        tableDataGridPane.setVisible(false);
        ddlTextArea.setVisible(false);

        eventLogger.clear();
        eventLogger.fireLogEvent("Connecting to {}/{} ...", connection.getUrl(), connection.getKeyspace());
        tablePanel.clear();
        disable(tablePanel, queryTextField, runButton);

        clientAdapter.connect(connection)
                .thenApply(CassandraAdminTemplate::getKeyspaceMetadata)
                .thenApply(KeyspaceMetadata::getTables)
                .thenApply(tables -> toMap(tables, AbstractTableMetadata::getName))
                .whenComplete(handleErrorIfPresent(this::printError))
                .whenComplete((metadata, error) -> {
                    if (metadata != null) {
                        tableMetadata = metadata;
                        showTableNames(connection.getUrl(), connection.getKeyspace());
                    }
                });
    }

    private ObservableMap<KeyCombination, Runnable> getAccelerators() {
        return view.getPrimaryStage().getScene().getAccelerators();
    }

    private void showDataRows(TableContext context) {
        runLater(() -> {
            ddlTextArea.setVisible(false);
            tableDataGridPane.setDataColumns(context.getColumns());

            context.previousPage()
                    .whenComplete(handleErrorIfPresent(this::printError))
                    .whenComplete((data, throwable) -> {
                        if (data != null) {
                            tableDataGridPane.setData(context, data);
                        }
                    });
        });
    }

    private void showTableNames(String url, String keyspace) {
        runLater(() -> {
            tablePanel.showTables(observableArrayList(tableMetadata.keySet()).sorted());
            eventLogger.printServerName(url, keyspace);
            eventLogger.fireLogEvent("Loaded tables from {}/{}", url, keyspace);
            enable(queryTextField, tablePanel);
        });
    }


    private void printError(Throwable throwable) {
        runLater(() -> {
            tableDataGridPane.setVisible(false);
            ddlTextArea.showException(throwable);
        });
    }
}
