package dn.operator;

import dn.rx.impl.Observable;
import dn.rx.Observer;

import java.util.function.Predicate;

public class Filter {
    public static <T> Observable<T> apply(
            Observable<T> source,
            Predicate<? super T> predicate
    ) {
        return Observable.create(observer -> {
            source.subscribe(new FilteringObserver<>(observer, predicate));
        });
    }

    private record FilteringObserver<T>(Observer<? super T> downstream,
                                        Predicate<? super T> predicate) implements Observer<T> {

        @Override
        public void onNext(T item) {
            if (predicate.test(item)) {
                downstream.onNext(item);
            }
        }

        @Override
        public void onError(Throwable t) {
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }
    }
}

