package com.inqwise.neo4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class Neo4jDriverTest {

	@Test
	void sessionCreatesNeo4jSession() {
		var result = ReactiveStubs.result(List.of("value"), List.of(TestRecords.record("value", 1)));
		var stubSession = new ReactiveStubs.StubReactiveSession(result);
		var stubDriver = new ReactiveStubs.StubDriver(stubSession);
		var driver = new Neo4jDriver(stubDriver);

		Neo4jSession session = driver.session();
		assertNotNull(session);
		session.run("RETURN 1").toCompletionStage().toCompletableFuture().join();
		assertEquals("RETURN 1", stubSession.lastRunQuery);
	}

	@Test
	void verifyConnectivityAndCloseDelegateToDriver() {
		var result = ReactiveStubs.result(List.of(), List.of());
		var stubSession = new ReactiveStubs.StubReactiveSession(result);
		var stubDriver = new ReactiveStubs.StubDriver(stubSession);
		var driver = new Neo4jDriver(stubDriver);

		driver.verifyConnectivity().toCompletionStage().toCompletableFuture().join();
		assertTrue(stubDriver.verifyConnectivityCalled);

		driver.close().toCompletionStage().toCompletableFuture().join();
		assertTrue(stubDriver.closeAsyncCalled);
	}
}
