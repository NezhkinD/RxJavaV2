import dn.rx.impl.Observable;
import org.junit.jupiter.api.Test;
import dn.scheduler.impl.IOImpl;
import dn.scheduler.impl.SingleImpl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class SchedulerTest {
    @Test
    void testIoScheduler_doesNotBlockMainThread() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> workerThreadName = new AtomicReference<>();

        Observable.just("X")
                .subscribeOn(new IOImpl())
                .subscribe(item -> {
                    workerThreadName.set(Thread.currentThread().getName());
                    latch.countDown();
                });

        assertTrue(latch.await(1, TimeUnit.SECONDS), "Задача не завершилась за 1 секунду");

        assertNotEquals(
                Thread.currentThread().getName(),
                workerThreadName.get(),
                "Задача должна была выполниться в фоновом потоке"
        );

        assertTrue(
                workerThreadName.get().contains("pool"),
                "Ожидался поток из пула (IO dn.scheduler)"
        );
    }

    @Test
    void testSingleScheduler_usesSameThreadForMultipleTasks() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        AtomicReference<String> firstTaskThread = new AtomicReference<>();
        AtomicReference<String> secondTaskThread = new AtomicReference<>();

        Observable.just("A")
                .observeOn(new SingleImpl())
                .subscribe(item -> {
                    firstTaskThread.set(Thread.currentThread().getName());
                    latch.countDown();
                });

        Observable.just("B")
                .observeOn(new SingleImpl())
                .subscribe(item -> {
                    secondTaskThread.set(Thread.currentThread().getName());
                    latch.countDown();
                });

        assertTrue(latch.await(1, TimeUnit.SECONDS), "Не все задачи завершились");

        assertEquals(
                firstTaskThread.get(),
                secondTaskThread.get(),
                "Обе задачи должны были выполняться в одном потоке"
        );

        assertNotEquals(
                Thread.currentThread().getName(),
                firstTaskThread.get(),
                "Задачи должны были выполняться в фоновом потоке"
        );
    }
}
