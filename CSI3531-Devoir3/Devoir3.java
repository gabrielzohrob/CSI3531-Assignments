import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

// TA thread class: defines what the Teaching Assistant does
class TA implements Runnable {

    public void run() {
        while (true) {
            try {
                // TA starts by sleeping until a student wakes him
                synchronized (System.out) {
                    System.out.println("Le TA est en train de dormir.\n");
                }

                // Wait until a student releases the 'asleep' semaphore to wake the TA
                Devoir3.asleep.acquire();

                while (true) {
                    // Lock the shared variables to prevent other threads from changing them
                    Devoir3.mutex.acquire();

                    // If no students are waiting, the TA goes back to sleep
                    if (Devoir3.waiting == 0) {
                        synchronized (System.out) {
                            System.out.println("Le TA retourne dormir. Aucun étudiant en attente.\n");
                        }

                        // Mark TA as asleep
                        Devoir3.taAwake.set(false);

                        // Release the lock
                        Devoir3.mutex.release();
                        break; // exit the inner loop and sleep again
                    }

                    // Take the next student from the waiting queue
                    int studentID = Devoir3.chairQueue.remove();
                    Devoir3.waiting--; // reduce number of waiting students

                    synchronized (System.out) {
                        System.out.println("Le TA commence à aider l'étudiant " + studentID +
                                ". Étudiants restants: " + Devoir3.waiting);
                    }

                    // Release the lock so other students can access shared variables
                    Devoir3.mutex.release();

                    // Simulate helping the student for 1–5 seconds
                    Thread.sleep((int) (Math.random() * 4000 + 1000));

                    // Signal the student that the help session is done
                    Devoir3.studentSem[studentID].release();

                    synchronized (System.out) {
                        System.out.println("Le TA a terminé d'aider l'étudiant " + studentID + ".\n");
                    }
                }
            } catch (Exception e) {
                System.out.println("Erreur dans TA: " + e.getMessage());
            }
        }
    }
}

// Student thread class: defines what each student does
class Student implements Runnable {
    private final int id;

    public Student(int ID) {
        this.id = ID; // save the student's unique ID
    }

    public void run() {
        while (true) {
            try {
                // Student is working (programming) before needing help
                synchronized (System.out) {
                    System.out.println("L'étudiant " + id + " est en train de programmer.\n");
                }

                // Simulate programming time (1–6 seconds)
                Thread.sleep((int) (Math.random() * 5000 + 1000));

                // Try to enter the hallway (critical section)
                Devoir3.mutex.acquire();

                // If a chair is available, take a seat
                if (Devoir3.waiting < Devoir3.chairs) {
                    Devoir3.chairQueue.add(id); // join the queue
                    Devoir3.waiting++; // increase number of waiting students

                    synchronized (System.out) {
                        System.out.println("L'étudiant " + id + " entre dans le couloir et s'assoit. Étudiants en attente: " + Devoir3.waiting + "\n");
                    }

                    // If this is the first student and TA is asleep, wake him
                    if (Devoir3.waiting == 1 && Devoir3.taAwake.compareAndSet(false, true)) {
                        synchronized (System.out) {
                            System.out.println("L'étudiant " + id + " réveille le TA.");
                        }
                        Devoir3.asleep.release(); // wake up the TA
                    }

                    // Done with shared data, release lock
                    Devoir3.mutex.release();

                    // Wait until the TA finishes helping
                    Devoir3.studentSem[id].acquire();

                    synchronized (System.out) {
                        System.out.println("L'étudiant " + id + " reçoit de l'aide du TA.\n");
                    }

                } else {
                    // If no chair is available, go back to programming
                    synchronized (System.out) {
                        System.out.println("L'étudiant " + id + " n'a pas trouvé de chaise disponible et retournera programmer et demander de l'aide plus tard.\n");
                    }

                    // Release the lock before going back to programming
                    Devoir3.mutex.release();

                    // Wait a bit before trying again (2–5 seconds)
                    Thread.sleep((int) (Math.random() * 3000 + 2000));
                }

            } catch (Exception e) {
                System.out.println("Erreur dans Student " + id + ": " + e.getMessage());
            }
        }
    }
}

// Main class: starts the simulation
public class Devoir3 {
    public static int numStudents = 5;          // Default number of students
    public static final int chairs = 3;         // Maximum number of hallway chairs

    // Shared variables and synchronization tools
    public static Semaphore mutex = new Semaphore(1);            // Controls access to shared resources
    public static Semaphore asleep = new Semaphore(0);           // Used to wake the TA
    public static Semaphore[] studentSem;                        // One semaphore per student
    public static Queue<Integer> chairQueue = new LinkedList<>(); // Queue to keep track of students in hallway
    public static int waiting = 0;                               // Current number of students waiting
    public static AtomicBoolean taAwake = new AtomicBoolean(false); // Tracks if TA is awake

    public static void main(String[] args) {
        int nStudents = numStudents;

        // Allow overriding the number of students through command-line argument
        if (args.length > 0) {
            try {
                nStudents = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Entrée invalide pour le nombre d'étudiants. Valeur par défaut : " + numStudents);
            }
        }

        // Initialize a private semaphore for each student
        studentSem = new Semaphore[nStudents];
        for (int i = 0; i < nStudents; i++) {
            studentSem[i] = new Semaphore(0);
        }

        // Start the TA thread
        Thread taThread = new Thread(new TA(), "TA-Thread");
        taThread.start();

        // Start all student threads
        for (int i = 0; i < nStudents; i++) {
            new Thread(new Student(i), "Student-" + i).start();
        }

        // Run the simulation for 20 seconds, then stop the program
        try {
            Thread.sleep(20000); // Run simulation
            System.out.println("\nSimulation terminée après 20 secondes.");
            System.exit(0);      // Forcefully end all threads
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}