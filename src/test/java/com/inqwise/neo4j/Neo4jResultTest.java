package com.inqwise.neo4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.neo4j.driver.Record;
import org.neo4j.driver.reactivestreams.ReactiveResult;
import org.neo4j.driver.summary.ResultSummary;
import org.reactivestreams.Publisher;

import io.vertx.ext.reactivestreams.ReactiveReadStream;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

@Timeout(value = 5, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class Neo4jResultTest {

	@Test
	void recordsExposesReactiveStream() {
		Record record = TestRecords.record("value", 10);
		var stub = ReactiveStubs.result(List.of("value"), List.of(record));
		var result = new Neo4jResult(stub);

		ReactiveReadStream<Record> stream = result.records();
		var records = ReadStreamCollector.collect(stream);
		assertEquals(1, records.size());
		assertEquals(10, records.get(0).get("value").asInt());
	}

    @Test
    void handlerStartsStreamWithoutResume() {
        Record record = TestRecords.record("value", 10);
        List<Record> records = new ArrayList<>();
        result(Flux.just(record)).records().handler(records::add);
        assertEquals(List.of(record), records);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void emptyStreamCompletesWithEitherHandlerOrder(boolean dataHandlerFirst) {
        ReactiveReadStream<Record> stream = result(Flux.empty()).records();
        AtomicInteger completions = new AtomicInteger();
        if (dataHandlerFirst) {
            stream.handler(record -> {});
        }
        stream.endHandler(ignored -> completions.incrementAndGet());
        if (!dataHandlerFirst) {
            stream.handler(record -> {});
        }
        stream.resume();
        stream.endHandler(ignored -> completions.incrementAndGet());
        assertEquals(1, completions.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void streamFailureReachesEitherHandlerOrder(boolean dataHandlerFirst) {
        RuntimeException failure = new IllegalStateException("Query failed");
        ReactiveReadStream<Record> stream = result(Flux.error(failure)).records();
        AtomicReference<Throwable> observed = new AtomicReference<>();
        if (dataHandlerFirst) {
            stream.handler(record -> {});
        }
        stream.exceptionHandler(observed::set);
        if (!dataHandlerFirst) {
            stream.handler(record -> {});
        }
        assertSame(failure, observed.get());
    }

    @Test
    void pausePreventsRecordsUntilResume() {
        Record record = TestRecords.record("value", 10);
        ReactiveReadStream<Record> stream = result(Flux.just(record)).records();
        List<Record> records = new ArrayList<>();
        stream.pause();
        stream.handler(records::add);
        assertTrue(records.isEmpty());
        stream.resume();
        assertEquals(List.of(record), records);
    }

    @Test
    void replacingHandlerDoesNotSubscribeAgain() {
        AtomicInteger subscriptions = new AtomicInteger();
        ReactiveReadStream<Record> stream = result(Flux.<Record>empty()
            .doOnSubscribe(ignored -> subscriptions.incrementAndGet())).records();
        assertEquals(0, subscriptions.get());
        stream.handler(null);
        assertEquals(0, subscriptions.get());
        stream.handler(record -> {});
        stream.handler(null);
        stream.handler(record -> {});
        assertEquals(1, subscriptions.get());
    }

    @Test
    void completionWaitsForRecordsBufferedAfterPause() {
        Record first = TestRecords.record("value", 1);
        Record second = TestRecords.record("value", 2);
        ReactiveReadStream<Record> stream = result(Flux.just(first, second)).records();
        List<Record> records = new ArrayList<>();
        AtomicInteger completions = new AtomicInteger();
        stream.endHandler(ignored -> completions.incrementAndGet());
        stream.handler(record -> {
            records.add(record);
            stream.pause();
        });
        assertEquals(List.of(first), records);
        assertEquals(0, completions.get());
        stream.resume();
        assertEquals(List.of(first, second), records);
        assertEquals(1, completions.get());
    }

    @Test
    void fetchDeliversRequestedRecordsBeforeCompletion() {
        Record first = TestRecords.record("value", 1);
        Record second = TestRecords.record("value", 2);
        ReactiveReadStream<Record> stream = result(Flux.just(first, second)).records();
        List<Record> records = new ArrayList<>();
        AtomicInteger completions = new AtomicInteger();
        stream.pause();
        stream.endHandler(ignored -> completions.incrementAndGet());
        stream.handler(records::add);
        stream.fetch(1);
        assertEquals(List.of(first), records);
        assertEquals(0, completions.get());
        stream.fetch(1);
        assertEquals(List.of(first, second), records);
        assertEquals(1, completions.get());
    }

    @Test
    void publisherDeliversRecordsEmittedAfterSubscription() {
        Sinks.Many<Record> publisher = Sinks.many().unicast().onBackpressureBuffer();
        Record record = TestRecords.record("value", 1);
        ReactiveReadStream<Record> stream = result(publisher.asFlux()).records();
        List<Record> records = new ArrayList<>();
        AtomicInteger completions = new AtomicInteger();
        stream.handler(records::add);
        stream.endHandler(ignored -> completions.incrementAndGet());
        assertEquals(Sinks.EmitResult.OK, publisher.tryEmitNext(record));
        assertEquals(List.of(record), records);
        assertEquals(0, completions.get());
        assertEquals(Sinks.EmitResult.OK, publisher.tryEmitComplete());
        assertEquals(1, completions.get());
    }

    @Test
    void publisherOnAnotherThreadCompletes() {
        Record record = TestRecords.record("value", 1);
        ReactiveReadStream<Record> stream = result(Flux.just(record)
            .subscribeOn(Schedulers.parallel())).records();
        assertEquals(List.of(record), ReadStreamCollector.collect(stream));
    }

    @Test
    void recordsAcrossMultipleBatchesAreDeliveredInOrder() {
        List<Record> expected = new ArrayList<>();
        for (int index = 0; index < 64; index++) {
            expected.add(TestRecords.record("value", index));
        }
        assertEquals(expected, ReadStreamCollector.collect(result(Flux.fromIterable(expected)).records()));
    }

    private static Neo4jResult result(Publisher<Record> records) {
        return new Neo4jResult(new ReactiveResult() {
            @Override
            public List<String> keys() {
                return List.of("value");
            }

            @Override
            public Publisher<Record> records() {
                return records;
            }

            @Override
            public Publisher<ResultSummary> consume() {
                return Mono.empty();
            }

            @Override
            public Publisher<Boolean> isOpen() {
                return Mono.just(true);
            }
        });
    }
}
