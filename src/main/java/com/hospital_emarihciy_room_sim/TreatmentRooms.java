package com.hospital_emarihciy_room_sim;

import com.hospital_emarihciy_room_sim.interfaces.RoomPool;

public class TreatmentRooms implements RoomPool {

    private final Semaphore rooms;

    public TreatmentRooms(int count) {
        this.rooms = new Semaphore(count);
    }

    @Override
    public boolean tryAcquire(long timeoutMs) throws InterruptedException {
        return rooms.tryAcquire(timeoutMs);
    }

    @Override
    public void release() {
        rooms.release();
    }

}
