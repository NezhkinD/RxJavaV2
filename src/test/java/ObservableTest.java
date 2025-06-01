
import dn.rx.impl.Observable;
import dn.scheduler.impl.IOImpl;
import dn.scheduler.impl.SingleImpl;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;


public class ObservableTest {
    @Test
    void testCreateAndSubscribe_withMultipleItems() {
        List<String> received = new ArrayList<>();
        Observable<String> source = Observable.create(observer -> {
            observer.onNext("item1");
            observer.onNext("item2");
            observer.onNext("item3");
            observer.onComplete();
        });

        source.subscribe(
                received::add,
                Throwable::printStackTrace,
                () -> received.add("completed")
        );

        assertEquals(List.of("item1", "item2", "item3", "completed"), received);
    }

    @Test
    void testJustSingleElement() {
        List<Integer> items = new ArrayList<>();
        Observable.just(2).subscribe(items::add);

        assertEquals(1, items.size());
        assertEquals(2, items.getFirst());
    }

    @Test
    void testJustMultipleElements() {
        List<String> items = new ArrayList<>();
        Observable.<String>just("q", "w", "e", "r", "t", "y").subscribe(items::add);

        assertEquals(6, items.size());
        assertEquals(Arrays.asList("q", "w", "e", "r", "t", "y"), items);
    }

    @Test
    void testSubscribeOnScheduler_switchesThread() throws InterruptedException {
        AtomicReference<String> threadName = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Observable.just("X")
                .subscribeOn(new IOImpl())
                .subscribe(item -> {
                    threadName.set(Thread.currentThread().getName());
                    latch.countDown();
                });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertNotEquals(Thread.currentThread().getName(), threadName.get());
    }

    @Test
    void testObserveOnScheduler_switchesThreadForEmission() throws InterruptedException {
        AtomicReference<String> threadName = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Observable.just("Y")
                .observeOn(new SingleImpl())
                .subscribe(item -> {
                    threadName.set(Thread.currentThread().getName());
                    latch.countDown();
                });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertTrue(threadName.get().startsWith("pool-"));
    }

    @Test
    void testErrorHandling_propagatesErrorCorrectly() {
        List<Throwable> errors = new ArrayList<>();
        Observable<String> errorSource = Observable.create(observer -> {
            throw new RuntimeException("Test exception");
        });

        errorSource.subscribe(
                item -> fail("onNext не должен быть вызван"),
                errors::add,
                () -> fail("onComplete не должен быть вызван")
        );

        assertEquals(1, errors.size());
        assertEquals("Test exception", errors.getFirst().getMessage());
    }
}
