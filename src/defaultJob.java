import java.util.LinkedList;

/**
 * default Job extecuted by the ff_node. Should be extended and reimplemented with the custom code in runJob() function
 * wait data on channels using input lock and notify output nodes using output lock
 * For more details see run() function doc
 * @param <T> Custom type of the channels
 */
public class defaultJob<T> implements Runnable {
    LinkedList<LinkedList<T>> in;
    LinkedList<LinkedList<T>> out;
    Object inputLock = null;
    Object outputLock = null;

    T EOF = null;
    public int id = -1;

    public defaultJob() {
        in = new LinkedList<>();
        out = new LinkedList<>();
    }

    /**
     * default Runnable run method
     * This method runs only if there are at least 1 input channel and 1 output channel
     * Regarding output channel: if there isn't any node on the other side, just don't send anything in the output channel in runJob
     * The input lock object (to be notified of new data) and the output lock object (to notify next node about new data) use by fallback first channel of input or output respectively
     * Normally a lock object must be set from the external to be able to interconnect fundamental blocks (like ff_node or ff_farm) to previous/next nodes
     * All input channels are scanned for elements, if no one present, the function wait on the input lock object for a notification. No active waiting
     */
    @Override
    public void run() {
        if (EOF == null) { return; } // EOF not defined
        if (in.size() == 0) { return; } // no input channels
        if (out.size() == 0) { return; } // no output channels

        if (inputLock == null) { inputLock = in.get(0); }
        if (outputLock == null) { outputLock = out.get(0); }

        while (true) {
            synchronized(inputLock) { // synchronized on the first input channel
                boolean noelements = true;
                while (noelements) {
                    if (in.size() == 0) { return; } // no input channels anymore
                    if (out.size() == 0) { return; } // no output channels anymore

                    for (int i = 0; i < in.size(); i++) { // scan all channels for elements
                        if (in.get(i).size() > 0) {
                            noelements = false;
                            break;
                        }
                    }

                    if (noelements) {
                        try {
                            inputLock.wait(); // no elements available in all input channels. Waiting
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                }

                synchronized (outputLock) {
                    runJob();
                }
            }
        }
    }

    /**
     * blank function that should be overwritten by class extending defaultJob.
     * Here main computation task is done once we're sure there's data in at least one of the input channels
     */
    public void runJob() {

    }

    /**
     * add new input channel to the Runnable node
     * @param input input channel
     */
    public void addInputChannel(LinkedList<T> input) {
        in.add(input);
    }

    /**
     *  add new output channel to the Runnable node
     * @param output output channel
     */
    public void addOutputChannel(LinkedList<T> output) {
        out.add(output);
    }
}
