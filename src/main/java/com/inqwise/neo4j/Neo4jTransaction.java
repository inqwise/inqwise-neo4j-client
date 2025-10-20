package com.inqwise.neo4j;

import org.neo4j.driver.reactivestreams.ReactiveTransaction;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * Reactive transaction wrapper that exposes Vert.x friendly helpers.
 */
public final class Neo4jTransaction {

	private final ReactiveTransaction delegate;

	Neo4jTransaction(ReactiveTransaction delegate) {
		this.delegate = delegate;
	}

	/**
	 * Executes a query within this transaction.
	 *
	 * @param query the Cypher query to run
	 * @return a future that completes with the {@link Neo4jResult}
	 */
	public Future<Neo4jResult> run(String query) {
		return run(query, null);
	}

	/**
	 * Executes a parametrised query within this transaction.
	 *
	 * @param query      the Cypher query to run
	 * @param parameters the parameters to bind (may be {@code null})
	 * @return a future that completes with the {@link Neo4jResult}
	 */
	public Future<Neo4jResult> run(String query, JsonObject parameters) {
		var pub = null == parameters ? delegate.run(query) : delegate.run(query, parameters.getMap());
		return FutureHelper.just(pub).map(Neo4jResult::new);
	}

	/**
	 * Commits the transaction.
	 *
	 * @return a future completing when the commit finishes
	 */
	public Future<Void> commit() {
		return FutureHelper.justEmpty(delegate.commit());
	}

	/**
	 * Closes the transaction.
	 *
	 * @return a future completing when the transaction has been closed
	 */
	public Future<Void> close() {
		return FutureHelper.justEmpty(delegate.close());
	}

	/**
	 * Rolls back the transaction.
	 *
	 * @return a future completing when the rollback finishes
	 */
	public Future<Void> rollback() {
		return FutureHelper.justEmpty(delegate.rollback());
	}

	/**
	 * Indicates whether the transaction is still open.
	 *
	 * @return a future completing with the transaction state
	 */
	public Future<Boolean> isOpen() {
		return FutureHelper.just(delegate.isOpen());
	}
}
