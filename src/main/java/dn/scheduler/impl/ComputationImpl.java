package dn.scheduler.impl;

import dn.scheduler.Scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ComputationImpl implements Scheduler {
    private static final int N = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService EXEC = Executors.newFixedThreadPool(N);

    @Override
    public void schedule(Runnable task) {
        EXEC.submit(task);
    }
}

