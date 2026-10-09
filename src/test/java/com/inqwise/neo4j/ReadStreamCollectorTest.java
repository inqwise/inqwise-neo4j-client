package com.inqwise.neo4j;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import reactor.core.publisher.Flux;

@Timeout(value = 5, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class ReadStreamCollectorTest {

    @Test
    void neverCompletingStreamFailsWithinDeadline() {
        AssertionError failure = assertThrows(AssertionError.class,
            () -> ReadStreamCollector.collect(new PublisherReadStream<>(Flux::never)));
        assertTrue(failure.getMessage().contains("within 2 seconds"));
    }

    @Test
    void publisherFailurePreservesOriginalCause() {
        RuntimeException cause = new IllegalStateException("Query failed");
        AssertionError failure = assertThrows(AssertionError.class,
            () -> ReadStreamCollector.collect(new PublisherReadStream<>(() -> Flux.error(cause))));
        assertSame(cause, failure.getCause());
    }
}
