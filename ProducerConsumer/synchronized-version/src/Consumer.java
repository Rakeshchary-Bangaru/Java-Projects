public class Consumer {

    private SharedBuffer buffer;

    public Consumer(SharedBuffer buffer) {
        this.buffer = buffer;
    }

    public void consume() throws InterruptedException {

        // Consume 10 items from the shared buffer
        for (int i = 0; i < 10; i++) {
            buffer.take();
        }
    }
}