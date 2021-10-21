package bbflow;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Fundamental building block rapresenting a node
 * Implemented and used by other building blocks
 * Support multiple input and output channels
 * Single lock for all input channels and a single lock for all output channels
 * @param <T> Custom type of the channels
 */
public class ff_node<T,U> extends block<T,U> {
    public node mynode;

    /**
     * default constructor for different blocks extending ff_node
     */
    public ff_node() {}

    /**
     * default constructor
     * @param job generic Runnable of type default Job
     *            extending Runnable
     */
    public ff_node(defaultJob<T,U> job) {
        if (job.getClass().isAnonymousClass()) {
            job.runType = defaultJob.INLINE;

            Object[] methods = Arrays.stream(job.getClass().getMethods()).toArray();
            for (int i=0; i<methods.length; i++) {
                if (((Method)methods[i]).getName() == "runJobMulti") {
                    if (((Method)methods[i]).getDeclaringClass().getName() != "bbflow.defaultJob") { // overridden method by user
                        job.runType = defaultJob.INLINE_MULTI;
                        break;
                    }
                }
            }
        }
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
        if (!mynode.isAlive()) {
            mynode.start();
        }
    }

    /**
     * default join function
     */
    public void join() {
        try {
            mynode.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * get output channel
     * @param index get output channel at index. Indexes starting from 0
     * @return return channel retrieved
     */
    public ff_queue<U> getOutputChannel(int index) {
        return mynode.getOutputChannel(index);
    }

    /**
     * get input channel
     * @param index get input channel at index. Indexes starting from 0
     * @return return channel retrieved
     */
    public ff_queue<T> getInputChannel(int index) {
        return mynode.getInputChannel(index);
    }
}
