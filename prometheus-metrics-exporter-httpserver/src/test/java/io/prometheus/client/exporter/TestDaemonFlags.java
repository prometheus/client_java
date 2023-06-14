package io.prometheus.client.exporter;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class TestDaemonFlags {

    @Test
    public void testDefaultIsNotDaemon() throws IOException, ExecutionException, InterruptedException {
        assertThat(usesDaemonExecutor(new HTTPServer(0))).isFalse();
    }

    @Test
    public void testDaemon() throws IOException, ExecutionException, InterruptedException {
        assertThat(usesDaemonExecutor(new HTTPServer(0, true))).isTrue();
    }

    @Test
    public void testNonDaemon() throws IOException, ExecutionException, InterruptedException {
        assertThat(usesDaemonExecutor(new HTTPServer(0, false))).isFalse();
    }

    private boolean usesDaemonExecutor(HTTPServer httpServer) throws IOException, InterruptedException, ExecutionException {
        try {
            FutureTask<Boolean> task = new FutureTask<Boolean>(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return Thread.currentThread().isDaemon();
                }
            });
            httpServer.server.getExecutor().execute(task);
            return task.get();
        } finally {
            httpServer.stop();
        }
    }
}
