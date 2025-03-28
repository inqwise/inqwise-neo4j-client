package com.inqwise.neo4j;

import java.util.Objects;
import java.util.function.Function;

import org.neo4j.driver.TransactionConfig;
import org.neo4j.driver.reactivestreams.ReactiveSession;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.reactivestreams.ReactiveReadStream;
import reactor.core.publisher.Mono;

public class Neo4jSession {

	private ReactiveSession delegate;

	Neo4jSession(ReactiveSession delegate) {
		this.delegate = delegate;
	}

	public Future<Neo4jTransaction> beginTransaction() {
		return FutureHelper.just(delegate.beginTransaction()).map(Neo4jTransaction::new);
	}
	
	public Future<Void> close(){
		return FutureHelper.justEmpty(delegate.close());
	}
	
	public ReactiveReadStream<org.neo4j.driver.Record> executeRead(Function<Neo4jTransactionContext, Future<Neo4jResult>> callback){
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
	
	public ReactiveReadStream<org.neo4j.driver.Record> executeWrite(Function<Neo4jTransactionContext, Future<Neo4jResult>> callback){
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
	
	public Future<Neo4jResult> run(String query, TransactionConfig config){
		return run(query, null, config);
	}
	
	public Future<Neo4jResult> run(String query, JsonObject parameters, TransactionConfig config){
		var pub = null == parameters ?delegate.run(query, config) : delegate.run(query, parameters.getMap(), config);
		return FutureHelper.just(pub).map(Neo4jResult::new);
	}
	
	public Future<Neo4jResult> run(String query){
		return run(query, (JsonObject)null);
	}
	
	public Future<Neo4jResult> run(String query, JsonObject parameters){
		var pub = null == parameters ?delegate.run(query) : delegate.run(query, parameters.getMap());
		return FutureHelper.just(pub).map(Neo4jResult::new);
	}
}
