package dn.operator;

import dn.rx.Observer;
import dn.rx.impl.Observable;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Merge {

    @SafeVarargs
    public static <T> Observable<T> apply(Observable<? extends T>... sources) {
        return Observable.create(observer -> {
            MergeObserver<T> mergeObserver = new MergeObserver<>(observer, sources.length);

            for (Observable<? extends T> source : sources) {
                source.subscribe(mergeObserver);
            }
        });
    }

    private static class MergeObserver<T> implements Observer<T> {
        private final Observer<? super T> downstream;
        private final AtomicInteger remaining;
        private final ConcurrentLinkedQueue<Throwable> errors;

        public MergeObserver(Observer<? super T> downstream, int sourceCount) {
            this.downstream = downstream;
            this.remaining = new AtomicInteger(sourceCount);
            this.errors = new ConcurrentLinkedQueue<>();
        }

        @Override
        public void onNext(T item) {
            downstream.onNext(item);
        }

        @Override
        public void onError(Throwable t) {
            errors.add(t);
            completeIfDone();
        }

        @Override
        public void onComplete() {
            completeIfDone();
        }

        private void completeIfDone() {
            if (remaining.decrementAndGet() == 0) {
                Throwable error = errors.poll();
                if (error != null) downstream.onError(error);
                else downstream.onComplete();
            }
        }
    }
}