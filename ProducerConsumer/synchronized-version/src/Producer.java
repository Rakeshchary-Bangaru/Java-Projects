public class Producer {

    private SharedBuffer buffer;

    public Producer(SharedBuffer buffer) {
        this.buffer = buffer;
    }

    public void produce() throws InterruptedException {

        // Produce 10 items and place them into the shared buffer
        for (int i = 0; i < 10; i++) {
            buffer.put(i);
        }
    }
}