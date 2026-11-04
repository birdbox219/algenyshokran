# Hospital Emergency Room Simulation

A multithreaded simulation of a hospital emergency department, written in Java.
It models the classic **producer–consumer problem with a bounded buffer**: patients
produce themselves into a limited waiting room, doctors consume them, and a small
pool of treatment rooms limits how many patients can be treated at the same time.

The simulation demonstrates:

- Bounded-buffer synchronization using semaphores only (no `java.util.concurrent`
  synchronizers).
- Mutual exclusion over a shared queue.
- Timed resource acquisition (a doctor gives up after 3 seconds without a room).
- Interruptible blocking and coordinated shutdown of worker threads.

---

## Assignment context

Course assignment for **CS241/SCS241: Operating Systems** (Cairo University,
Faculty of Computing and Artificial Intelligence), Assignment 1 — *Hospital
Emergency Room Simulation*, Fall 2026.

Required deliverables and where they live:

| Requirement | Implementation |
|---|---|
| `Hospital` class | `Hospital.java` |
| `Semaphore` class with a timed acquire method | `Semaphore.java` |
| `Patient` class | `Patient.java` |
| `Doctor` class | `Doctor.java` |
| Additional classes for shared resources | `WaitingRoom.java`, `TreatmentRooms.java`, `CliLogger.java`, `interfaces/*` |

---

## System model

The hospital has three shared resources:

1. **Waiting room** — a bounded queue whose capacity is given by the user.
2. **Treatment rooms** — exactly **two** in the whole department.
3. **Verdict signal** — one per patient, used by the doctor to report the outcome.

### Rules

- A patient arrives and tries to enter the waiting room.
- If the waiting room is full, the patient waits **outside** until a seat is freed.
- A doctor stays idle until a patient is in the waiting room.
- The doctor takes one patient and becomes assigned to them.
- Before treating, the doctor tries to acquire a treatment room with a
  **3-second timeout**.
  - Success → treatment starts, lasts a random 1.0–3.3 seconds, then the room is
    released and the patient is marked `TREATED`.
  - Failure → the patient is marked `SENT_BACK`, returns to the waiting room, and
    the doctor becomes available again.
- A patient leaves the hospital only after being `TREATED`.

### Why the patient re-enqueues itself

A doctor never pushes a patient back into the waiting room. It only signals
`SENT_BACK`; the patient's own thread loops and re-enters the queue. This keeps the
doctor from blocking on a full waiting room, which could otherwise stall the whole
department.

---

## Class overview

### `Hospital` — entry point and orchestrator

- Reads three integers from `stdin`: waiting-room capacity, number of doctors,
  number of patients.
- Builds the shared objects: `CliLogger`, `TreatmentRooms(2)`, `WaitingRoom(cap)`.
- Starts all doctor threads, then all patient threads.
- Joins every patient thread, so `main` waits until every patient has been
  `TREATED` and has left.
- Interrupts the doctors (they are idle, blocked in the queue) and joins them.
- Prints the closing messages.

### `Semaphore` — counting semaphore built from scratch

| Method | Behaviour |
|---|---|
| `Semaphore(int initialPermits)` | Sets the permit count; rejects negatives. |
| `acquire()` | Blocks (spins) until a permit is taken; throws `InterruptedException` if interrupted. |
| `tryAcquire(long timeoutMs)` | Acquires if possible, otherwise retries for at most `timeoutMs`; returns `false` on timeout, `true` on success. |
| `release()` | Increments the permit count. |

Implementation notes:

- The permit counter is guarded by `synchronized (this)` blocks, so the
  "check then decrement" sequence is atomic. `release()` uses method-level
  `synchronized`, which locks the same object monitor.
- Waiting is implemented as **busy waiting** (`while` loops), matching the
  approach used in the course labs. Idle threads therefore consume CPU.
- `Thread.interrupted()` is checked inside the wait loops so an interrupted thread
  leaves gracefully with `InterruptedException`.
- The class is a raw counting semaphore: it has no upper bound and no ownership
  tracking, so each successful `acquire`/`tryAcquire` must be matched by exactly
  one `release`.

