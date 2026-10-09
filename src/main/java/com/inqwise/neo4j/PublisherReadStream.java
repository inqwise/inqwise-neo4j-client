package com.inqwise.neo4j;

import java.util.Objects;
import java.util.function.Supplier;

import org.reactivestreams.Publisher;

import io.vertx.core.Handler;
import io.vertx.ext.reactivestreams.impl.ReactiveReadStreamImpl;

final class PublisherReadStream<T> extends ReactiveReadStreamImpl<T> {

    private final Supplier<? extends Publisher<T>> publisher;
    private boolean subscribed;
    private boolean completed;
    private Throwable failure;
    private Handler<Void> endHandler;
    private Handler<Throwable> exceptionHandler;
    private boolean terminalDelivered;
    private long pendingRecords;

    PublisherReadStream(Supplier<? extends Publisher<T>> publisher) {
        super(DEFAULT_BATCH_SIZE);
        this.publisher = Objects.requireNonNull(publisher);
    }

    @Override
    public synchronized PublisherReadStream<T> handler(Handler<T> handler) {
        super.handler(handler == null ? null : record -> {
            try {
                handler.handle(record);
            } finally {
                pendingRecords--;
                deliverTerminal();
            }
        });
        if (handler != null && !subscribed) {
            subscribed = true;
            try {
                publisher.get().subscribe(this);
            } catch (Exception e) {
                onError(e);
            }
        }
        return this;
    }

    @Override
    public synchronized PublisherReadStream<T> endHandler(Handler<Void> handler) {
        endHandler = handler;
        deliverTerminal();
        return this;
    }

    @Override
    public synchronized PublisherReadStream<T> exceptionHandler(Handler<Throwable> handler) {
        exceptionHandler = handler;
        deliverTerminal();
        return this;
    }

    @Override
    public synchronized void onNext(T record) {
        Objects.requireNonNull(record);
        pendingRecords++;
        super.onNext(record);
    }

    @Override
    public synchronized void onComplete() {
        completed = true;
        deliverTerminal();
    }

    @Override
    public synchronized void onError(Throwable throwable) {
        failure = Objects.requireNonNull(throwable);
        deliverTerminal();
    }

    // Synchronous publishers may terminate while handler(...) is still returning.
    private void deliverTerminal() {
        if (terminalDelivered) {
            return;
        }
        if (failure != null && exceptionHandler != null) {
            terminalDelivered = true;
            exceptionHandler.handle(failure);
        } else if (completed && pendingRecords == 0 && endHandler != null) {
            terminalDelivered = true;
            endHandler.handle(null);
        }
    }
}
