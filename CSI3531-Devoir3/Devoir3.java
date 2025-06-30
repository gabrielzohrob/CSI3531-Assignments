import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

// TA thread class
class TA implements Runnable {

    public void run() {
        while (true) {
            try {
                synchronized (System.out) {
                    System.out.println("Le TA est en train de dormir.\n");
                }

                // TA waits to be woken up
                Devoir3.asleep.acquire();

                while (true) {
                    Devoir3.mutex.acquire();

                    // No students waiting
                    if (Devoir3.waiting == 0) {
                        synchronized (System.out) {
                            System.out.println("Le TA retourne dormir. Aucun étudiant en attente.\n");
                        }
                        Devoir3.taAwake.set(false);  // Mark TA as asleep
                        Devoir3.mutex.release();
                        break;
                    }

                    // Help next student in queue
                    int studentID = Devoir3.chairQueue.remove();
                    Devoir3.waiting--;

                    synchronized (System.out) {
                        System.out.println("Le TA commence à aider l'étudiant " + studentID +
                                ". Étudiants restants: " + Devoir3.waiting);
                    }

                    Devoir3.mutex.release();

                    // Simulate helping time
                    Thread.sleep((int) (Math.random() * 4000 + 1000));

                    // Notify student that help is done
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

// Student thread class
class Student implements Runnable {
    private final int id;

    public Student(int ID) {
        this.id = ID;
    }

    public void run() {
        while (true) {
            try {
                // Simulate programming
                synchronized (System.out) {
                    System.out.println("L'étudiant " + id + " est en train de programmer.\n");
                }
                Thread.sleep((int) (Math.random() * 5000 + 1000));

                Devoir3.mutex.acquire();

                if (Devoir3.waiting < Devoir3.chairs) {
                    // Take a seat in the hallway
                    Devoir3.chairQueue.add(id);
                    Devoir3.waiting++;

                    synchronized (System.out) {
                        System.out.println("L'étudiant " + id + " entre dans le couloir et s'assoit. Étudiants en attente: " + Devoir3.waiting + "\n");
                    }

                    // Wake the TA only if asleep
                    if (Devoir3.waiting==1 && Devoir3.taAwake.compareAndSet(false, true)) {
                        synchronized (System.out) {
                            System.out.println("L'étudiant " + id + " réveille le TA.");
                        }
                        Devoir3.asleep.release();
                    }

                    Devoir3.mutex.release();

                    // Wait for TA to help
                    Devoir3.studentSem[id].acquire();

                    synchronized (System.out) {
                        System.out.println("L'étudiant " + id + " reçoit de l'aide du TA.\n");
                    }

                } else {
                    // No chairs available
                    synchronized (System.out) {
                        System.out.println("L'étudiant " + id + " n'a pas trouvé de chaise disponible et retournera programmer et demander de l'aide plus tard.\n");
                    }
                    Devoir3.mutex.release();

                    // Wait before trying again
                    Thread.sleep((int) (Math.random() * 3000 + 2000));
                }

            } catch (Exception e) {
                System.out.println("Erreur dans Student " + id + ": " + e.getMessage());
            }
        }
    }
}

// Main class
public class Devoir3 {
    public static int numStudents = 5;
    public static final int chairs = 3;

    // Shared variables and synchronization tools
    public static Semaphore mutex = new Semaphore(1);
    public static Semaphore asleep = new Semaphore(0);
    public static Semaphore[] studentSem;
    public static Queue<Integer> chairQueue = new LinkedList<>();
    public static int waiting = 0;
    public static AtomicBoolean taAwake = new AtomicBoolean(false);

    public static void main(String[] args) {
        int nStudents = numStudents;

        // Optional command-line argument to override number of students
        if (args.length > 0) {
            try {
                nStudents = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Entrée invalide pour le nombre d'étudiants. Valeur par défaut : " + numStudents);
            }
        }

        // Initialize semaphores for each student
        studentSem = new Semaphore[nStudents];
        for (int i = 0; i < nStudents; i++) {
            studentSem[i] = new Semaphore(0);
        }

        // Start TA thread
        Thread taThread = new Thread(new TA(), "TA-Thread");
        taThread.start();

        // Start student threads
        for (int i = 0; i < nStudents; i++) {
            new Thread(new Student(i), "Student-" + i).start();
        }

        // Let the simulation run
        try {
            Thread.sleep(20000);  // Run for 20 seconds
            System.out.println("\nSimulation terminée après 20 secondes.");
            System.exit(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}