package dn.rx;


@FunctionalInterface
public interface Subscribe<T> {

    void subscribe(Observer<? super T> observer);
}

