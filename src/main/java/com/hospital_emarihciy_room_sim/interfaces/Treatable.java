
package com.hospital_emarihciy_room_sim.interfaces;

public interface Treatable {
    enum Result {
        TREATED,
        SENT_BACK
    }

    String label();

    void setResult(Result result);
}
