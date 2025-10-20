package com.inqwise.neo4j;

import org.neo4j.driver.Driver;
import org.neo4j.driver.reactivestreams.ReactiveSession;

import io.vertx.core.Future;

/**
 * Thin wrapper around the Neo4j {@link Driver} providing Vert.x friendly asynchronous APIs.
 */
public final class Neo4jDriver {

	private final Driver delegate;

	Neo4jDriver(Driver delegate) {
		this.delegate = delegate;
	}

	/**
	 * Opens a new reactive session backed by the underlying driver.
	 *
	 * @return a session exposing Vert.x style helpers
	 */
	public Neo4jSession session() {
		return new Neo4jSession(delegate.session(ReactiveSession.class));
	}

	/**
	 * Verifies the connectivity to the configured Neo4j server.
	 *
	 * @return a {@link Future} completing when connectivity was confirmed
	 */
	public Future<Void> verifyConnectivity() {
		return Future.fromCompletionStage(delegate.verifyConnectivityAsync());
	}

	/**
	 * Closes the underlying driver and releases associated resources.
	 *
	 * @return a {@link Future} completing once the driver has been closed
	 */
	public Future<Void> close() {
		return Future.fromCompletionStage(delegate.closeAsync());
	}
}
