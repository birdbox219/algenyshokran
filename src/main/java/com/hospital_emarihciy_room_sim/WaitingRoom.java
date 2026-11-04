package com.hospital_emarihciy_room_sim;

import com.hospital_emarihciy_room_sim.interfaces.PatientQueue;
import com.hospital_emarihciy_room_sim.interfaces.Treatable;
import com.hospital_emarihciy_room_sim.interfaces.EventLogger;

import java.util.LinkedList;
import java.util.Queue;

public class WaitingRoom implements PatientQueue {

    private final Queue<Treatable> queue = new LinkedList<>();

    private final Semaphore empty;
    private final Semaphore full = new Semaphore(0);
    private final Semaphore mutex = new Semaphore(1);

    private final EventLogger log;

    public WaitingRoom(int capacity, EventLogger log) {
        this.empty = new Semaphore(capacity);
        this.log = log;
    }

    @Override
    public void put(Treatable patient) throws InterruptedException {

        boolean seatAcquired = false;
        boolean mutexAcquired = false;
        boolean added = false;

        try {

            if (!empty.tryAcquire(0)) {

                log.log("The waiting room is full.");
                log.log(patient.label() + " is waiting outside for an available seat.");

                empty.acquire();
            }

            seatAcquired = true;

            mutex.acquire();
            mutexAcquired = true;

            queue.add(patient);
            added = true;

            log.log(patient.label() + " entered the waiting room.");

        } finally {

            if (mutexAcquired) {
                mutex.release();
            }

            if (seatAcquired && !added) {
                empty.release();
            }
        }

        full.release();
    }

    @Override
    public Treatable take() throws InterruptedException {

        boolean mutexAcquired = false;
        boolean removed = false;

        Treatable patient = null;

        full.acquire();

        try {

            mutex.acquire();
            mutexAcquired = true;

            patient = queue.poll();

            if (patient != null) {
                removed = true;
            }

        } finally {

            if (mutexAcquired) {
                mutex.release();
            }

            if (!removed) {
                full.release();
            }
        }

        if (removed) {
            empty.release();
        }

        return patient;
    }
}
