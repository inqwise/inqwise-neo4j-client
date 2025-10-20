package com.inqwise.neo4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import io.vertx.core.Future;
import reactor.core.publisher.Mono;

class FutureHelperTest {

	@Test
	void justTransformsPublisherIntoSucceededFuture() {
		Future<String> future = FutureHelper.just(Mono.just("value"));
		assertEquals("value", future.toCompletionStage().toCompletableFuture().join());
	}

	@Test
	void justEmptyCompletesWithVoid() {
		Future<Void> future = FutureHelper.justEmpty(Mono.empty());
		assertNull(future.toCompletionStage().toCompletableFuture().join());
	}

	@Test
	void toPublisherConvertsFutureToMono() {
		Mono<String> mono = FutureHelper.toPublisher(Future.succeededFuture("value"));
		assertEquals("value", mono.block());
	}
}
