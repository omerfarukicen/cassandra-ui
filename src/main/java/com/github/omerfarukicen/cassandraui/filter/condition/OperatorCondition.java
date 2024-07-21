package com.github.omerfarukicen.cassandraui.filter.condition;

import java.util.Optional;
import java.util.Set;

import static com.github.omerfarukicen.cassandraui.util.StringUtil.lastWord;

public class OperatorCondition implements StateCondition {
    @Override
    public boolean isCurrentState(String[] words, Set<String> columnNames) {
        Optional<String> lastWord = lastWord(words);
        return lastWord.isPresent() && columnNames.stream().anyMatch(name -> name.equalsIgnoreCase(lastWord.get()));
    }

    @Override
    public State name() {
        return State.OPERATOR;
    }
}
