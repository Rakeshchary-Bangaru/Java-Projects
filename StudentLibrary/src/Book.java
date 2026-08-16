import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a book in the Student Library problem.
 *
 * Each Book is a shared resource that can be requested
 * by multiple students.
 *
 * Every book has its own ReentrantLock so that only
 * one student can read a particular book at a time.
 */
public class Book {

    // Unique identifier for the book.
    private final int id;

    // Title/name of the book.
    private final String title;

    // Lock used to control access to this particular book.
    private final Lock lock;

    /**
     * Creates a new Book object.
     *
     * A fair ReentrantLock is created for each book.
     * The value true enables fairness, which helps reduce
     * starvation among students waiting for the same book.
     *
     * @param id    unique ID of the book
     * @param title title of the book
     */
    public Book(int id, String title) {
        this.id = id;
        this.title = title;

        // Each book gets its own fair lock.
        this.lock = new ReentrantLock(true);
    }

    /**
     * Returns the unique ID of the book.
     *
     * @return book ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the title of the book.
     *
     * @return book title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the lock associated with this book.
     *
     * Students use this lock to acquire and release
     * access to the book.
     *
     * @return lock belonging to this book
     */
    public Lock getLock() {
        return lock;
    }
}