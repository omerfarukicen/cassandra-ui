package com.github.omerfarukicen.cassandraui.ui.window.editor.main.table;

import com.datastax.driver.core.TableMetadata;
import com.github.omerfarukicen.cassandraui.util.UIUtil;
import javafx.scene.control.TextArea;
import org.apache.commons.lang3.exception.ExceptionUtils;

import static com.github.omerfarukicen.cassandraui.util.CqlUtil.formatDDL;

public class DdlTextArea extends TextArea {
    public DdlTextArea() {
        setEditable(false);
        setPrefSize(200, 200);
        setVisible(false);
        UIUtil.fillParent(this);
    }

    public void showTableDDL(TableMetadata tableMetadata) {
        setText(formatDDL(tableMetadata.toString()));
        setVisible(true);
    }

    public void showException(Throwable throwable) {
        setText(ExceptionUtils.getStackTrace(throwable));
        setVisible(true);
    }
}
