package com.hospital_emarihciy_room_sim;

import java.util.Random;
import com.hospital_emarihciy_room_sim.interfaces.*;

public class Doctor implements Runnable {

    private final int id;
    private final PatientQueue queue;
    private final EventLogger log;
    private final RoomPool rooms;
    private final Random random = new Random();

    public Doctor(int id, PatientQueue queue, EventLogger log, RoomPool rooms) {
        this.id = id;
        this.queue = queue;
        this.log = log;
        this.rooms = rooms;
    }

    @Override
    public void run() {
        // TODO: Implement the run method to simulate the doctor's behavior in the
        // hospital
        // I Got bored wallahy
    }

}
