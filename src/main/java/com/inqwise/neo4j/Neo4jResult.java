package com.inqwise.neo4j;

import org.neo4j.driver.Record;
import org.neo4j.driver.reactivestreams.ReactiveResult;

import io.vertx.ext.reactivestreams.ReactiveReadStream;

/**
 * Wraps a reactive Neo4j {@link ReactiveResult} and exposes it as a Vert.x {@link ReactiveReadStream}.
 */
public final class Neo4jResult {

	private final ReactiveResult delegate;

	/**
	 * Creates a new wrapper around the provided reactive result.
	 *
	 * @param delegate the reactive result to adapt
	 */
	public Neo4jResult(ReactiveResult delegate) {
		this.delegate = delegate;
	}

	/**
	 * Returns a read stream emitting each record produced by the query.
	 *
	 * @return a Vert.x reactive read stream of result records
	 */
	public ReactiveReadStream<Record> records() {
		var readStream = ReactiveReadStream.<Record>readStream();
		delegate.records().subscribe(readStream);
		return readStream;
	}

	ReactiveResult delegate() {
		return delegate;
	}
}
