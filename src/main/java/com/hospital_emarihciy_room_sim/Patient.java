package com.hospital_emarihciy_room_sim;

import com.hospital_emarihciy_room_sim.interfaces.Treatable;
import com.hospital_emarihciy_room_sim.interfaces.PatientQueue;
import com.hospital_emarihciy_room_sim.interfaces.EventLogger;

public class Patient implements Treatable, Runnable {

    private final int id;
    private final PatientQueue queue;
    private final EventLogger log;
    private final Semaphore verdict = new Semaphore(0);
    private volatile Result result;

    public Patient(int id, PatientQueue queue, EventLogger log) {
        this.id = id;
        this.queue = queue;
        this.log = log;
    }

    @Override
    public String label() {
        return "P" + id;
    }

    @Override
    public void setResult(Result result) {
        this.result = result;
        verdict.release();
    }

    @Override
    public void run() {
        
         try {

            log.log(label() + " arrived.");

            while (true) {

                result = null;
                queue.put(this);
                verdict.acquire();

                if (result == Result.TREATED) {
                    break;
                }

                log.log(label() + " is returning to the waiting room.");
            }

            log.log(label() + " left.");

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            log.log(label() + " was interrupted.");
        }
        

            
        }




    }
    


