package dn.operator;

import dn.rx.Observer;
import dn.rx.impl.Observable;

import java.util.function.BiFunction;

public class Reduce {

    public static <T> Observable<T> apply(
            Observable<T> source,
            BiFunction<? super T, ? super T, ? extends T> accumulator
    ) {
        return Observable.create(observer -> source.subscribe(new ReducingObserver<>(observer, accumulator)));
    }

    private static class ReducingObserver<T> implements Observer<T> {
        private final Observer<? super T> downstream;
        private final BiFunction<? super T, ? super T, ? extends T> accumulator;
        private T accumulated;

        public ReducingObserver(Observer<? super T> downstream, BiFunction<? super T, ? super T, ? extends T> accumulator) {
            this.downstream = downstream;
            this.accumulator = accumulator;
        }

        @Override
        public void onNext(T item) {
            if (accumulated == null) accumulated = item;
            else accumulated = accumulator.apply(accumulated, item);
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            if (accumulated != null) downstream.onNext(accumulated);
            downstream.onComplete();
        }
    }
}
