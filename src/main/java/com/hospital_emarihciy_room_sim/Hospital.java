package com.hospital_emarihciy_room_sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.hospital_emarihciy_room_sim.interfaces.EventLogger;
import com.hospital_emarihciy_room_sim.interfaces.PatientQueue;
import com.hospital_emarihciy_room_sim.interfaces.RoomPool;

public class Hospital {
    private static final int TREATMENT_ROOMS = 2;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Waiting room capacity: ");
        System.out.flush();
        int cap = sc.nextInt();

        System.out.print("Number of doctors: ");
        System.out.flush();
        int nDoctors = sc.nextInt();

        System.out.print("Number of patients: ");
        System.out.flush();
        int nPatients = sc.nextInt();

        System.out.println();

        EventLogger log = new CliLogger();
        RoomPool rooms = new TreatmentRooms(TREATMENT_ROOMS);
        PatientQueue waitingRoom = new WaitingRoom(cap, log);

        List<Thread> doctorThreads = new ArrayList<>();
        for (int i = 1; i <= nDoctors; i++) {
            Thread doctor = new Thread(new Doctor(i, waitingRoom, log, rooms), "Doctor-" + i);
            doctorThreads.add(doctor);
            doctor.start();
        }

        List<Thread> patientThreads = new ArrayList<>();
        for (int i = 1; i <= nPatients; i++) {
            Thread patient = new Thread(new Patient(i, waitingRoom, log), "Patient-" + i);
            patientThreads.add(patient);
            patient.start();
        }

        for (Thread patient : patientThreads) {
            try {
                patient.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.log("All patients have been served.");

        for (Thread doctor : doctorThreads) {
            doctor.interrupt();
        }

        for (Thread doctor : doctorThreads) {
            try {
                doctor.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.log("End of the treatment :)");

        sc.close();
    }
}
