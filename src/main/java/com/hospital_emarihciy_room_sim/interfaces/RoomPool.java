package com.hospital_emarihciy_room_sim.interfaces;

public interface RoomPool {
    boolean tryAcquire(long timeoutMs) throws InterruptedException;

    void release();
}
