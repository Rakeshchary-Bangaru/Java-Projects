import java.util.concurrent.TimeUnit;

/**
 * Represents a student in the Student Library problem.
 *
 * Each Student is a Runnable task that can be executed
 * by a thread from the ExecutorService thread pool.
 *
 * The student:
 * 1. Enters the reading area using a Semaphore permit.
 * 2. Tries to acquire the selected book's lock.
 * 3. Reads the book if the lock is acquired.
 * 4. Releases the book after reading.
 * 5. Leaves the reading area and returns the Semaphore permit.
 */
public class Student implements Runnable {

    // Name used to identify the student.
    private final String name;

    // Shared Library object containing the Semaphore.
    private final Library library;

    // Book that this student wants to read.
    private final Book book;

    /**
     * Creates a Student with a name, library, and selected book.
     *
     * @param name    student name
     * @param library shared library
     * @param book    book the student wants to read
     */
    public Student(String name, Library library, Book book) {
        this.name = name;
        this.library = library;
        this.book = book;
    }

    /**
     * Contains the work performed by the student thread.
     */
    @Override
    public void run() {

        try {

            /*
             * Acquire a Semaphore permit before entering
             * the reading area.
             *
             * If all reading slots are occupied,
             * the student waits here.
             */
            library.enterLibrary();

            System.out.println(
                    name + " entered the reading area."
            );

            try {

                /*
                 * Try to acquire the selected book's lock.
                 *
                 * The student waits for a maximum of 1 second.
                 * If the lock is still unavailable, tryLock()
                 * returns false instead of waiting forever.
                 */
                if (book.getLock().tryLock(
                        1,
                        TimeUnit.SECONDS)) {

                    try {

                        System.out.println(
                                name + " is reading " +
                                book.getTitle()
                        );

                        /*
                         * Simulate the time spent reading.
                         *
                         * sleep() pauses the current student thread
                         * for 1 second while it still holds the book lock.
                         */
                        Thread.sleep(1000);

                        System.out.println(
                                name + " finished reading " +
                                book.getTitle()
                        );

                    } finally {

                        /*
                         * Always release the book lock after reading.
                         *
                         * finally ensures the book is released even
                         * if an exception occurs.
                         */
                        book.getLock().unlock();

                        System.out.println(
                                name + " released " +
                                book.getTitle()
                        );
                    }

                } else {

                    /*
                     * The book could not be acquired within
                     * the 1-second timeout.
                     */
                    System.out.println(
                            name + " could not get " +
                            book.getTitle()
                    );
                }

            } finally {

                /*
                 * Return the Semaphore permit when the student
                 * leaves the reading area.
                 *
                 * This happens whether the student successfully
                 * reads the book or not.
                 */
                library.leaveLibrary();

                System.out.println(
                        name + " left the reading area."
                );
            }

        } catch (InterruptedException e) {

            /*
             * Restore the interrupted status because methods like
             * acquire(), tryLock(timeout), and sleep() can be interrupted.
             */
            Thread.currentThread().interrupt();
        }
    }
}