package com.inqwise.neo4j;

import java.util.concurrent.atomic.AtomicReference;

import org.neo4j.driver.Record;
import org.neo4j.driver.reactivestreams.ReactiveResult;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.ext.reactivestreams.ReactiveReadStream;

public class Neo4jResult {

	private ReactiveResult delegate;

	public Neo4jResult(ReactiveResult delegate) {
		this.delegate = delegate;
	}
	
	public ReactiveReadStream<Record> records(){
		var readStream = ReactiveReadStream.<Record>readStream();
		delegate.records().subscribe(readStream);
		return readStream;
	}
		
//	static Future<Record> singleItem(Publisher<Record> publisher){
//		Promise<Record> promise = Promise.promise();
//		publisher.subscribe(new Subscriber<Record>(){
//			AtomicReference<Record> record = new AtomicReference<>();
//			@Override
//			public void onSubscribe(Subscription s) {
//			}
//
//			@Override
//			public void onNext(Record t) {
//				if(null != record.getAndSet(t))
//					throw new IllegalStateException("expected single record");
//			}
//
//			@Override
//			public void onError(Throwable t) {
//				promise.fail(t);
//			}
//
//			@Override
//			public void onComplete() {
//				promise.complete(record.get());
//			}});
//		
//		return promise.future();
//	};
	
	ReactiveResult delegate() {
		return delegate;
	}
}
