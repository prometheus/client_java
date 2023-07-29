package io.prometheus.client.it.common;

import org.testcontainers.containers.output.OutputFrame;

import java.util.function.Consumer;

/**
 * Print Docker logs from TestContainers to stdout or stderr.
 */
public class LogConsumer implements Consumer<OutputFrame> {

    private final String prefix;

    private LogConsumer(String prefix) {
        this.prefix = prefix;
    }

    public static LogConsumer withPrefix(String prefix) {
     return new LogConsumer(prefix);
    }

    @Override
    public void accept(OutputFrame outputFrame) {
        switch (outputFrame.getType()) {
            case STDOUT:
                System.out.print(prefix + " - " + outputFrame.getUtf8String());
                break;
            case END:
                System.out.println(prefix + " - END");
                break;
            default: // STDERR or unexpected
                System.err.print(prefix + " - " + outputFrame.getUtf8String());
                break;
        }
    }
}
