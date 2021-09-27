import java.util.ArrayList;
import java.util.LinkedList;

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
     * add a Input channel to the ff_node. LinkedList is O(1) adding/removing first element
     * @param input input channel
     */
    public void addInputChannel(LinkedList<T> input) {
        job.addInputChannel(input);
    }

    /**
     * add a Output channel to the ff_node
     * LinkedList is O(1) adding/removing first element
     * @param output output channel
     */
    public void addOutputChannel(LinkedList<T> output) {
        job.addOutputChannel(output);
    }

    /**
     * Lock Object used to wait data from input channels up to EOF
     * @param l Lock Object
     */
    public void setInputLock(Object l) {
        this.job.inputLock = l;
    }

    /**
     * Lock Object used to notify the next node on the output channel
     * @param l Lock Object
     */
    public void setOutputLock(Object l) {
        this.job.outputLock = l;
    }
}
