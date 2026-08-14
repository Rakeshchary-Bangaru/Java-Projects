/**
 * Producer-Consumer Problem using Java Synchronization
 *
 * PROBLEM:
 * The Producer-Consumer problem is a multithreading problem where one thread
 * (Producer) generates data and another thread (Consumer) processes/removes it.
 *
 * Both threads share the same buffer, which creates synchronization challenges:
 *
 * 1. The Producer should not add items when the buffer is full.
 * 2. The Consumer should not remove items when the buffer is empty.
 * 3. Producer and Consumer should not modify the shared LinkedList
 *    simultaneously in an unsafe way.
 *
 *
 * SOLUTION:

 * A buffer capacity of 3 is used, meaning the buffer can hold a maximum
 * of three items at a time.
 *
 * The Producer generates 10 items  and inserts them into the buffer.
 * The Consumer removes those 10 items from the buffer.
 *
 *
 * SYNCHRONIZATION:
 * Both Producer and Consumer access the same LinkedList, so access to the
 * buffer is protected using synchronized(list).
 *
 * Producer:
 *
 *     If buffer is FULL
 *          -> wait()
 *
 *     Otherwise
 *          -> add item
 *          -> notifyAll()
 *
 *
 * Consumer:
 *
 *     If buffer is EMPTY
 *          -> wait()
 *
 *     Otherwise
 *          -> remove item
 *          -> notifyAll()
 *
 *
 * wait() temporarily releases the object's monitor lock and puts the
 * current thread into the waiting state.
 *
 * notifyAll() wakes waiting threads so they can recheck their conditions
 * and compete to acquire the lock again.
 *
 *
 * WHY WHILE INSTEAD OF IF:
 * The condition is checked using a while loop because a thread must
 * recheck the buffer state after waking up. Another thread may have
 * changed the buffer before the awakened thread reacquires the lock.
 *
 * CONCEPTS DEMONSTRATED:
 * - Java Threads
 * - Producer-Consumer Pattern
 * - Shared Resources
 * - LinkedList as a bounded buffer
 * - synchronized blocks
 * - Object monitor locks
 * - wait()
 * - notifyAll()
 * - Race-condition prevention
 * - Thread coordination
 */

public class Main {

    public static void main(String[] args) {

        // Create one shared buffer with a maximum capacity of 3 items.
        SharedBuffer buffer = new SharedBuffer(3);

        // Producer and Consumer receive the same SharedBuffer object.
        Producer producer = new Producer(buffer);
        Consumer consumer = new Consumer(buffer);

        // Producer thread generates 10 items and adds them to the buffer.
        Thread producerThread = new Thread(() -> {
            try {
                producer.produce();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer-Thread");

        // Consumer thread removes the 10 produced items from the buffer.
        Thread consumerThread = new Thread(() -> {
            try {
                consumer.consume();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer-Thread");

        // Start Producer and Consumer concurrently.
        producerThread.start();
        consumerThread.start();
    }
}

/*
 *
 * CHALLENGES FACED:
 *
 * 1. Synchronizing on the correct shared object
 * ------------------------------------------------
 * Initially, synchronization can be confusing because every Java object
 * can have its own monitor lock.
 *
 * If Producer synchronizes on one object and Consumer synchronizes on
 * another object, they do not protect the shared resource from each other.
 *
 * Both operations therefore synchronize on the SAME LinkedList object:
 *
 *     synchronized(list)
 *
 * This ensures that only one thread at a time modifies the shared buffer.
 * It also avoids unnecessary synchronization on unrelated objects.
 *
 *
 * 2. Correct usage of wait() and notifyAll()
 * -------------------------------------------
 * wait() and notifyAll() must be called while the thread owns the monitor
 * of the same object.
 *
 * Because the code uses:
 *
 *     synchronized(list)
 *
 * it must use:
 *
 *     list.wait();
 *     list.notifyAll();
 *
 * Calling wait() on a different object would result in an
 * IllegalMonitorStateException.
 *
 * Another challenge is avoiding unnecessary lock contention. Synchronizing
 * too much code can reduce concurrency because other threads must wait for
 * the same lock. Therefore, only the operations involving the shared buffer
 * are placed inside the synchronized section.
 *
 *
*/