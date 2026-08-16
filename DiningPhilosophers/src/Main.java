import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Main class for the Dining Philosophers problem.
 *
 * This class:
 * - Creates the shared forks
 * - Creates the philosophers
 * - Assigns left and right forks to each philosopher
 * - Uses a fixed thread pool to execute philosopher tasks
 * - Waits for all philosophers to finish
 * - Displays how many times each philosopher successfully ate
 */
public class Main {

    public static void main(String[] args)
            throws InterruptedException {

        // Total number of philosophers and forks.
        int numberOfPhilosophers = 5;

        /*
         * Array used to store all shared Fork objects.
         *
         * There are 5 philosophers and therefore 5 forks.
         */
        Fork[] forks =
                new Fork[numberOfPhilosophers];

        /*
         * Store all Philosopher objects so that their
         * final eating counts can be accessed later.
         */
        Philosopher[] philosophers =
                new Philosopher[numberOfPhilosophers];

        /*
         * Create the forks.
         *
         * Each fork gets a unique ID:
         * Fork 1, Fork 2, Fork 3, Fork 4, Fork 5.
         */
        for (int i = 0; i < numberOfPhilosophers; i++) {

            forks[i] = new Fork(i + 1);
        }

        /*
         * Create a fixed thread pool with 5 worker threads.
         *
         * Each philosopher is submitted as a Runnable task
         * and can therefore run concurrently.
         */
        ExecutorService executor =
                Executors.newFixedThreadPool(
                        numberOfPhilosophers
                );

        System.out.println();
        System.out.println(
                "=========================================="
        );
        System.out.println(
                "         DINING PHILOSOPHERS"
        );
        System.out.println(
                "=========================================="
        );
        System.out.println();

        /*
         * Create each philosopher and assign their
         * left and right forks.
         */
        for (int i = 0; i < numberOfPhilosophers; i++) {

            /*
             * The fork at the current array position
             * becomes the philosopher's left fork.
             */
            Fork leftFork = forks[i];

            /*
             * The next fork becomes the right fork.
             *
             * Modulo (%) creates the circular relationship.
             *
             * Example:
             *
             * i = 0 -> Fork 1 + Fork 2
             * i = 1 -> Fork 2 + Fork 3
             * i = 2 -> Fork 3 + Fork 4
             * i = 3 -> Fork 4 + Fork 5
             * i = 4 -> Fork 5 + Fork 1
             *
             * For the final philosopher:
             *
             * (4 + 1) % 5 = 0
             *
             * Therefore forks[0] becomes the right fork.
             */
            Fork rightFork =
                    forks[(i + 1) % numberOfPhilosophers];

            /*
             * Create the philosopher and store it
             * in the philosophers array.
             */
            philosophers[i] =
                    new Philosopher(
                            "Philosopher-" + (i + 1),
                            leftFork,
                            rightFork
                    );

            /*
             * Submit the philosopher task to the thread pool.
             *
             * A worker thread from the pool will execute
             * the philosopher's run() method.
             */
            executor.submit(philosophers[i]);
        }

        /*
         * Stop accepting new tasks.
         *
         * Tasks already submitted will continue executing.
         */
        executor.shutdown();

        /*
         * Make the main thread wait for the existing
         * philosopher tasks to finish.
         *
         * The main thread waits for a maximum of 1 minute.
         */
        executor.awaitTermination(
                1,
                TimeUnit.MINUTES
        );

        /*
         * Display the final number of successful meals
         * for each philosopher.
         */
        System.out.println();
        System.out.println(
                "=========================================="
        );
        System.out.println(
                "            FINAL RESULTS"
        );
        System.out.println(
                "=========================================="
        );

        /*
         * Enhanced for-loop used to access each
         * Philosopher object stored in the array.
         */
        for (Philosopher philosopher : philosophers) {

            System.out.printf(
                    "%-15s ate %d time(s)%n",
                    philosopher.getName(),
                    philosopher.getEatCount()
            );
        }

        System.out.println(
                "=========================================="
        );

        System.out.println(
                " All philosophers finished."
        );
    }
}

/*
 * Challenges Faced:
 *
 * 1. Preventing Deadlock:
 *    If every philosopher holds one fork and waits for another, the program
 *    can enter a deadlock. This was handled using timed tryLock(), so a
 *    philosopher releases the first fork if the second fork is unavailable.
 *
 * 2. Managing Shared Forks:
 *    Neighbouring philosophers must share the same Fork objects correctly.
 *    The modulo operator was used to create the circular relationship so
 *    the last philosopher shares Fork 1 with the first philosopher.
 */