package com.inqwise.neo4j;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.Promise;
import io.vertx.ext.reactivestreams.ReactiveReadStream;

final class ReadStreamCollector {

	private ReadStreamCollector() {
	}

	static <T> List<T> collect(ReactiveReadStream<T> stream) {
		List<T> items = new ArrayList<>();
		Promise<Void> completion = Promise.promise();
		stream.exceptionHandler(completion::fail);
		stream.endHandler(v -> completion.complete(null));
		stream.handler(items::add);
		stream.resume();
		completion.future().toCompletionStage().toCompletableFuture().join();
		return items;
	}
}
