package com.qaware.mcp.tools.knowledge;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class TikaToolTest {

    private AtomicReference<Throwable> exceptionRef = new AtomicReference<>();

    @Test
    void testIsThreadSafe() throws InterruptedException {
        int threadCount = 10;
        int iterationsPerThread = 50;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < iterationsPerThread; j++) {
                    if (j % 2 == 0) {
                        verify("corpus-test/sub/HelloWorld.pdf", -969099747);
                    } else {
                        verify("corpus-test/sub/Word.docx", -1747692632);
                    }
                }
            });
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS), "Executor did not terminate in time");

        assertNull(exceptionRef.get());
    }


    private void verify(String path, int expected) {
        try (InputStream inputStream =
                     Objects.requireNonNull(
                             TikaToolTest.class.getClassLoader().getResourceAsStream(path),
                             "Not found on classpath: " + path)) {

            assertEquals(expected, TikaTool.parse(inputStream).hashCode(), path);

        } catch (Throwable t) {
            exceptionRef.set(t);
        }
    }

}
