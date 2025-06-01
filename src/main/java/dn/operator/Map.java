package dn.operator;

import dn.rx.Observer;
import dn.rx.impl.Observable;

import java.util.function.Function;

public class Map {
    public static <T, R> Observable<R> apply(
            Observable<T> source,
            Function<? super T, ? extends R> mapper
    ) {
        return Observable.create(observer -> {
            source.subscribe(new MappingObserver<>(observer, mapper));
        });
    }

    private record MappingObserver<T, R>(Observer<? super R> downstream,
                                         Function<? super T, ? extends R> mapper) implements Observer<T> {

        @Override
            public void onNext(T item) {
                downstream.onNext(mapper.apply(item));
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

