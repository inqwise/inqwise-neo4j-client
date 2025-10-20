package com.inqwise.neo4j;

import org.neo4j.driver.reactivestreams.ReactiveTransactionContext;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * Adapts {@link ReactiveTransactionContext} to Vert.x futures to ease custom transactional work.
 */
public final class Neo4jTransactionContext {

	private final ReactiveTransactionContext delegate;

	/**
	 * Creates a new wrapper around the provided transaction context.
	 *
	 * @param delegate the reactive context to adapt
	 */
	public Neo4jTransactionContext(ReactiveTransactionContext delegate) {
		this.delegate = delegate;
	}

	/**
	 * Runs a query without parameters using the underlying context.
	 *
	 * @param query the Cypher query to execute
	 * @return a future completing with the resulting {@link Neo4jResult}
	 */
	public Future<Neo4jResult> run(String query) {
		return run(query, null);
	}

	/**
	 * Runs a parametrised query using the underlying context.
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
