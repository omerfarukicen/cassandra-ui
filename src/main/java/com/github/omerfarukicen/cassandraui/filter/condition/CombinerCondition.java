package com.github.omerfarukicen.cassandraui.filter.condition;

import com.github.omerfarukicen.cassandraui.filter.Operator;

import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.stream;
import static com.github.omerfarukicen.cassandraui.util.StringUtil.wordAtPosition;

public class CombinerCondition implements StateCondition {
    @Override
    public boolean isCurrentState(String[] words, Set<String> columnNames) {
        Optional<String> secondFromHead = wordAtPosition(words, -2);
        return secondFromHead.isPresent()
                && stream(Operator.values()).anyMatch(operator -> operator.getValue().equals(secondFromHead.get()));
    }

    @Override
    public State name() {
        return State.COMBINER;
    }
}
