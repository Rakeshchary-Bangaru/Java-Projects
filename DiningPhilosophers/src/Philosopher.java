import java.util.concurrent.TimeUnit;

/**
 * Represents a philosopher in the Dining Philosophers problem.
 *
 * Each philosopher:
 * - Has a name
 * - Shares a left and right fork with neighbouring philosophers
 * - Thinks for some time
 * - Tries to acquire both forks
 * - Eats only when both forks are successfully acquired
 *
 * Runnable is implemented so that each philosopher can be executed
 * as a task by a Thread or ExecutorService.
 */
public class Philosopher implements Runnable {

    // Name used to identify the philosopher in the console output.
    private final String name;

    // Shared fork on the philosopher's left side.
    private final Fork leftFork;

    // Shared fork on the philosopher's right side.
    private final Fork rightFork;

    // Counts how many times this philosopher successfully ate.
    private int eatCount = 0;

    /**
     * Creates a philosopher with a name and two shared forks.
     *
     * @param name      name of the philosopher
     * @param leftFork  fork on the left side
     * @param rightFork fork on the right side
     */
    public Philosopher(String name, Fork leftFork, Fork rightFork) {
        this.name = name;
        this.leftFork = leftFork;
        this.rightFork = rightFork;
    }

    /**
     * Entry point for the philosopher task.
     *
     * Each philosopher repeats the think-and-eat process three times.
     */
    @Override
    public void run() {

        for (int i = 0; i < 3; i++) {

            try {
                think();
                eat();

            } catch (InterruptedException e) {

                // Restore the interrupted status of the current thread.
                Thread.currentThread().interrupt();

                // Stop this philosopher's task.
                return;
            }
        }
    }

    /**
     * Simulates the philosopher thinking.
     */
    private void think() throws InterruptedException {

        System.out.println(
                " " + name + " is thinking..."
        );

        // Pause for 500 milliseconds to simulate thinking.
        Thread.sleep(500);
    }

    /**
     * Tries to acquire both forks and eat.
     *
     * Timed tryLock() is used instead of lock() so that a philosopher
     * does not wait forever for a fork.
     */
    private void eat() throws InterruptedException {

        /*
         * Try to acquire the left fork.
         *
         * The philosopher waits for a maximum of one second.
         * If the fork is unavailable after one second, the attempt fails.
         */
        if (leftFork.getLock().tryLock(1, TimeUnit.SECONDS)) {

            try {

                System.out.println(
                        "🍴 " + name +
                        " picked up Fork " +
                        leftFork.getId()
                );

                /*
                 * After obtaining the left fork,
                 * try to acquire the right fork.
                 */
                if (rightFork.getLock().tryLock(1, TimeUnit.SECONDS)) {

                    try {

                        System.out.println(
                                "🍴 " + name +
                                " picked up Fork " +
                                rightFork.getId()
                        );

                        /*
                         * Both forks have been acquired successfully,
                         * so the philosopher can now eat.
                         */
                        eatCount++;

                        System.out.println(
                                " " + name +
                                " is EATING | Meal #" +
                                eatCount
                        );

                        // Simulate time spent eating.
                        Thread.sleep(500);

                    } finally {

                        /*
                         * Always release the right fork,
                         * even if an exception occurs while eating.
                         */
                        rightFork.getLock().unlock();

                        System.out.println(
                                "  " + name +
                                " released Fork " +
                                rightFork.getId()
                        );
                    }

                } else {

                    /*
                     * Right fork could not be acquired.
                     * The philosopher will leave this block and
                     * release the already-held left fork.
                     */
                    System.out.println(
                            " " + name +
                            " could not get Fork " +
                            rightFork.getId()
                    );
                }

            } finally {

                /*
                 * Always release the left fork.
                 *
                 * This is especially important when the philosopher
                 * successfully gets the left fork but fails to get
                 * the right fork.
                 */
                leftFork.getLock().unlock();

                System.out.println(
                        "  " + name +
                        " released Fork " +
                        leftFork.getId()
                );
            }

        } else {

            // Left fork could not be acquired within one second.
            System.out.println(
                    name +
                    " could not get Fork " +
                    leftFork.getId()
            );
        }
    }

    /**
     * Returns the philosopher's name.
     *
     * @return philosopher name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns how many times the philosopher successfully ate.
     *
     * @return successful meal count
     */
    public int getEatCount() {
        return eatCount;
    }
}