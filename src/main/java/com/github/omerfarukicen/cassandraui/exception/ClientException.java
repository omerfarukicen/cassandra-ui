package com.github.omerfarukicen.cassandraui.exception;

public abstract class ClientException extends RuntimeException {
    public ClientException(String message) {
        super(message);
    }
}
