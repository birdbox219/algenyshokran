package com.hospital_emarihciy_room_sim;

import java.util.Scanner;
import com.hospital_emarihciy_room_sim.interfaces.*;

public class Hospital {
    private static final int TREATMENT_ROOMS = 2;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int cap = sc.nextInt();
        int nDoctors = sc.nextInt();
        int nPatients = sc.nextInt();
        System.out.println();

        EventLogger log = new CliLogger();
        RoomPool rooms = new TreatmentRooms(TREATMENT_ROOMS);
        PatientQueue waitingRoom = new WaitingRoom(cap, log);

        // TODO: Create and start doctor threads
        // TODO: Create and start patient threads
        // TODO: Wait for all threads to finish
        // TODO: Print the final statistics

    }
}
