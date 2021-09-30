package bbflow;

/**
 * the basic entity where all nodes are constructed onto it
 * if a new custom building block is created, all functions MUST be extended
 * @param <T>
 */
public class block<T> {
    public void addInputChannel(ff_queue<T> input) {
    }

    public void addOutputChannel(ff_queue<T> output) {
    }

    public void start() {
    }

    public void join() {
    }
}
