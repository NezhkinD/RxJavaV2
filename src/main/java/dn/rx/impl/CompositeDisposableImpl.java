package dn.rx.impl;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CompositeDisposableImpl {
    private final Set<DisposableImpl> disposables = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void add(DisposableImpl d) {
        disposables.add(d);
    }

    public void remove(DisposableImpl d) {
        disposables.remove(d);
    }

    public void dispose() {
        for (DisposableImpl d : disposables) {
            d.dispose();
        }
        disposables.clear();
    }

    public boolean isDisposed() {
        return disposables.stream().allMatch(DisposableImpl::isDisposed);
    }
}

