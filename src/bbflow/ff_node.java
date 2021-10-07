package bbflow;

/**
 * Fundamental building block rapresenting a node
 * Implemented and used by other building blocks
 * Support multiple input and output channels
 * Single lock for all input channels and a single lock for all output channels
 * @param <T> Custom type of the channels
 */
public class ff_node<T,U> extends block<T,U> {
    node mynode;

    /**
     * default constructor
     * @param job generic Runnable of type default Job
     *            extending Runnable
     */
    public ff_node(defaultJob<T,U> job) {
        mynode = new node(job);
    }

    public ff_node(ff_node<T,U> customEmitterR) {
        this.mynode = new node(customEmitterR.mynode.job);
    }

    /**
     * add a Input channel to the bbflow.ff_node. LinkedList is O(1) adding/removing first element
     * @param input input channel
     */
    public void addInputChannel(ff_queue<T> input) {
        mynode.addInputChannel(input);
    }

    /**
     * add a Output channel to the bbflow.ff_node
     * LinkedList is O(1) adding/removing first element
     * @param output output channel
     */
    public void addOutputChannel(ff_queue<U> output) {
        mynode.addOutputChannel(output);
    }

    /**
     * add a Input channel to the bbflow.ff_node. LinkedList is O(1) adding/removing first element
     * @param index index of channel to remove
     * @return
     */
    public boolean removeInputChannel(int index) {
        return mynode.removeInputChannel(index);
    }

    /**
     * add a Output channel to the bbflow.ff_node
     * LinkedList is O(1) adding/removing first element
     * @param index index of channel to remove
     * @return
     */
    public boolean removeOutputChannel(int index) {
        return mynode.removeOutputChannel(index);
    }

    public void start() {
        mynode.start();
    }

    public void join() {
        try {
            mynode.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ff_queue<U> getOutputChannel(int index) {
        return mynode.getOutputChannel(index);
    }

    public ff_queue<T> getInputChannel(int index) {
        return mynode.getInputChannel(index);
    }
}
