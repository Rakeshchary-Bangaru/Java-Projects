/**
 * Producer-Consumer Problem using ReentrantLock and Condition
 *
 * PROBLEM:
 * The Producer-Consumer problem involves multiple threads sharing
 * a common buffer.
 *
 * The Producer generates data and inserts it into the buffer.
 * The Consumer removes and processes data from the buffer.
 *
 * We need to make sure:
 *
 * 1. Producer does not insert when the buffer is FULL.
 * 2. Consumer does not remove when the buffer is EMPTY.
 * 3. Producer and Consumer do not modify the LinkedList
 *    simultaneously in an unsafe way.
 *
 *
 * BUFFER:
 *
 * A LinkedList<Integer> is used as the shared buffer.
 *
 * The maximum capacity is 3.
 
 * PRODUCER:
 *
 * Producer generates 10 items:
 *
 *      0, 1, 2, 3 ... 9
 *
 * If the buffer becomes full:
 * Producer waits until Consumer removes an item.
 *
 *
 * CONSUMER:
 *
 * Consumer removes the 10 items produced by Producer.
 *
 * If the buffer becomes empty:
 
 * Consumer waits until Producer adds an item.
 *
 *
 * LOCKING:
 *
 * A ReentrantLock protects the shared LinkedList.
 *
 * Before accessing the buffer:
 *
 *      lock.lock();
 *
 * After accessing the buffer:
 *
 *      lock.unlock();
 *
 * unlock() is placed inside finally so that the lock is
 * released even if an exception occurs.
 *
 *
 * CONDITIONS:
 *
 * Two Condition objects are used:
 *
 *      full
 *          Producer waits here when the buffer is full.
 *
 *      empty
 *          Consumer waits here when the buffer is empty.
 *

 *
 * WHY WHILE INSTEAD OF IF:
 *
 * A waiting thread must check the condition again after waking.
 *
 * Another thread may have modified the buffer before the awakened
 * thread successfully reacquires the lock.
 *
 * CONCEPTS DEMONSTRATED:
 *
 * - Java Threads
 * - Producer-Consumer Pattern
 * - Shared Resources
 * - LinkedList
 * - ReentrantLock
 * - Condition
 * - await()
 * - signal()
 * - Explicit locking
 * - Thread coordination
 * - Bounded buffer
 */

public class Main {

    public static void main(String[] args) {

        // Create one shared buffer that can hold maximum 3 items
        SharedBuffer buffer = new SharedBuffer(3);

        /*
         * Both Producer and Consumer receive the SAME
         * SharedBuffer object.
         */
        Producer producer = new Producer(buffer);
        Consumer consumer = new Consumer(buffer);


        // Create Producer thread
        Thread producerThread = new Thread(() -> {

            try {

                producer.produce();

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
            }

        }, "Producer-Thread");


        // Create Consumer thread
        Thread consumerThread = new Thread(() -> {

            try {

                consumer.consume();

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
            }

        }, "Consumer-Thread");


        // Start both threads
        producerThread.start();
        consumerThread.start();
    }
}

/*
 * CHALLENGES FACED:
 *
 * 1. Understanding explicit locking
 * ----------------------------------
 *
 * Unlike synchronized, ReentrantLock requires us to manually
 * acquire and release the lock.
 *
 *      lock.lock();
 *
 *      try {
 *          // critical section
 *      }
 *      finally {
 *          lock.unlock();
 *      }
 *
 * Forgetting unlock() could cause other threads to wait indefinitely.
 *
 * Using finally ensures the lock is always released.
 *
 *
 * 2. Understanding await() and signal()
 * --------------------------------------
 *
 * Producer and Consumer wait for different buffer conditions.
 *
 * Producer waits when:
 *
 *      buffer is FULL
 *
 * using:
 *
 *      full.await();
 *
 *
 * Consumer waits when:
 *
 *      buffer is EMPTY
 *
 * using:
 *
 *      empty.await();
 *
 *
 * When Consumer removes an item:
 *
 *      full.signal();
 *
 * tells a waiting Producer that space may now be available.
 *
 *
 * When Producer adds an item:
 *
 *      empty.signal();
 *
 * tells a waiting Consumer that data may now be available.
 *
*/