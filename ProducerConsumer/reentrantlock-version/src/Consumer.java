public class Consumer {

    // Same SharedBuffer object used by Producer
    private final SharedBuffer buffer;

    public Consumer(SharedBuffer buffer) {
        this.buffer = buffer;
    }

    public void consume() throws InterruptedException {

        // Consume the same 10 items produced by Producer
        for (int i = 0; i < 10; i++) {

            // Remove an item from the shared buffer
            buffer.take();
        }
    }
}