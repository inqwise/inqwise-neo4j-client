package com.inqwise.neo4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.neo4j.driver.AuthToken;
import org.neo4j.driver.BaseSession;
import org.neo4j.driver.Driver;
import org.neo4j.driver.ExecutableQuery;
import org.neo4j.driver.Query;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.TransactionConfig;
import org.neo4j.driver.Value;
import org.neo4j.driver.reactivestreams.ReactiveResult;
import org.neo4j.driver.reactivestreams.ReactiveSession;
import org.neo4j.driver.reactivestreams.ReactiveTransaction;
import org.neo4j.driver.reactivestreams.ReactiveTransactionCallback;
import org.neo4j.driver.reactivestreams.ReactiveTransactionContext;
import org.neo4j.driver.summary.ResultSummary;
import org.reactivestreams.Publisher;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

final class ReactiveStubs {

	private ReactiveStubs() {
	}

	static StubReactiveResult result(List<String> keys, List<org.neo4j.driver.Record> records) {
		return new StubReactiveResult(keys, records);
	}

	static final class StubReactiveResult implements ReactiveResult {
		private final List<String> keys;
		private final List<org.neo4j.driver.Record> records;
		private final Mono<Boolean> openPublisher;

		StubReactiveResult(List<String> keys, List<org.neo4j.driver.Record> records) {
			this.keys = keys;
			this.records = new ArrayList<>(records);
			this.openPublisher = Mono.just(Boolean.TRUE);
		}

		@Override
		public List<String> keys() {
			return keys;
		}

		@Override
		public Publisher<org.neo4j.driver.Record> records() {
			return Flux.fromIterable(records);
		}

		@Override
		public Publisher<ResultSummary> consume() {
			return Mono.empty();
		}

		@Override
		public Publisher<Boolean> isOpen() {
			return openPublisher;
		}
	}

	static class StubReactiveTransactionContext implements ReactiveTransactionContext {

		String lastQuery;
		Map<String, Object> lastParameters;
		TransactionConfig lastConfig;
		final StubReactiveResult result;

		StubReactiveTransactionContext(StubReactiveResult result) {
			this.result = Objects.requireNonNull(result);
		}

		@Override
		public Publisher<ReactiveResult> run(String query) {
			this.lastQuery = query;
			this.lastParameters = null;
			this.lastConfig = null;
			return Mono.just(result);
		}

		@Override
		public Publisher<ReactiveResult> run(String query, Map<String, Object> parameters) {
			this.lastQuery = query;
			this.lastParameters = parameters;
			this.lastConfig = null;
			return Mono.just(result);
		}

		@Override
		public Publisher<ReactiveResult> run(String query, Value value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Publisher<ReactiveResult> run(String query, org.neo4j.driver.Record record) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Publisher<ReactiveResult> run(Query query) {
			throw new UnsupportedOperationException();
		}


	}

	static class StubReactiveTransaction implements ReactiveTransaction {
		String lastQuery;
		Map<String, Object> lastParameters;
		TransactionConfig lastConfig;
		boolean commitCalled;
		boolean rollbackCalled;
		boolean closeCalled;
		boolean open = true;
		final StubReactiveResult result;

		StubReactiveTransaction(StubReactiveResult result) {
			this.result = result;
		}

		@Override
		public Publisher<ReactiveResult> run(String query) {
			this.lastQuery = query;
			this.lastParameters = null;
			this.lastConfig = null;
			return Mono.just(result);
		}

		@Override
		public Publisher<ReactiveResult> run(String query, Map<String, Object> parameters) {
			this.lastQuery = query;
			this.lastParameters = parameters;
			this.lastConfig = null;
			return Mono.just(result);
		}

		@Override
		public Publisher<ReactiveResult> run(String query, Value value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Publisher<ReactiveResult> run(String query, org.neo4j.driver.Record record) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Publisher<ReactiveResult> run(Query query) {
			throw new UnsupportedOperationException();
		}


		@Override
		public <T> Publisher<T> commit() {
			this.commitCalled = true;
			this.open = false;
			return Mono.<T>empty();
		}

		@Override
		public <T> Publisher<T> rollback() {
			this.rollbackCalled = true;
			this.open = false;
			return Mono.<T>empty();
		}

		@Override
		public Publisher<Void> close() {
			this.closeCalled = true;
			this.open = false;
			return Mono.empty();
		}

		@Override
		public Publisher<Boolean> isOpen() {
			return Mono.just(open);
		}
	}

	static class StubReactiveSession implements ReactiveSession {

