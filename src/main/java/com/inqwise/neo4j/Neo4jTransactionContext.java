package com.inqwise.neo4j;

import org.neo4j.driver.reactivestreams.ReactiveTransactionContext;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class Neo4jTransactionContext {
	
	private ReactiveTransactionContext delegate;
	
	public Neo4jTransactionContext(ReactiveTransactionContext delegate) {
		this.delegate = delegate;
	}
	
	public Future<Neo4jResult> run(String query){
		return run(query, null);
	}

	public Future<Neo4jResult> run(String query, JsonObject parameters){
		var pub = null == parameters ?delegate.run(query) : delegate.run(query, parameters.getMap());
		return FutureHelper.just(pub).map(Neo4jResult::new);
	}
}
