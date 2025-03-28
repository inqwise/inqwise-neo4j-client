package com.inqwise.neo4j;

import org.neo4j.driver.Driver;
import org.neo4j.driver.reactivestreams.ReactiveSession;

import io.vertx.core.Future;

public class Neo4jDriver {

	private Driver delegate;

	Neo4jDriver(Driver delegate) {
		this.delegate = delegate;
	}
	
	public Neo4jSession session() {
        return new Neo4jSession(delegate.session(ReactiveSession.class));
    }
	
	public Future<Void> verifyConnectivity(){
		return Future.fromCompletionStage(delegate.verifyConnectivityAsync());
	}
	
	public Future<Void> close(){
		return Future.fromCompletionStage(delegate.closeAsync());
	}
}
