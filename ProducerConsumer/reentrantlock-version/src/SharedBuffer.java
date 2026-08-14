import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SharedBuffer {

    // Shared buffer accessed by both Producer and Consumer threads
    private final LinkedList<Integer> list;

    // Maximum number of items that can be stored in the buffer
    private final int capacity;

    // Explicit lock used to protect the shared LinkedList
    private final Lock lock;

    /*
     * Condition used when the buffer is FULL.
     * Producer waits on this condition.
     */
    private final Condition full;

    /*
     * Condition used when the buffer is EMPTY.
     * Consumer waits on this condition.
     */
    private final Condition empty;


    public SharedBuffer(int capacity) {

        this.list = new LinkedList<>();
        this.capacity = capacity;

        // Create one ReentrantLock shared by put() and take()
        this.lock = new ReentrantLock();

        /*
         * Create two separate waiting conditions using the same lock.
         *
         * full  -> Producer waits when buffer is full.
         * empty -> Consumer waits when buffer is empty.
         */
        this.full = lock.newCondition();
        this.empty = lock.newCondition();
    }


    // Producer uses this method to insert an item into the buffer
    public void put(int item) throws InterruptedException {

        // Producer acquires the lock before accessing the shared buffer
        lock.lock();

        try {

            /*
             * If the buffer is FULL, Producer cannot add another item.
             *
             * await() makes the Producer wait and temporarily
             * releases the ReentrantLock.
             *
             * We use while instead of if because the condition
             * must be checked again after the thread wakes up.
             */
            while (list.size() == capacity) {
                full.await();
            }

            // Buffer has space, so Producer can add the item
            list.add(item);

            System.out.println(
                Thread.currentThread().getName()
                + " produced: " + item
                + " | Buffer: " + list
            );

            /*
             * An item has been added.
             *
             * If Consumer was waiting because the buffer was empty,
             * wake one waiting Consumer.
             */
            empty.signal();

        } finally {

            /*
             * Always release the lock.
             *
             * finally ensures unlock() executes even if
             * an exception occurs inside the try block.
             */
            lock.unlock();
        }
    }


    // Consumer uses this method to remove an item from the buffer
    public int take() throws InterruptedException {

        // Consumer acquires the SAME lock
        lock.lock();

        try {

            /*
             * If the buffer is EMPTY, there is nothing to consume.
             *
             * Consumer waits and temporarily releases the lock.
             */
            while (list.isEmpty()) {
                empty.await();
            }

            // An item is available, so Consumer removes the first item
            int item = list.removeFirst();

            System.out.println(
                Thread.currentThread().getName()
                + " consumed: " + item
                + " | Buffer: " + list
            );

            /*
             * Consumer removed an item.
             *
             * The buffer now has space.
             * Wake one Producer that may be waiting because
             * the buffer was full.
             */
            full.signal();

            return item;

        } finally {

            // Always release the lock
            lock.unlock();
        }
    }
}