		final StubReactiveTransactionContext transactionContext;
		final StubReactiveTransaction transaction;
		StubReactiveResult runResult;
		boolean closeCalled;
		String lastRunQuery;
		Map<String, Object> lastRunParameters;
		TransactionConfig lastRunConfig;

		StubReactiveSession(StubReactiveResult result) {
			this.runResult = result;
			this.transaction = new StubReactiveTransaction(result);
			this.transactionContext = new StubReactiveTransactionContext(result);
		}

		@Override
		public Publisher<ReactiveTransaction> beginTransaction(TransactionConfig config) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Publisher<ReactiveTransaction> beginTransaction() {
			return Mono.just(transaction);
		}

		@Override
		public <T> Publisher<T> close() {
			this.closeCalled = true;
			return Mono.empty();
		}

		@Override
		public <T> Publisher<T> executeRead(ReactiveTransactionCallback<? extends Publisher<T>> callback) {
			Publisher<T> publisher = callback.execute(transactionContext);
			return Flux.from(publisher);
		}

		@Override
		public <T> Publisher<T> executeRead(ReactiveTransactionCallback<? extends Publisher<T>> callback, TransactionConfig config) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> Publisher<T> executeWrite(ReactiveTransactionCallback<? extends Publisher<T>> callback) {
			Publisher<T> publisher = callback.execute(transactionContext);
			return Flux.from(publisher);
		}

		@Override
		public <T> Publisher<T> executeWrite(ReactiveTransactionCallback<? extends Publisher<T>> callback, TransactionConfig config) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Publisher<ReactiveResult> run(String query) {
			this.lastRunQuery = query;
			this.lastRunParameters = null;
			this.lastRunConfig = null;
			return Mono.just(runResult);
		}

		@Override
		public Publisher<ReactiveResult> run(String query, Map<String, Object> parameters) {
			this.lastRunQuery = query;
			this.lastRunParameters = parameters;
			this.lastRunConfig = null;
			return Mono.just(runResult);
		}

		@Override
		public Publisher<ReactiveResult> run(String query, TransactionConfig config) {
			this.lastRunQuery = query;
			this.lastRunParameters = null;
			this.lastRunConfig = config;
			return Mono.just(runResult);
		}

		@Override
		public Publisher<ReactiveResult> run(String query, Map<String, Object> parameters, TransactionConfig config) {
			this.lastRunQuery = query;
			this.lastRunParameters = parameters;
			this.lastRunConfig = config;
			return Mono.just(runResult);
		}

		@Override
		public Publisher<ReactiveResult> run(String query, Value value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Publisher<ReactiveResult> run(String query, org.neo4j.driver.Record record) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Publisher<ReactiveResult> run(Query query) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Publisher<ReactiveResult> run(Query query, TransactionConfig config) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Set<org.neo4j.driver.Bookmark> lastBookmarks() {
			return Collections.emptySet();
		}

	}

	static class StubDriver implements Driver {
		final StubReactiveSession session;
		boolean verifyConnectivityCalled;
		boolean closeAsyncCalled;
		boolean closeCalled;

		StubDriver(StubReactiveSession session) {
			this.session = session;
		}

		@Override
		public ExecutableQuery executableQuery(String query) {
			throw new UnsupportedOperationException();
		}

		@Override
		public org.neo4j.driver.BookmarkManager executableQueryBookmarkManager() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isEncrypted() {
			return false;
		}

		@Override
		public <T extends BaseSession> T session(Class<T> sessionType) {
			if (sessionType.isInstance(session)) {
				return sessionType.cast(session);
			}
			throw new UnsupportedOperationException();
		}

		@Override
		public <T extends BaseSession> T session(Class<T> sessionType, AuthToken authToken) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T extends BaseSession> T session(Class<T> sessionType, SessionConfig sessionConfig) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T extends BaseSession> T session(Class<T> sessionType, SessionConfig sessionConfig, AuthToken authToken) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void close() {
			this.closeCalled = true;
		}

		@Override
		public CompletionStage<Void> closeAsync() {
			this.closeAsyncCalled = true;
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public void verifyConnectivity() {
			this.verifyConnectivityCalled = true;
		}

		@Override
		public CompletionStage<Void> verifyConnectivityAsync() {
			this.verifyConnectivityCalled = true;
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public boolean verifyAuthentication(AuthToken authToken) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean supportsSessionAuth() {
			return false;
		}

		@Override
		public boolean supportsMultiDb() {
			return false;
		}

		@Override
		public CompletionStage<Boolean> supportsMultiDbAsync() {
			return CompletableFuture.completedFuture(Boolean.FALSE);
		}
	}
}
