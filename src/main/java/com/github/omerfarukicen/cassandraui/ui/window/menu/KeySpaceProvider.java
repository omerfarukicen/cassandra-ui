package com.github.omerfarukicen.cassandraui.ui.window.menu;

import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface KeySpaceProvider {
    CompletableFuture<List<String>> loadKeyspaces(String url, @Nullable String username, @Nullable String password);
}
