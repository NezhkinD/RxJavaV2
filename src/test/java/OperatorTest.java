import dn.operator.*;
import dn.rx.impl.Observable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class OperatorTest {
    @Test
    void testMapAndFilter_chainTransformsAndFiltersValues() {
        Observable<Integer> source = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onNext(3);
            observer.onComplete();
        });

        List<Integer> result = new ArrayList<>();

        Map.apply(source, i -> i * 10)
                .subscribe(item -> {});
        Filter.apply(Map.apply(source, i -> i * 10), i -> i > 10)
                .subscribe(result::add);

        assertEquals(List.of(20, 30), result);
    }

    @Test
    void testReduce_aggregatesValuesCorrectly() {
        Observable<Integer> source = Observable.create(observer -> {
            observer.onNext(2);
            observer.onNext(3);
            observer.onNext(5);
            observer.onComplete();
        });

        List<Integer> result = new ArrayList<>();
        Reduce.apply(source, Integer::sum).subscribe(result::add);

        assertEquals(List.of(10), result);
    }

    @Test
    void testMerge_combinesMultipleObservables() {
        Observable<String> first = Observable.create(observer -> {
            observer.onNext("A1");
            observer.onNext("A2");
            observer.onComplete();
        });

        Observable<String> second = Observable.create(observer -> {
            observer.onNext("B1");
            observer.onComplete();
        });

        List<String> mergedResults = new ArrayList<>();
        Merge.apply(first, second).subscribe(mergedResults::add);

        Set<String> expectedSet = Set.of("A1", "A2", "B1");
        Set<String> actualSet = new HashSet<>(mergedResults);

        assertEquals(expectedSet.size(), actualSet.size());
        assertTrue(actualSet.containsAll(expectedSet));
    }

    @Test
    void testConcat_emitsItemsInOrder() {
        Observable<String> first = Observable.create(observer -> {
            observer.onNext("q");
            observer.onComplete();
        });

        Observable<String> second = Observable.create(observer -> {
            observer.onNext("w");
            observer.onComplete();
        });

        List<String> results = new ArrayList<>();
        Concat.apply(first, second).subscribe(results::add);

        assertEquals(List.of("q", "w"), results);
    }

    @Test
    void testFlatMap_flattensInnerObservables() {
        Observable<Integer> source = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onComplete();
        });

        List<Integer> results = new ArrayList<>();
        FlatMap.apply(source, i -> Observable.just(i, i * 10))
                .subscribe(results::add);

        assertEquals(8, results.size()); // 1, 10, 2, 20
        assertEquals(List.of(1, 10, 1, 10, 2, 20, 2, 20), results);
    }
}
