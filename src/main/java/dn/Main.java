package dn;

import dn.rx.impl.Observable;
import dn.operator.*;
import dn.scheduler.impl.ComputationImpl;
import dn.scheduler.impl.IOImpl;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        runExamples(
                new Example("mapAndFilter", Main::mapFilterExample),
                new Example("flatMap", Main::flatMapExample),
                new Example("merge", Main::mergeExample),
                new Example("concat", Main::concatExample)
        );

        Thread.sleep(1000);
    }

    private static void runExamples(Example... examples) {
        for (Example example : examples) {
            example.run();
        }
    }

    private static void mapFilterExample() {
        Observable<Integer> source = Observable.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        Filter.apply(
                        Map.apply(source, i -> i * 10),
                        i -> i >= 30
                )
                .subscribeOn(new IOImpl())
                .observeOn(new ComputationImpl())
                .subscribe(
                        item -> System.out.println("Received: " + item),
                        Throwable::printStackTrace,
                        () -> System.out.println("Completed")
                );
    }

    private static void flatMapExample() {
        Observable<Integer> source = Observable.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        FlatMap.apply(source, i ->
                Observable.just(i, i * i)
        ).subscribe(item -> System.out.println("flatMap: " + item));
    }

    private static void mergeExample() {
        Merge.apply(
                Observable.just("q", "w"),
                Observable.just("1", "2")
        ).subscribe(item -> System.out.println("merge: " + item));
    }

    private static void concatExample() {
        Concat.apply(
                Observable.just("a", "s"),
                Observable.just("d")
        ).subscribe(item -> System.out.println("concat: " + item));
    }

    private record Example(String title, Runnable action) {
        public void run() {
            System.out.println("----" + title + ": " + action);
            action.run();
        }
    }
}