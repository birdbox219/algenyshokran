package com.hospital_emarihciy_room_sim;

public class Semaphore {

    // TODO: implement the semaphore class with the following methods:
    // acquire() - to acquire a permit from the semaphore, blocking if necessary
    // until one is available`
    // release() - to release a permit back to the semaphore
    // tryAcquire(long timeoutMs) - to try to acquire a permit from the semaphore,
    // blocking up to the specified timeout if necessary until one is available.
    // Returns true if a permit was acquired and false if the timeout elapsed before
    // a permit was acquired.
    // The semaphore should be initialized with a given number of permits, which
    // represents the number of available resources (in this case, treatment rooms).

    // please for follow the naming conveention X_x

    public Semaphore(int initialPermits) {

    }

    public synchronized void acquire() throws InterruptedException {

    }

    public synchronized boolean tryAcquire(long timeoutMs) throws InterruptedException {
        return false;
    }

    public synchronized void release() {

    }
}
