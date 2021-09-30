package bbflow;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Fundamental building block rapresenting a node
 * Implemented and used by other building blocks
 * Support multiple input and output channels
 * Single lock for all input channels and a single lock for all output channels
 * @param <T> Custom type of the channels
 */
public class ff_node<T> extends Thread {
    defaultJob<T> job;

    /**
     * default constructor
     * @param job generic Runnable of type default Job
     *            extending Runnable
     */
    public ff_node(defaultJob<T> job) {
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

    public void setBufferSize(int bufferSize) {
        job.bufferSize = bufferSize;
    }
}
