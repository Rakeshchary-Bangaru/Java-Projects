import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Student Library Problem
 *
 * Problem:
 * --------
 * Multiple students want to read books from a library at the same time.
 * However, the library has limited reading slots and each book can only
 * be read by one student at a time.
 *
 * If access to these shared resources is not controlled properly,
 * multiple students may try to use the same resource at the same time,
 * causing concurrency problems.
 *
 *
 * Solution:
 * ---------
 * 1. A Semaphore is used in the Library class to limit the number
 *    of students allowed in the reading area at the same time.
 *
 * 2. Each Book has its own fair ReentrantLock so that only one
 *    student can read a particular book at a time.
 *
 * 3. Students are implemented as Runnable tasks and executed using
 *    a fixed thread pool.
 *
 * 4. tryLock() with a timeout is used so that a student does not
 *    wait forever for a book.
 *
 * 5. Fair Semaphore and fair ReentrantLock are used to help reduce
 *    the possibility of starvation.
 */
public class Main {

    public static void main(String[] args)
            throws InterruptedException {

        // Total number of students.
        int numberOfStudents = 10;

        /*
         * Create a library with 3 reading slots.
         *
         * Therefore, only 3 students can use the
         * reading area at the same time.
         */
        Library library = new Library(3);

        /*
         * Create shared Book objects.
         *
         * Each Book has its own ReentrantLock.
         */
        Book[] books = {
                new Book(1, "Java Programming"),
                new Book(2, "Database Systems"),
                new Book(3, "Operating Systems"),
                new Book(4, "Computer Networks"),
                new Book(5, "Software Engineering")
        };

        /*
         * Create a fixed thread pool with 5 worker threads.
         *
         * These worker threads execute Student Runnable tasks.
         */
        ExecutorService executor =
                Executors.newFixedThreadPool(5);

        System.out.println();

        System.out.println(
                "======================================"
        );

        System.out.println(
                "          STUDENT LIBRARY"
        );

        System.out.println(
                "======================================"
        );

        /*
         * Create students and assign books to them.
         */
        for (int i = 0; i < numberOfStudents; i++) {

            /*
             * Modulo (%) allows books to be reused.
             *
             * Example:
             *
             * Student-1 -> Book-1
             * Student-2 -> Book-2
             * ...
             * Student-5 -> Book-5
             * Student-6 -> Book-1
             */
            Book selectedBook =
                    books[i % books.length];

            // Create a Student task.
            Student student =
                    new Student(
                            "Student-" + (i + 1),
                            library,
                            selectedBook
                    );

            /*
             * Submit the student to the thread pool.
             *
             * A worker thread will execute student.run().
             */
            executor.submit(student);
        }

        /*
         * Stop accepting new tasks.
         *
         * Already submitted tasks will continue running.
         */
        executor.shutdown();

        /*
         * Make the main thread wait for all submitted
         * student tasks to finish.
         */
        executor.awaitTermination(
                1,
                TimeUnit.MINUTES
        );

        System.out.println(
                "======================================"
        );

        System.out.println(
                "All students finished."
        );
    }

    /*
     * Challenges Faced:
     *
     * 1. Controlling Shared Resources:
     *    Multiple students may try to use the reading area and books
     *    at the same time. A Semaphore was used to limit the number
     *    of readers, while individual book locks prevent two students
     *    from reading the same book simultaneously.
     *
     * 2. Preventing Starvation and Indefinite Waiting:
     *    A student could keep waiting while other students repeatedly
     *    get access to the resources. Fair Semaphore/ReentrantLock
     *    were used to reduce starvation, and timed tryLock() prevents
     *    a student from waiting indefinitely for a book.
     */
} 