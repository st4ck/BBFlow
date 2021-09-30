package bbflow;

/**
 * Thread running the job for ff_node building block
 * @param <T> Custom type of the channels
 */
public class node<T> extends Thread {
    defaultJob<T> job;
    public node(defaultJob<T> job) {
        this.job = job;
    }

    public void run() {
        // run the method
        job.run();
    }

    /**
     * add a Input channel to the bbflow.ff_node. LinkedList is O(1) adding/removing first element
     * @param input input channel
     */
    public void addInputChannel(ff_queue<T> input) {
        job.addInputChannel(input);
    }

    /**
     * add a Output channel to the bbflow.ff_node
     * LinkedList is O(1) adding/removing first element
     * @param output output channel
     */
    public void addOutputChannel(ff_queue<T> output) {
        job.addOutputChannel(output);
    }

    public boolean removeInputChannel(int index) {
        return job.removeInputChannel(index);
    }

    public boolean removeOutputChannel(int index) {
        return job.removeInputChannel(index);
    }

    public ff_queue<T> getOutputChannel(int index) {
        return job.getOutputChannel(index);
    }

    public ff_queue<T> getInputChannel(int index) {
        return job.getInputChannel(index);
    }
}
