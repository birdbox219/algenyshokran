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

        while (!Thread.currentThread().isInterrupted()) {
            try {

                Treatable patient = waitingRoom.dequeue();
                
                if (patient == null) {
                    break;
                }

                System.out.println("Doctor " + id + " is assigned to " + patient.getName() + ".");


                System.out.println("Doctor " + id + " is trying to acquire a treatment room.");
                boolean acquired = treatmentRooms.tryAcquireRoom(3000);

                if (acquired) {

                    System.out.println("Doctor " + id + " acquired a treatment room.");
                    System.out.println("Doctor " + id + " begins treating " + patient.getName() + ".");


                    int treatmentTime = 1000 + random.nextInt(2300);
                    try {
    
                        Thread.sleep(treatmentTime);
                        }        
                    finally 
                    {
                        rooms.release();
                    }


                    System.out.println("Doctor " + id + " finished treating " + patient.getName() + ".");
                    treatmentRooms.releaseRoom();
                    System.out.println("Doctor " + id + " released the treatment room.");
                    
                    patient.setTreated(true);
                    System.out.println("Doctor " + id + " is now available.");
                } else {

                    System.out.println("Doctor " + id + " could not acquire a treatment room.");
                    System.out.println("Doctor " + id + " is sending " + patient.getName() + " back to the waiting room.");
                    
                    patient.setResult(Treatable.Result.SENT_BACK)
                    System.out.println("Doctor " + id + " is now available.");
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

}
