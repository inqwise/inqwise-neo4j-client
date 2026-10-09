package com.inqwise.neo4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.neo4j.driver.Record;
import org.neo4j.driver.TransactionConfig;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.reactivestreams.ReactiveReadStream;

@Timeout(value = 5, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class Neo4jSessionTest {

	@Test
	void beginTransactionWrapsDelegate() {
		var result = ReactiveStubs.result(List.of("value"), List.of(TestRecords.record("value", 1)));
		var delegate = new ReactiveStubs.StubReactiveSession(result);
		var session = new Neo4jSession(delegate);

		Neo4jTransaction transaction = session.beginTransaction().toCompletionStage().toCompletableFuture().join();
		transaction.run("RETURN 1").toCompletionStage().toCompletableFuture().join();

		assertEquals("RETURN 1", delegate.transaction.lastQuery);
	}

	@Test
	void closePropagatesToReactiveSession() {
		var result = ReactiveStubs.result(List.of(), List.of());
		var delegate = new ReactiveStubs.StubReactiveSession(result);
		var session = new Neo4jSession(delegate);

		session.close().toCompletionStage().toCompletableFuture().join();
		assertTrue(delegate.closeCalled);
	}

	@Test
	void runWithAndWithoutParameters() {
		var result = ReactiveStubs.result(List.of("name"), List.of(TestRecords.record("name", "value")));
		var delegate = new ReactiveStubs.StubReactiveSession(result);
		var session = new Neo4jSession(delegate);

		Neo4jResult simple = session.run("RETURN $name").toCompletionStage().toCompletableFuture().join();
		assertSame(result, simple.delegate());
		assertEquals("RETURN $name", delegate.lastRunQuery);
		assertEquals(null, delegate.lastRunParameters);

		var params = new JsonObject(Map.of("name", "value"));
		session.run("MATCH (n)", params).toCompletionStage().toCompletableFuture().join();
		assertEquals("MATCH (n)", delegate.lastRunQuery);
		assertEquals(Map.of("name", "value"), delegate.lastRunParameters);
	}

	@Test
	void runWithTransactionConfigTracksConfig() {
		var result = ReactiveStubs.result(List.of("n"), List.of(TestRecords.record("n", 1)));
		var delegate = new ReactiveStubs.StubReactiveSession(result);
		var session = new Neo4jSession(delegate);
		var config = TransactionConfig.builder().withTimeout(Duration.ofSeconds(5)).build();

		session.run("RETURN 1", config).toCompletionStage().toCompletableFuture().join();
		assertEquals(config, delegate.lastRunConfig);

		var params = new JsonObject(Map.of("value", 2));
		session.run("RETURN $value", params, config).toCompletionStage().toCompletableFuture().join();
		assertEquals(Map.of("value", 2), delegate.lastRunParameters);
		assertEquals(config, delegate.lastRunConfig);
	}

	@Test
	void executeReadUsesCallbackAndEmitsRecords() {
		var record = TestRecords.record("value", 7);
		var result = ReactiveStubs.result(List.of("value"), List.of(record));
		var delegate = new ReactiveStubs.StubReactiveSession(result);
		var session = new Neo4jSession(delegate);

		List<Record> records = ReadStreamCollector.collect(session.executeRead(ctx -> ctx.run("RETURN $value", new JsonObject(Map.of("value", 7)))));
		assertEquals("RETURN $value", delegate.transactionContext.lastQuery);
		assertEquals(Map.of("value", 7), delegate.transactionContext.lastParameters);
		assertEquals(1, records.size());
		assertEquals("value", records.get(0).keys().get(0));
	}

	@Test
	void executeWriteUsesCallback() {
		var record = TestRecords.record("name", "graph");
		var result = ReactiveStubs.result(List.of("name"), List.of(record));
		var delegate = new ReactiveStubs.StubReactiveSession(result);
		var session = new Neo4jSession(delegate);

		List<Record> records = ReadStreamCollector.collect(session.executeWrite(ctx -> ctx.run("CREATE (n)")));

		assertEquals("CREATE (n)", delegate.transactionContext.lastQuery);
		assertEquals(1, records.size());
	}

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void transactionStreamStartsWithHandlerOnly(boolean write) {
        Record record = TestRecords.record("value", 7);
        var result = ReactiveStubs.result(List.of("value"), List.of(record));
        var session = new Neo4jSession(new ReactiveStubs.StubReactiveSession(result));
        ReactiveReadStream<Record> stream = write
            ? session.executeWrite(ctx -> ctx.run("RETURN 7"))
            : session.executeRead(ctx -> ctx.run("RETURN 7"));
        List<Record> records = new ArrayList<>();
        stream.handler(records::add);
        assertEquals(List.of(record), records);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void emptyTransactionStreamCompletes(boolean write) {
        var result = ReactiveStubs.result(List.of(), List.of());
        var session = new Neo4jSession(new ReactiveStubs.StubReactiveSession(result));
        ReactiveReadStream<Record> stream = write
            ? session.executeWrite(ctx -> ctx.run("RETURN 7 LIMIT 0"))
            : session.executeRead(ctx -> ctx.run("RETURN 7 LIMIT 0"));
        assertEquals(List.of(), ReadStreamCollector.collect(stream));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void failedTransactionCallbackReachesExceptionHandler(boolean write) {
        var result = ReactiveStubs.result(List.of(), List.of());
        var session = new Neo4jSession(new ReactiveStubs.StubReactiveSession(result));
        RuntimeException failure = new IllegalStateException("Query failed");
        ReactiveReadStream<Record> stream = write
            ? session.executeWrite(ctx -> Future.failedFuture(failure))
            : session.executeRead(ctx -> Future.failedFuture(failure));
        AtomicReference<Throwable> observed = new AtomicReference<>();
        stream.handler(record -> {});
        stream.exceptionHandler(observed::set);
        assertSame(failure, observed.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void thrownTransactionCallbackReachesExceptionHandler(boolean write) {
        var result = ReactiveStubs.result(List.of(), List.of());
        var session = new Neo4jSession(new ReactiveStubs.StubReactiveSession(result));
        RuntimeException failure = new IllegalStateException("Callback failed");
        ReactiveReadStream<Record> stream = write
            ? session.executeWrite(ctx -> { throw failure; })
            : session.executeRead(ctx -> { throw failure; });
        AtomicReference<Throwable> observed = new AtomicReference<>();
        stream.exceptionHandler(observed::set);
        stream.handler(record -> {});
        assertSame(failure, observed.get());
    }
}
