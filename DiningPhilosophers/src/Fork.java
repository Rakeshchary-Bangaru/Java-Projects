import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a fork in the Dining Philosophers problem.
 *
 * Each Fork object is a shared resource between two neighbouring philosophers.
 * A ReentrantLock is used so that only one philosopher can use the fork
 * at a time.
 */
public class Fork {

    // Unique number used to identify the fork.
    private final int id;

    // Lock that controls access to this fork.
    private final Lock lock;

    /**
     * Creates a new Fork with the given ID.
     *
     * Each fork gets its own separate ReentrantLock.
     *
     * @param id unique identifier of the fork
     */
    public Fork(int id) {
        this.id = id;
        this.lock = new ReentrantLock();
    }

    /**
     * Returns the ID of the fork.
     *
     * @return fork ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the Lock associated with this fork.
     *
     * Philosophers use this lock to try to acquire and release the fork.
     *
     * @return lock belonging to this fork
     */
    public Lock getLock() {
        return lock;
    }
}