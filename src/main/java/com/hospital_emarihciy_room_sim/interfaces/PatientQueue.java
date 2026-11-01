package com.hospital_emarihciy_room_sim.interfaces;

public interface PatientQueue {
    void put(Treatable patient) throws InterruptedException;

    Treatable take() throws InterruptedException;
}
