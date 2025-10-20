package com.inqwise.neo4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.vertx.core.json.JsonObject;

class Neo4jTransactionTest {

	@Test
	void runDelegatesToReactiveTransaction() {
		var result = ReactiveStubs.result(java.util.List.of("value"), java.util.List.of(TestRecords.record("value", "hello")));
		var delegate = new ReactiveStubs.StubReactiveTransaction(result);
		var transaction = new Neo4jTransaction(delegate);

		Neo4jResult neo4jResult = transaction.run("RETURN $value").toCompletionStage().toCompletableFuture().join();

		assertEquals("RETURN $value", delegate.lastQuery);
		assertSame(result, neo4jResult.delegate());
	}

	@Test
	void runWithParametersPassesMap() {
		var result = ReactiveStubs.result(java.util.List.of("name"), java.util.List.of(TestRecords.record("name", "neo4j")));
		var delegate = new ReactiveStubs.StubReactiveTransaction(result);
		var transaction = new Neo4jTransaction(delegate);
		var params = new JsonObject(Map.of("name", "neo4j"));

		transaction.run("MATCH (n)", params).toCompletionStage().toCompletableFuture().join();

		assertEquals("MATCH (n)", delegate.lastQuery);
		assertEquals(Map.of("name", "neo4j"), delegate.lastParameters);
	}

	@Test
	void commitCloseAndRollbackPropagate() {
		var result = ReactiveStubs.result(java.util.List.of(), java.util.List.of());
		var delegate = new ReactiveStubs.StubReactiveTransaction(result);
		var transaction = new Neo4jTransaction(delegate);

		transaction.commit().toCompletionStage().toCompletableFuture().join();
		assertTrue(delegate.commitCalled);
		assertFalse(delegate.open);

		delegate.open = true;
		transaction.rollback().toCompletionStage().toCompletableFuture().join();
		assertTrue(delegate.rollbackCalled);
		assertFalse(delegate.open);

		delegate.open = true;
		transaction.close().toCompletionStage().toCompletableFuture().join();
		assertTrue(delegate.closeCalled);
		assertFalse(delegate.open);
	}

	@Test
	void isOpenReflectsUnderlyingTransactionState() {
		var result = ReactiveStubs.result(java.util.List.of(), java.util.List.of());
		var delegate = new ReactiveStubs.StubReactiveTransaction(result);
		var transaction = new Neo4jTransaction(delegate);

		assertTrue(transaction.isOpen().toCompletionStage().toCompletableFuture().join());
		delegate.open = false;
		assertFalse(transaction.isOpen().toCompletionStage().toCompletableFuture().join());
	}
}
