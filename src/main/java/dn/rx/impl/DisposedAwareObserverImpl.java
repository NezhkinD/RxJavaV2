package dn.rx.impl;

import dn.rx.Observer;

class DisposedAwareObserver<T> implements Observer<T> {
    private final Observer<? super T> observer;
    private final DisposableImpl disposable;

    public DisposedAwareObserver(Observer<? super T> observer, DisposableImpl disposable) {
        this.observer = observer;
        this.disposable = disposable;
    }

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