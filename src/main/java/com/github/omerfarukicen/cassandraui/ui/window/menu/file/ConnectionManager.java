package com.github.omerfarukicen.cassandraui.ui.window.menu.file;

import com.datastax.driver.core.TypeCodec;
import lombok.NonNull;
import com.github.omerfarukicen.cassandraui.i18n.MessageByLocaleService;
import com.github.omerfarukicen.cassandraui.properties.StorageProperties;
import com.github.omerfarukicen.cassandraui.properties.UIProperties;
import com.github.omerfarukicen.cassandraui.ui.ConnectionData;
import com.github.omerfarukicen.cassandraui.ui.MainController;
import com.github.omerfarukicen.cassandraui.ui.fx.CellFactory;
import com.github.omerfarukicen.cassandraui.ui.window.menu.ConnectionDataHandler;
import com.github.omerfarukicen.cassandraui.ui.window.menu.KeySpaceProvider;
import com.google.common.io.Files;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.omerfarukicen.cassandraui.util.JsonUtil.fromJson;
import static com.github.omerfarukicen.cassandraui.util.JsonUtil.toJson;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class ConnectionManager extends Stage implements BeanFactoryAware {
    private final MessageByLocaleService localeService;
    private final KeySpaceProvider keySpaceProvider;
    private final UIProperties uiProperties;
    private final StorageProperties storageProperties;
    private final TableView<ConnectionData> table;
    private final File configurationsFile;
    private BeanFactory beanFactory;

    @SneakyThrows
    public ConnectionManager(Stage parent, MessageByLocaleService localeService, KeySpaceProvider keySpaceProvider,
            UIProperties uiProperties,
            StorageProperties storageProperties) {
        this.localeService = localeService;
        this.keySpaceProvider = keySpaceProvider;
        this.uiProperties = uiProperties;
        this.storageProperties = storageProperties;

        initModality(Modality.APPLICATION_MODAL);
        initOwner(parent);
        setTitle(localeService.getMessage("ui.menu.file.manager.title"));
        getIcons().add(new Image("cassandra_ico.png"));
        Integer width = uiProperties.getConnectionManagerWidth();
        Integer height = uiProperties.getConnectionManagerHeight();
        Integer rightWidth = uiProperties.getConnectionManagerRightPaneWidth();
        int leftWidth = width - rightWidth;

        VBox connectionBox = new VBox(uiProperties.getNewConnectSpacing());
        connectionBox.setAlignment(Pos.CENTER);
        connectionBox.setMinWidth(leftWidth);
        connectionBox.setMaxWidth(leftWidth);

        table = buildConnectionDataTable(leftWidth);
        configurationsFile = getConfigurationsFile();
        if (!configurationsFile.exists()) {
            configurationsFile.createNewFile();
        }
        String json = Files.toString(configurationsFile, UTF_8);
        if (!StringUtils.isBlank(json)) {
            List<ConnectionData> connections = fromJson(json, ConnectionData.class);
            table.getItems().addAll(connections);
        }
        connectionBox.getChildren().add(table);

        VBox buttons = new VBox();
        buttons.setMinWidth(rightWidth);
        buttons.setMaxWidth(rightWidth);
        buttons.setSpacing(uiProperties.getConnectionManagerButtonsSpacing());
        buttons.setAlignment(Pos.CENTER);

        Button newConnection = getButton("+");
        newConnection.setOnAction(event -> onNewConnection());
        Button removeConnection = getButton("-");
        removeConnection.setOnAction(event -> onConnectionRemove());
        Button editConnection = getButton("✎");
        editConnection.setOnAction(event -> onConnectionEdit());
        Button useConnection = getButton("✓");
        useConnection.setOnAction(event -> onConnectionUsage());

        buttons.getChildren().add(newConnection);
        buttons.getChildren().add(removeConnection);
        buttons.getChildren().add(editConnection);
        buttons.getChildren().add(useConnection);

        SplitPane splitPane = new SplitPane(connectionBox, buttons);
        Scene content = new Scene(splitPane, width, height);

        setScene(content);
        show();
    }

    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @SneakyThrows
    private File getConfigurationsFile() {
        return new File(storageProperties.getLocation(), storageProperties.getPropertiesFile());
    }

    private Button getButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(uiProperties.getConnectionManagerButtonWidth());
        button.setMinWidth(uiProperties.getConnectionManagerButtonWidth());
        return button;
    }

    private TableView<ConnectionData> buildConnectionDataTable(Integer leftWidth) {
        TableView<ConnectionData> dataTableView = new TableView<>();
        dataTableView.setMinWidth(leftWidth);
        dataTableView.setMaxWidth(leftWidth);
        dataTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        dataTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        dataTableView.getSelectionModel().setCellSelectionEnabled(false);
        dataTableView.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() > 1) {
                onConnectionUsage();
            }
        });

        int colWidth = leftWidth / 4;

        String urlTitle = localeService.getMessage("ui.menu.file.manager.table.url");
        TableColumn<ConnectionData, String> url = buildColumn(urlTitle, colWidth, ConnectionData::getUrl);

        String spaceTitle = localeService.getMessage("ui.menu.file.manager.table.keyspace");
        TableColumn<ConnectionData, String> keyspace = buildColumn(spaceTitle, colWidth, ConnectionData::getKeyspace);

        String userTitle = localeService.getMessage("ui.menu.file.manager.table.username");
        TableColumn<ConnectionData, String> username = buildColumn(userTitle, colWidth, ConnectionData::getUsername);

        String passTitle = localeService.getMessage("ui.menu.file.manager.table.password");
        TableColumn<ConnectionData, String> password = buildColumn(passTitle, colWidth, ConnectionData::getPassword);

        dataTableView.getColumns().add(url);
        dataTableView.getColumns().add(keyspace);
        dataTableView.getColumns().add(username);
        dataTableView.getColumns().add(password);
        dataTableView.setItems(FXCollections.observableArrayList());

        return dataTableView;
    }

    private TableColumn<ConnectionData, String> buildColumn(String name, int columnWidth, Function<ConnectionData,
            String> extractor) {
        TableColumn<ConnectionData, String> column = new TableColumn<>();
        Label columnLabel = new Label(name);
        column.setCellFactory(CellFactory.create(TypeCodec.varchar()));

        column.setCellValueFactory(param -> {
            String field = extractor.apply(param.getValue());
            return new SimpleObjectProperty<>(field);
        });

        column.setOnEditCommit(event -> log.info("edit"));
        column.setGraphic(columnLabel);
        column.setMinWidth(columnWidth);
        return column;
    }

    private void onNewConnection() {
        beanFactory.getBean("newConnectionBox", this, (ConnectionDataHandler) this::onConnectionData,
                keySpaceProvider);
    }

    private void onConnectionData(ConnectionData data) {
        table.getItems().add(data);
        flushConfigFile(configurationsFile);
    }

    private void onConnectionEdit() {
        TableView.TableViewSelectionModel<ConnectionData> selectionModel = table.getSelectionModel();
        if (!selectionModel.isEmpty()) {
            int focusedIndex = selectionModel.getFocusedIndex();
            ConnectionData data = table.getItems().get(focusedIndex);
            NewConnectionBox connectionBox = (NewConnectionBox) beanFactory.getBean("newConnectionBox",
                    this, (ConnectionDataHandler) this::onConnectionEditData, keySpaceProvider);
            connectionBox.update(data);
        }
    }

    private void onConnectionEditData(ConnectionData newData) {
        TableView.TableViewSelectionModel<ConnectionData> selectionModel = table.getSelectionModel();
        if (!selectionModel.isEmpty()) {
            int focusedIndex = selectionModel.getFocusedIndex();
            ConnectionData data = table.getItems().get(focusedIndex);
            data.setKeyspace(newData.getKeyspace());
            data.setPassword(newData.getPassword());
            data.setUrl(newData.getUrl());
            data.setUsername(newData.getUsername());
            // hack: better way to refresh the display??
            table.getColumns().get(0).setVisible(false);
            table.getColumns().get(0).setVisible(true);
        }
        flushConfigFile(configurationsFile);
    }

    private void onConnectionUsage() {
        doWithSelected(connectionData -> {
            MainController controller = beanFactory.getBean(MainController.class);
            controller.loadTables(connectionData);
            ConnectionManager.this.close();
        });
    }

    private void onConnectionRemove() {
        doWithSelected(connectionData -> {
            table.getItems().remove(connectionData);
            flushConfigFile(configurationsFile);
        });
    }

    private void doWithSelected(Consumer<ConnectionData> callback) {
        TableView.TableViewSelectionModel<ConnectionData> selectionModel = table.getSelectionModel();

        if (!selectionModel.isEmpty()) {
            int focusedIndex = selectionModel.getFocusedIndex();
            ConnectionData connectionData = table.getItems().get(focusedIndex);
            callback.accept(connectionData);
        }
    }

    @SneakyThrows
    private void flushConfigFile(File configurationsFile) {
        Files.write(toJson(table.getItems()), configurationsFile, UTF_8);
    }
}