### `WaitingRoom` — bounded buffer

An `implements PatientQueue` wrapper around a `LinkedList<Treatable>` protected by
three semaphores:

| Semaphore | Initial value | Role |
|---|---:|---|
| `empty` | capacity | Counts free seats |
| `full` | 0 | Counts patients inside the room |
| `mutex` | 1 | Binary semaphore protecting the `LinkedList` |

`put(patient)`:

1. Try a seat without waiting (`empty.tryAcquire(0)`).
2. If none is available, log that the room is full and wait with `empty.acquire()`.
3. Take `mutex`, append to the queue, release `mutex`.
4. Release `full`.

If the thread is interrupted after taking a seat but before inserting the patient,
the seat is returned to `empty` in a `finally` block, so capacity accounting stays
correct.

`take()`:

1. Acquire `full` (blocks until a patient exists).
2. Take `mutex`, poll the head of the queue, release `mutex`.
3. Release `empty` (freeing a seat), then return the patient.

`full` is acquired *outside* the `try`, so an interrupt cannot leave a stray permit
behind.

### `TreatmentRooms` — semaphore-backed room pool

`implements RoomPool` and wraps a `Semaphore` initialised with 2 permits.
`tryAcquire(timeoutMs)` forwards to the semaphore, `release()` returns the permit.
Because the semaphore starts at 2, at most two doctors can hold a room at once.

### `Patient` — producer and result waiter

- Implements `Treatable` (so a doctor can signal it) and `Runnable`.
- Holds `Semaphore verdict = new Semaphore(0)` — an **event/notification
  semaphore**. `acquire()` blocks until the doctor calls `release()`.
- `setResult(...)` stores the `volatile` result and then releases the verdict
  permit. Storing before releasing means the patient always sees the value that
  woke it, and a result produced before the patient starts waiting is not lost.
- `run()`:

```
arrived
loop:
    result = null
    queue.put(this)     // enter waiting room (or wait outside if full)
    verdict.acquire()   // block until the doctor decides
    if result == TREATED: break
    log "returning to the waiting room"
left
```

### `Doctor` — consumer and resource holder

```
while not interrupted:
    patient = queue.take()            // idle until a patient exists
    log assigned
    acquired = rooms.tryAcquire(3000)
    if acquired:
        try  { log begin; sleep(random 1000..3299 ms); log finish }
        finally { rooms.release(); log released }
        patient.setResult(TREATED)
    else:
        log could not acquire / sending back
        patient.setResult(SENT_BACK)
    log available
```

The room is released in a `finally` block so an interrupted treatment still frees
the room. `InterruptedException` restores the interrupt flag and ends the loop,
which is how shutdown works.

### `CliLogger` and the interfaces

- `CliLogger implements EventLogger` prints a line per event and is
  `synchronized`, so log lines from concurrent threads never interleave.
- `interfaces/Treatable` — what a doctor can report to (`label()`,
  `setResult(Result)`, with `Result = { TREATED, SENT_BACK }`).
- `interfaces/PatientQueue` — `put` / `take`.
- `interfaces/RoomPool` — `tryAcquire(timeoutMs)` / `release`.
- `interfaces/EventLogger` — `log(message)`.

These interfaces keep `Doctor` independent of the concrete waiting room and room
pool implementations.

---

## Synchronization summary

| Concern | Mechanism |
|---|---|
| Bounded waiting room | `empty` / `full` counting semaphores |
| Queue integrity | `mutex` binary semaphore |
| Treatment room limit | `Semaphore(2)` in `TreatmentRooms` |
| Room wait limit | `tryAcquire(3000)` |
| Doctor → patient hand-off | per-patient `Semaphore(0)` + `volatile` result |
| Log integrity | `synchronized` logger |
| Shutdown | interrupt idle doctors, then `join` |

**Deadlock notes.** Waiting-room ordering is always `empty`/`full` → `mutex`, and
`mutex` is never held while another semaphore is acquired. Treatment rooms are a
separate resource with no nested acquisition. The doctor never blocks trying to
return a patient, because `SENT_BACK` is handled by the patient's own thread.
This combination is what keeps the simulation deadlock-free.

