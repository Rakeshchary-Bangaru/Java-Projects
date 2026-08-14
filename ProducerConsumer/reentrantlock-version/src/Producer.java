public class Producer {

    // SharedBuffer object shared with the Consumer
    private final SharedBuffer buffer;

    public Producer(SharedBuffer buffer) {
        this.buffer = buffer;
    }

    public void produce() throws InterruptedException {

        // Produce 10 items: 0 to 9
        for (int i = 0; i < 10; i++) {

            // Add each item to the shared buffer
            buffer.put(i);
        }
    }
}