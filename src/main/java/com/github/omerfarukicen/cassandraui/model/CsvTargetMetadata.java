package com.github.omerfarukicen.cassandraui.model;

import lombok.Value;
import org.apache.commons.csv.CSVFormat;

import java.io.File;

@Value
public class CsvTargetMetadata {
    private String table;
    private CSVFormat format;
    private File target;
}
