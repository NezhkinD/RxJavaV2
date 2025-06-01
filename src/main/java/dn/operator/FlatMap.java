package dn.operator;

import dn.rx.Observer;
import dn.rx.impl.CompositeDisposableImpl;
import dn.rx.impl.Observable;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class FlatMap {

    public static <T, R> Observable<R> apply(
            Observable<T> source,
            Function<? super T, ? extends Observable<? extends R>> mapper
    ) {
        return Observable.create(observer -> {
            FlatMapParentObserver<T, R> parentObserver = new FlatMapParentObserver<>(observer, mapper);
            source.subscribe(parentObserver);
        });
    }

    private static class FlatMapParentObserver<T, R> implements Observer<T> {
        private final Observer<? super R> downstream;
        private final Function<? super T, ? extends Observable<? extends R>> mapper;
        private final CompositeDisposableImpl composite;
        private final AtomicInteger activeCount;
        private final ConcurrentLinkedQueue<Throwable> errors;

        public FlatMapParentObserver(Observer<? super R> downstream, Function<? super T, ? extends Observable<? extends R>> mapper) {
            this.downstream = downstream;
            this.mapper = mapper;
            this.composite = new CompositeDisposableImpl();
            this.activeCount = new AtomicInteger(1);
            this.errors = new ConcurrentLinkedQueue<>();
        }

        @Override
        public void onNext(T item) {
            activeCount.incrementAndGet();
            Observable<? extends R> inner = mapper.apply(item);
            inner.subscribe(new InnerObserver<>(this));
            composite.add(inner.subscribe(new InnerObserver<>(this)));
        }

        @Override
        public void onError(Throwable t) {
            errors.add(t);
            checkCompletion();
        }

        @Override
        public void onComplete() {
            checkCompletion();
        }

        private void checkCompletion() {
            if (activeCount.decrementAndGet() == 0) {
                Throwable error = errors.poll();
                if (error != null) {
                    downstream.onError(error);
                } else {
                    downstream.onComplete();
                }
                composite.dispose();
            }
        }
    }

    private static class InnerObserver<T, R> implements Observer<R> {
        private final FlatMapParentObserver<T, R> parent;

        public InnerObserver(FlatMapParentObserver<T, R> parent) {
            this.parent = parent;
        }

        @Override
        public void onNext(R item) {
            parent.downstream.onNext(item);
        }

        @Override
        public void onError(Throwable t) {
            parent.errors.add(t);
            parent.checkCompletion();
        }

        @Override
        public void onComplete() {
            parent.checkCompletion();
        }
    }
}