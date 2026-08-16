import java.util.concurrent.Semaphore;

/**
 * Represents the library's shared reading area.
 *
 * A Semaphore is used to control how many students
 * are allowed to use the reading area at the same time.
 */
public class Library {

    // Controls the number of students that can read concurrently.
    private final Semaphore readingSlots;

    /**
     * Creates a Library with a fixed number of reading slots.
     *
     * The value true enables fairness, which helps reduce
     * starvation by generally allowing waiting students
     * to acquire permits in order.
     *
     * @param numberOfReadingSlots maximum number of students
     *                             allowed to read at once
     */
    public Library(int numberOfReadingSlots) {

        this.readingSlots =
                new Semaphore(numberOfReadingSlots, true);
    }

    /**
     * Called when a student wants to enter the reading area.
     *
     * acquire() takes one permit.
     * If no permits are available, the student waits until
     * another student leaves and releases one.
     *
     * @throws InterruptedException if the waiting thread is interrupted
     */
    public void enterLibrary() throws InterruptedException {

        readingSlots.acquire();
    }

    /**
     * Called when a student leaves the reading area.
     *
     * release() returns one permit so another waiting
     * student can enter.
     */
    public void leaveLibrary() {

        readingSlots.release();
    }
}