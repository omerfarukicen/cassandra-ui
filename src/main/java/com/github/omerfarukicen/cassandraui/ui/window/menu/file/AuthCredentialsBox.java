package com.github.omerfarukicen.cassandraui.ui.window.menu.file;

import com.github.omerfarukicen.cassandraui.i18n.MessageByLocaleService;
import com.github.omerfarukicen.cassandraui.properties.UIProperties;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class AuthCredentialsBox extends VBox {
    private final TextField username;
    private final TextField password;
    private final MessageByLocaleService localeService;

    AuthCredentialsBox(MessageByLocaleService localeService, UIProperties uiProperties) {
        this.localeService = localeService;
        username = getUsernameField();
        password = getPasswordField();
        getChildren().add(username);
        getChildren().add(password);
        setHeight(uiProperties.getCredentialsBoxHeight());
        setSpacing(uiProperties.getCredentialsBoxSpacing());
        setVisible(false);
    }

    public String getUsername() {
        return username.getText();
    }

    public String getPassword() {
        return password.getText();
    }

    public void setUsername(String userId) {
        username.setText(userId);
    }

    public void setPassword(String passwd) {
        password.setText(passwd);
    }

    @Override
    protected void setWidth(double value) {
        super.setWidth(value);
        username.setMinWidth(value);
        username.setMaxWidth(value);
        username.setPrefWidth(value);
        password.setMinWidth(value);
        password.setMaxWidth(value);
        password.setPrefWidth(value);
    }

    private TextField getUsernameField() {
        TextField username = new TextField();
        username.setPromptText(localeService.getMessage("ui.menu.file.connect.auth.username"));
        username.setAlignment(Pos.CENTER);
        return username;
    }

    private TextField getPasswordField() {
        TextField username = new TextField();
        username.setPromptText(localeService.getMessage("ui.menu.file.connect.auth.password"));
        username.setAlignment(Pos.CENTER);
        return username;
    }
}
