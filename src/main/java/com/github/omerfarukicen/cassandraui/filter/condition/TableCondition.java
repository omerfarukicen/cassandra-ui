package com.github.omerfarukicen.cassandraui.filter.condition;

import java.util.Optional;
import java.util.Set;

import static com.github.omerfarukicen.cassandraui.filter.Combiner.AND;
import static com.github.omerfarukicen.cassandraui.filter.Combiner.OR;
import static com.github.omerfarukicen.cassandraui.util.StringUtil.lastWord;
import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;

public class TableCondition implements StateCondition {
    @Override
    public boolean isCurrentState(String[] words, Set<String> columnNames) {
        Optional<String> lastWord = lastWord(words);
        return !lastWord.isPresent() || equalsAnyIgnoreCase(lastWord.get(), AND.name(), OR.name());
    }

    @Override
    public State name() {
        return State.TABLE;
    }
}
