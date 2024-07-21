package com.github.omerfarukicen.cassandraui.ui.window.menu.about;

import com.github.omerfarukicen.cassandraui.i18n.MessageByLocaleService;
import com.github.omerfarukicen.cassandraui.properties.UIProperties;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AboutBox extends Stage {
    private final MessageByLocaleService localeService;
    private final UIProperties properties;

    public AboutBox(Stage parent, MessageByLocaleService localeService, UIProperties properties) {
        this.localeService = localeService;
        this.properties = properties;
        initModality(Modality.APPLICATION_MODAL);
        initOwner(parent);
        getIcons().add(new Image("cassandra_ico.png"));
        setTitle(localeService.getMessage("ui.menu.help.about.title"));
        setScene(buildScene());
        show();
    }

    private Scene buildScene() {
        VBox aboutBox = new VBox(properties.getAboutBoxSpacing());
        Scene dialogScene = new Scene(aboutBox, properties.getAboutBoxWidth(), properties.getAboutBoxHeight());
        Text text = new Text(localeService.getMessage("ui.menu.help.about.text"));
        text.setTextAlignment(TextAlignment.CENTER);
        text.setWrappingWidth(properties.getAboutBoxWidth());
        aboutBox.getChildren().add(text);
        return dialogScene;
    }
}
