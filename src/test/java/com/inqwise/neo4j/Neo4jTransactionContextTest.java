package com.inqwise.neo4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.vertx.core.json.JsonObject;

class Neo4jTransactionContextTest {

	@Test
	void runDelegatesToUnderlyingContext() {
		var result = ReactiveStubs.result(java.util.List.of("value"), java.util.List.of(TestRecords.record("value", 42)));
		var delegate = new ReactiveStubs.StubReactiveTransactionContext(result);
		var context = new Neo4jTransactionContext(delegate);

		Neo4jResult neo4jResult = context.run("RETURN 1").toCompletionStage().toCompletableFuture().join();

		assertEquals("RETURN 1", delegate.lastQuery);
		assertSame(result, neo4jResult.delegate());
	}

	@Test
	void runWithParametersUsesProvidedMap() {
		var result = ReactiveStubs.result(java.util.List.of("name"), java.util.List.of(TestRecords.record("name", "graph")));
		var delegate = new ReactiveStubs.StubReactiveTransactionContext(result);
		var context = new Neo4jTransactionContext(delegate);
		var params = new JsonObject(Map.of("name", "graph"));

		context.run("MATCH (n)", params).toCompletionStage().toCompletableFuture().join();

		assertEquals("MATCH (n)", delegate.lastQuery);
		assertEquals(Map.of("name", "graph"), delegate.lastParameters);
	}
}
