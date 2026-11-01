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
        // TODO: Implement the put method to add a patient to the waiting room queue

    }

    @Override
    public Treatable take() throws InterruptedException {
        // TODO: Implement the take method to remove a patient from the waiting room
        // queue

        return null; // Placeholder !!!!!!!! UWU replace it with your work

    }

}
