package com.hospital_emarihciy_room_sim;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Semaphore {

    private int permits;

    public Semaphore(int initialPermits) {
        if (initialPermits < 0)
            throw new IllegalArgumentException("Permits can't be negative");

        this.permits = initialPermits;
    }

    public void acquire() throws InterruptedException {
        while (true) {
            if (Thread.interrupted())
                throw new InterruptedException();

            synchronized (this) {
                if (permits > 0) {
                    permits--;
                    return;
                }
            }
        }
    }

    public boolean tryAcquire(long timeoutMs)
            throws InterruptedException {

        if (timeoutMs < 0)
            throw new IllegalArgumentException("Timeout can't be negative");

        if (Thread.interrupted())
            throw new InterruptedException();

        synchronized (this) {
            if (permits > 0) {
                permits--;
                return true;
            }
        }

        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < timeoutMs) {

            if (Thread.interrupted())
                throw new InterruptedException();

            synchronized (this) {
                if (permits > 0) {
                    permits--;
                    return true;
                }
            }
        }

        return false;
    }

    public synchronized void release() {
        permits++;
    }
}
