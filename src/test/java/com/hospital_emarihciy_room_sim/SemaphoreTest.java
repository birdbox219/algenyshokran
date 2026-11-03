
package com.hospital_emarihciy_room_sim;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SemaphoreTest {

    @Test
    void testAcquire() throws InterruptedException {
        Semaphore semaphore = new Semaphore(1);

        semaphore.acquire();

        assertFalse(semaphore.tryAcquire(0));
    }

    @Test
    void testTryAcquire() throws InterruptedException {
        Semaphore semaphore = new Semaphore(1);

        assertTrue(semaphore.tryAcquire(1000));
        assertFalse(semaphore.tryAcquire(100));
    }

    @Test
    void testRelease() throws InterruptedException {
        Semaphore semaphore = new Semaphore(1);

        semaphore.acquire();
        semaphore.release();

        assertTrue(semaphore.tryAcquire(0));
    }

    @Test
    void testTimeout() throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);

        long startTime = System.currentTimeMillis();

        boolean result = semaphore.tryAcquire(500);

        long elapsedTime = System.currentTimeMillis() - startTime;

        assertFalse(result);
        assertTrue(elapsedTime >= 500);
    }

    @Test
    void testNegativePermits() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Semaphore(-1));
    }

    @Test
    void testNegativeTimeout() {
        Semaphore semaphore = new Semaphore(1);

        assertThrows(
                IllegalArgumentException.class,
                () -> semaphore.tryAcquire(-1));
    }

    @Test
    void testMultipleThreads() throws InterruptedException {

        Semaphore semaphore = new Semaphore(2);

        int numberOfDoctors = 10;

        AtomicInteger activeDoctors = new AtomicInteger(0);
        AtomicInteger maxActiveDoctors = new AtomicInteger(0);
        AtomicInteger completedDoctors = new AtomicInteger(0);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numberOfDoctors);

        for (int i = 0; i < numberOfDoctors; i++) {

            Thread doctor = new Thread(() -> {

                boolean acquired = false;

                try {
                    startLatch.await();

                    semaphore.acquire();
                    acquired = true;

                    int active = activeDoctors.incrementAndGet();

                    maxActiveDoctors.accumulateAndGet(
                            active, Math::max);

                    Thread.sleep(100);

                    completedDoctors.incrementAndGet();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();

                } finally {
                    if (acquired) {
                        activeDoctors.decrementAndGet();
                        semaphore.release();
                    }

                    finishLatch.countDown();
                }
            });

            doctor.start();
        }

        startLatch.countDown();

        finishLatch.await();

        assertEquals(numberOfDoctors, completedDoctors.get());
        assertTrue(maxActiveDoctors.get() <= 2);
    }

    @Test
    void testAcquireWaitsUntilRelease() throws InterruptedException {

        Semaphore semaphore = new Semaphore(0);

        AtomicBoolean acquired = new AtomicBoolean(false);

        CountDownLatch started = new CountDownLatch(1);

        Thread doctor = new Thread(() -> {
            try {
                started.countDown();

                semaphore.acquire();

                acquired.set(true);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        doctor.start();

        started.await();
        Thread.sleep(100);

        assertFalse(acquired.get());

        semaphore.release();

        doctor.join(2000);

        assertFalse(doctor.isAlive());
        assertTrue(acquired.get());
    }

    @Test
    void testTryAcquireSucceedsAfterRelease()
            throws InterruptedException {

        Semaphore semaphore = new Semaphore(0);

        AtomicBoolean acquired = new AtomicBoolean(false);

        CountDownLatch started = new CountDownLatch(1);

        Thread doctor = new Thread(() -> {
            try {
                started.countDown();

                acquired.set(semaphore.tryAcquire(2000));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        doctor.start();

        started.await();
        Thread.sleep(100);

        semaphore.release();

        doctor.join(2000);

        assertFalse(doctor.isAlive());
        assertTrue(acquired.get());
    }

    @Test
    void testAcquireInterrupted() throws InterruptedException {

        Semaphore semaphore = new Semaphore(0);

        AtomicBoolean interrupted = new AtomicBoolean(false);

        CountDownLatch started = new CountDownLatch(1);

        Thread doctor = new Thread(() -> {
            try {
                started.countDown();

                semaphore.acquire();

            } catch (InterruptedException e) {
                interrupted.set(true);
            }
        });

        doctor.start();

        started.await();
        Thread.sleep(100);

        doctor.interrupt();

        doctor.join(2000);

        assertFalse(doctor.isAlive());
        assertTrue(interrupted.get());
    }

    @Test
    void testTryAcquireInterrupted() throws InterruptedException {

        Semaphore semaphore = new Semaphore(0);

        AtomicBoolean interrupted = new AtomicBoolean(false);

        CountDownLatch started = new CountDownLatch(1);

        Thread doctor = new Thread(() -> {
            try {
                started.countDown();

                semaphore.tryAcquire(5000);

            } catch (InterruptedException e) {
                interrupted.set(true);
            }
        });

        doctor.start();

        started.await();
        Thread.sleep(100);

        doctor.interrupt();

        doctor.join(2000);

        assertFalse(doctor.isAlive());
        assertTrue(interrupted.get());
    }
}
