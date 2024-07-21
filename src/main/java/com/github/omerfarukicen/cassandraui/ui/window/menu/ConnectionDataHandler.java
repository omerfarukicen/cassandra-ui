package com.github.omerfarukicen.cassandraui.ui.window.menu;

import com.github.omerfarukicen.cassandraui.ui.ConnectionData;

@FunctionalInterface
public interface ConnectionDataHandler {
    void onConnectionData(ConnectionData connectionData);
}
