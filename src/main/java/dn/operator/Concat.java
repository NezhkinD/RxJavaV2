package dn.operator;

import dn.rx.Observer;
import dn.rx.impl.Observable;

public class Concat {

    @SafeVarargs
    public static <T> Observable<T> apply(Observable<? extends T>... sources) {
        return Observable.create(observer -> {
            new ConcatObserver<>(observer, sources).start();
        });
    }

    private static class ConcatObserver<T> implements Observer<T> {
        private final Observer<? super T> downstream;
        private final Observable<? extends T>[] sources;
        private int index;

        @SafeVarargs
        public ConcatObserver(Observer<? super T> downstream, Observable<? extends T>... sources) {
            this.downstream = downstream;
            this.sources = sources;
        }

        public void start() {
            if (index < sources.length) subscribeToNext();
            else downstream.onComplete();

        }

        private void subscribeToNext() {
            sources[index++].subscribe(this);
        }

        @Override
        public void onNext(T item) {
            downstream.onNext(item);
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            if (index < sources.length) subscribeToNext();
            else downstream.onComplete();
        }
    }
}