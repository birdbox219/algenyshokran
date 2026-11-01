package com.hospital_emarihciy_room_sim;

import com.hospital_emarihciy_room_sim.interfaces.EventLogger;

public class CliLogger implements EventLogger {

    @Override
    public synchronized void log(String message) {
        System.out.println(message);
    }

}
