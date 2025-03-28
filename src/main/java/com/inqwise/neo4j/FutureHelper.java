package com.inqwise.neo4j;

import org.reactivestreams.Publisher;

import io.vertx.core.Future;
import reactor.core.publisher.Mono;

public class FutureHelper {
	static  <T> Future<T> just(Publisher<T> publisher){
		return Future.fromCompletionStage(Mono.from(publisher).toFuture());
	}
	static  Future<Void> justEmpty(Publisher<?> publisher){
		return just(publisher).mapEmpty();
	}
	
	static <T> Mono<T> toPublisher(Future<T> future) {
		return Mono.fromCompletionStage(future.toCompletionStage().toCompletableFuture());
	}
}
