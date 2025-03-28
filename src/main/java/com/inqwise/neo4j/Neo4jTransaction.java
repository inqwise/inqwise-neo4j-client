package com.inqwise.neo4j;

import org.neo4j.driver.reactivestreams.ReactiveTransaction;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class Neo4jTransaction {

	private ReactiveTransaction delegate;

	Neo4jTransaction(ReactiveTransaction delegate) {
		this.delegate = delegate;
	}
	
	public Future<Neo4jResult> run(String query){
		return run(query, null);
	}
	
	public Future<Neo4jResult> run(String query, JsonObject parameters){
		var pub = null == parameters ?delegate.run(query) : delegate.run(query, parameters.getMap());
		return FutureHelper.just(pub).map(Neo4jResult::new);
	}
	
	public Future<Void> commit() {
		return FutureHelper.justEmpty(delegate.commit());
	}
	
	public Future<Void> close() {
		return FutureHelper.justEmpty(delegate.close());
	}
	
	public Future<Void> rollback() {
		return FutureHelper.justEmpty(delegate.rollback());
	}
	
	public Future<Boolean> isOpen() {
		return FutureHelper.just(delegate.isOpen());
	}
}
