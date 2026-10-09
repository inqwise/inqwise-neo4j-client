package com.inqwise.neo4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
        try {
            completion.future().toCompletionStage().toCompletableFuture().get(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while collecting records", e);
        } catch (ExecutionException e) {
            throw new AssertionError("Record stream failed", e.getCause());
        } catch (TimeoutException e) {
            throw new AssertionError("Record stream did not complete within 2 seconds", e);
        }
		return items;
	}
}
