package bbflow;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * default Job extecuted by the bbflow.ff_node. Should be extended and reimplemented with the custom code in runJob() function
 * check if there are at least one input or output channel
 * For more details see run() function doc
 * @param <T> Custom type of the channels
 */
public class defaultJob<T> implements Runnable {
    public LinkedList<ff_queue<T>> in;
    public LinkedList<ff_queue<T>> out;

    public int id = -1;

    public defaultJob() {
        in = new LinkedList<>();
        out = new LinkedList<>();
    }

    /**
     * default Runnable run method
     * This method runs only if there are at least 1 input channel and 1 output channel
     * Regarding output channel: if there isn't any node on the other side, just don't send anything in the output channel in runJob
     */
    @Override
    public void run() {
        if (in.size() == 0) { return; } // no input channels
        if (out.size() == 0) { return; } // no output channels

        while (true) {
            if (in.size() == 0) {
                return;
            } // no input channels anymore
            if (out.size() == 0) {
                return;
            } // no output channels anymore

            // elements available
            try {
                runJob();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * blank function that should be overwritten by class extending bbflow.defaultJob.
     * Here main computation task is done once we're sure there's data in at least one of the input channels
     */
    public void runJob() throws InterruptedException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

    }

    /**
     * add new input channel to the Runnable node
     * @param input input channel
     */
    public void addInputChannel(ff_queue<T> input) {
        in.add(input);
    }

    /**
     *  add new output channel to the Runnable node
     * @param output output channel
     */
    public void addOutputChannel(ff_queue<T> output)
    {
        out.add(output);
    }
}
