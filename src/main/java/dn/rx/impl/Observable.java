package dn.rx.impl;

import dn.rx.Observer;
import dn.rx.Subscribe;
import dn.scheduler.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.function.Consumer;

public class Observable<T> {
    private static final Logger log = LoggerFactory.getLogger(Observable.class);

    private final Subscribe<T> source;

    private Observable(Subscribe<T> source) {
        this.source = source;
    }

    public static <T> Observable<T> create(Subscribe<T> source) {
        log.debug("create observable");
        return new Observable<>(source);
    }

    public static <T> Observable<T> just(T item) {
        return create(observer -> {
            observer.onNext(item);
            observer.onComplete();
        });
    }

    @SafeVarargs
    public static <T> Observable<T> just(T... items) {
        return create(observer -> {
            Arrays.stream(items).forEach(observer::onNext);
            observer.onComplete();
        });
    }

    private static <T> Observer<T> fromConsumers(
            Consumer<? super T> onNext,
            Consumer<Throwable> onError,
            Runnable onComplete
    ) {
        return new Observer<T>() {
            @Override
            public void onNext(T item) {
                onNext.accept(item);
            }

            @Override
            public void onError(Throwable t) {
                onError.accept(t);
            }

            @Override
            public void onComplete() {
                onComplete.run();
            }
        };
    }

    public void subscribe(
            Consumer<? super T> onNext,
            Consumer<Throwable> onError,
            Runnable onComplete
    ) {
        Observer<T> observer = fromConsumers(onNext, onError, onComplete);
        subscribe(observer);
    }

    public void subscribe(Consumer<? super T> onNext) {
        subscribe(onNext, Throwable::printStackTrace, () -> {
        });
    }

    public DisposableImpl subscribe(Observer<? super T> observer) {
        log.debug("new subscribe observable");
        DisposableImpl disposable = new DisposableImpl();

        try {
            source.subscribe(new DisposedAwareObserver<>(observer, disposable));
        } catch (Throwable t) {
            observer.onError(t);
        }

        return disposable;
    }

    private record DisposedAwareObserver<T>(Observer<? super T> observer,
                                            DisposableImpl disposable) implements Observer<T> {

        @Override
        public void onNext(T item) {
            if (!disposable.isDisposed()) {
                observer.onNext(item);
            }
        }

        @Override
        public void onError(Throwable t) {
            if (!disposable.isDisposed()) {
                observer.onError(t);
            }
        }

        @Override
        public void onComplete() {
            if (!disposable.isDisposed()) {
                observer.onComplete();
            }
        }
    }

    public Observable<T> subscribeOn(Scheduler scheduler) {
        return create(observer ->
                scheduler.schedule(() -> subscribe(observer))
        );
    }

    public Observable<T> observeOn(Scheduler scheduler) {
        return create(observer ->
                this.subscribe(new ScheduledObserver<>(observer, scheduler))
        );
    }

    private record ScheduledObserver<T>(Observer<T> downstream, Scheduler scheduler) implements Observer<T> {

        @Override
        public void onNext(T item) {
            scheduler.schedule(() -> downstream.onNext(item));
        }

        @Override
        public void onError(Throwable t) {
            scheduler.schedule(() -> downstream.onError(t));
        }

        @Override
        public void onComplete() {
            scheduler.schedule(downstream::onComplete);
        }
    }
}