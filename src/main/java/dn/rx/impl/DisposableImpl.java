package dn.rx.impl;

import java.util.concurrent.atomic.AtomicBoolean;

public class DisposableImpl {
    private final AtomicBoolean disposed = new AtomicBoolean(false);

    public void dispose() {
        disposed.set(true);
    }

    public boolean isDisposed() {
        return disposed.get();
    }
}

