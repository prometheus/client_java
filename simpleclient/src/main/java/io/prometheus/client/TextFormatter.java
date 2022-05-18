package io.prometheus.client;

import java.io.Writer;

public abstract class TextFormatter {

    private final Writer writer;

    public TextFormatter(Writer writer) {
        if (null == writer) {
            throw new IllegalArgumentException();
        }

        this.writer = writer;
    }

    abstract void format(SimpleCollector<?> collector);

}
