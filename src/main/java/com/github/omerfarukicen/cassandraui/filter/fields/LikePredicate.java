package com.github.omerfarukicen.cassandraui.filter.fields;

import com.github.omerfarukicen.cassandraui.ui.DataObject;

import java.util.regex.Pattern;

import static java.util.Objects.nonNull;

public class LikePredicate extends RowPredicate {
    public LikePredicate(String field, String value) {
        super(field, value);
    }

    @Override
    public boolean test(DataObject data) {
        String value = getValue();
        Pattern pattern = Pattern.compile(value);
        Object actualValue = data.get(getField());
        //noinspection unchecked
        return nonNull(actualValue)
                && pattern.matcher(actualValue.toString()).find();
    }
}
