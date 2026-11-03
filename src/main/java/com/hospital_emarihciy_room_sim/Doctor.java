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
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Treatable patient = queue.take();
                
                if (patient == null) {
                    break;
                }

                log.log("Doctor " + id + " is assigned to " + patient.label() + ".");
                log.log("Doctor " + id + " is trying to acquire a treatment room.");
                boolean acquired = rooms.tryAcquire(3000);

                if (acquired) {

                    try {
                        log.log("Doctor " + id + " acquired a treatment room.");
                        log.log("Doctor " + id + " begins treating " + patient.label() + ".");
                        int treatmentTime = 1000 + random.nextInt(2300);
                        Thread.sleep(treatmentTime);
                        log.log("Doctor " + id + " finished treating " + patient.label() + ".");
                    } finally {
                        rooms.release();
                        log.log("Doctor " + id + " released the treatment room.");
                    }
                    patient.setResult(Treatable.Result.TREATED);
                    log.log("Doctor " + id + " is now available.");
                } else {
                    log.log("Doctor " + id + " could not acquire a treatment room.");
                    log.log("Doctor " + id + " is sending " + patient.label() + " back to the waiting room.");
                    
                    patient.setResult(Treatable.Result.SENT_BACK);
                    log.log("Doctor " + id + " is now available.");
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

}