---

## Build and run

### Requirements

- JDK 24
- Maven 3.9.11

### Build and test

```powershell
mvn clean verify
```

### Run

```powershell
java -cp target/classes com.hospital_emarihciy_room_sim.Hospital
```

### Input

Three integers, one per line (or whitespace-separated):

```
<waiting room capacity>
<number of doctors>
<number of patients>
```

Example:

```
2
3
5
```

`capacity` and `doctors` must be at least 1, and `patients` must be at least 0.
The program does not validate these values yet, so `0` for capacity or doctors
makes the simulation wait forever.

### Sample run (excerpt)

Real output for capacity 2, 3 doctors, 5 patients. Thread scheduling changes the
order, and whether a doctor ever times out depends on timing.

```
P3 arrived.
P2 arrived.
P4 arrived.
P5 arrived.
The waiting room is full.
The waiting room is full.
P5 is waiting outside for an available seat.
P4 is waiting outside for an available seat.
P3 entered the waiting room.
P1 arrived.
The waiting room is full.
P1 is waiting outside for an available seat.
P2 entered the waiting room.
P4 entered the waiting room.
P5 entered the waiting room.
P1 entered the waiting room.
Doctor 1 is assigned to P4.
Doctor 1 is trying to acquire a treatment room.
Doctor 1 acquired a treatment room.
Doctor 1 begins treating P4.
Doctor 3 is assigned to P2.
Doctor 3 is trying to acquire a treatment room.
Doctor 2 is assigned to P3.
Doctor 3 acquired a treatment room.
Doctor 3 begins treating P2.
Doctor 2 is trying to acquire a treatment room.
Doctor 1 finished treating P4.
Doctor 2 acquired a treatment room.
Doctor 2 begins treating P3.
Doctor 1 released the treatment room.
P4 left.
Doctor 1 is now available.
...
All patients have been served.
End of the treatment :)
```

All required events are logged: arrival, entry to the waiting room, waiting
outside when full, assignment to a doctor, start of treatment, being sent back,
completion of treatment, and departure.

---

## Project structure

```
algenyshokran/
├── pom.xml
├── README.md
├── .gitignore
└── src/
    ├── main/java/com/hospital_emarihciy_room_sim/
    │   ├── Hospital.java          # entry point, thread orchestration
    │   ├── Doctor.java            # consumer, room holder
    │   ├── Patient.java           # producer, verdict waiter
    │   ├── WaitingRoom.java       # bounded buffer (empty/full/mutex)
    │   ├── TreatmentRooms.java    # 2-permit room pool
    │   ├── Semaphore.java         # custom counting semaphore
    │   ├── CliLogger.java         # synchronized console logger
    │   └── interfaces/
    │       ├── Treatable.java
    │       ├── PatientQueue.java
    │       ├── RoomPool.java
    │       └── EventLogger.java
    └── test/java/com/hospital_emarihciy_room_sim/
        ├── AppTest.java
        └── SemaphoreTest.java     # 11 semaphore tests
```

---

## Tests

`SemaphoreTest` covers:

- basic acquire and release
- `tryAcquire` success and timeout
- rejection of negative permits and negative timeouts
- mutual exclusion under 10 concurrent threads
- acquisition blocked until another thread releases
- successful `tryAcquire` after a later release
- interruption of both `acquire` and `tryAcquire`

`AppTest` is still the Maven placeholder (`assertTrue(true)`) and should be
replaced with a waiting-room or end-to-end test.

---

## Known limitations

- **Busy waiting:** `Semaphore` spins instead of using `wait()`/`notifyAll()`, so
  blocked threads consume CPU. Correct but inefficient.
- **Wall-clock timeout:** `tryAcquire` measures elapsed time with
  `System.currentTimeMillis()`; `System.nanoTime()` would be more robust.
- **No input validation:** zero capacity or zero doctors causes a non-terminating
  simulation.
- **Interrupted doctor:** if a doctor is interrupted while holding an assigned
  patient, that patient never receives a verdict. Current shutdown avoids this by
  interrupting doctors only after every patient has left.
