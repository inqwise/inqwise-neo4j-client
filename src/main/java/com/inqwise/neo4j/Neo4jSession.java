package com.inqwise.neo4j;

import java.util.Objects;
import java.util.function.Function;

import org.neo4j.driver.TransactionConfig;
import org.neo4j.driver.reactivestreams.ReactiveSession;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.reactivestreams.ReactiveReadStream;
import reactor.core.publisher.Mono;

/**
 * Reactive session wrapper that bridges Neo4j reactive types with Vert.x futures and read streams.
 */
public final class Neo4jSession {

	private final ReactiveSession delegate;

	Neo4jSession(ReactiveSession delegate) {
		this.delegate = delegate;
	}

	/**
	 * Begins a new reactive transaction.
	 *
	 * @return a future producing the opened {@link Neo4jTransaction}
	 */
	public Future<Neo4jTransaction> beginTransaction() {
		return FutureHelper.just(delegate.beginTransaction()).map(Neo4jTransaction::new);
	}

	/**
	 * Closes the session once all resources have been released.
	 *
	 * @return a future completing when the session has been closed
	 */
	public Future<Void> close() {
		return FutureHelper.justEmpty(delegate.close());
	}

	/**
	 * Executes a read transaction and streams the resulting records to the returned read stream.
	 *
	 * @param callback the transactional work to perform
	 * @return a reactive read stream of records
	 */
	public ReactiveReadStream<org.neo4j.driver.Record> executeRead(Function<Neo4jTransactionContext, Future<Neo4jResult>> callback) {
		Objects.requireNonNull(callback);

		ReactiveReadStream<org.neo4j.driver.Record> read = ReactiveReadStream.readStream();
		delegate.executeRead(
                tx -> {
                	return Mono.fromFuture(callback.apply(new Neo4jTransactionContext(tx)).toCompletionStage().toCompletableFuture())
                	.flatMapMany(r -> r.delegate().records());
                }
        ).subscribe(read);
		return read;
	}

	/**
	 * Executes a write transaction and returns the produced records.
	 *
	 * @param callback the transactional work to execute
	 * @return a reactive read stream of records emitted by the transaction
	 */
	public ReactiveReadStream<org.neo4j.driver.Record> executeWrite(Function<Neo4jTransactionContext, Future<Neo4jResult>> callback) {
		Objects.requireNonNull(callback);

		ReactiveReadStream<org.neo4j.driver.Record> read = ReactiveReadStream.readStream();
		delegate.executeWrite(
                tx -> {
                	return Mono.fromFuture(callback.apply(new Neo4jTransactionContext(tx)).toCompletionStage().toCompletableFuture())
                	.flatMapMany(r -> r.delegate().records());
                }
        ).subscribe(read);
		return read;
	}

	/**
	 * Runs a query using the supplied transaction configuration.
	 *
	 * @param query  the Cypher query to execute
	 * @param config the transaction configuration
	 * @return a future completing with the resulting {@link Neo4jResult}
	 */
	public Future<Neo4jResult> run(String query, TransactionConfig config) {
		return run(query, null, config);
	}

	/**
	 * Runs a parametrised query using the supplied transaction configuration.
	 *
	 * @param query      the Cypher query to execute
	 * @param parameters the parameters to bind (may be {@code null})
	 * @param config     the transaction configuration
	 * @return a future completing with the resulting {@link Neo4jResult}
	 */
	public Future<Neo4jResult> run(String query, JsonObject parameters, TransactionConfig config) {
		var pub = null == parameters ? delegate.run(query, config) : delegate.run(query, parameters.getMap(), config);
		return FutureHelper.just(pub).map(Neo4jResult::new);
	}

	/**
	 * Runs a query without parameters using the default transaction configuration.
	 *
	 * @param query the Cypher query to execute
	 * @return a future completing with the resulting {@link Neo4jResult}
	 */
	public Future<Neo4jResult> run(String query) {
		return run(query, (JsonObject) null);
	}

	/**
	 * Runs a parametrised query using the default transaction configuration.
	 *
	 * @param query      the Cypher query to execute
	 * @param parameters the parameters to bind (may be {@code null})
	 * @return a future completing with the resulting {@link Neo4jResult}
	 */
	public Future<Neo4jResult> run(String query, JsonObject parameters) {
		var pub = null == parameters ? delegate.run(query) : delegate.run(query, parameters.getMap());
		return FutureHelper.just(pub).map(Neo4jResult::new);
	}
}
