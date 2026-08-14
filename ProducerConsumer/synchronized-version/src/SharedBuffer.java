import java.util.LinkedList;

public class SharedBuffer {

    // Shared buffer accessed by both Producer and Consumer threads
    private LinkedList<Integer> list;

    // Maximum number of items allowed in the buffer
    private int capacity;

    public SharedBuffer(int capacity) {
        this.list = new LinkedList<>();
        this.capacity = capacity;
    }

    // Adds an item to the shared buffer
    public void put(int item) throws InterruptedException {

        // Producer and Consumer synchronize on the same list object
        synchronized (list) {

            // Wait while the buffer is full
            while (list.size() == capacity) {
                list.wait();
            }

            list.add(item);

            System.out.println(
                Thread.currentThread().getName()
                + " produced: " + item
                + " | Buffer: " + list
            );

            // Notify waiting threads that the buffer state has changed
            list.notifyAll();
        }
    }

    // Removes and returns an item from the shared buffer
    public int take() throws InterruptedException {

        synchronized (list) {

            // Wait while there are no items available to consume
            while (list.isEmpty()) {
                list.wait();
            }

            int item = list.removeFirst();

            System.out.println(
                Thread.currentThread().getName()
                + " consumed: " + item
                + " | Buffer: " + list
            );

            // Notify waiting threads that space/data may now be available
            list.notifyAll();

            return item;
        }
    }
}