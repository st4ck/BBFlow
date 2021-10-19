package bbflow;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Currency;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * default Job extecuted by the bbflow.ff_node. Should be extended and reimplemented with the custom code in runJob() function
 * check if there are at least one input or output channel
 * For more details see run() function doc
 * @param <T> Custom type of the channels
 */
public class defaultJob<T,U> implements Runnable {
    public LinkedList<ff_queue<T>> in = new LinkedList<>();
    public LinkedList<ff_queue<U>> out = new LinkedList<>();

    public static final int CUSTOM_FUNCTION = 1;
    public static final int INLINE = 2;
    public static final int INLINE_MULTI = 3;

    public int runType = CUSTOM_FUNCTION;
    public int id = -1;
    public int position = 0;

    public defaultJob combined = null;
    public int combined_side;

    public defaultJob() {

    }

    public defaultJob(int id) {
        this.id = id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void init() { }

    public static defaultJob uniqueJob(defaultJob obj) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            oos.close();
            bos.close();
            byte[] byteData = bos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(byteData);
            return (defaultJob) new ObjectInputStream(bais).readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static defaultJob uniqueJob(defaultJob obj, int id) {
        defaultJob r = uniqueJob(obj);
        r.id = id;
        return r;
    }


    int sendpos = 0;
    public void sendOut(U element) {
        if (combined == null) {
            if (out.size() > 0) {
                if (sendpos >= out.size()) {
                    sendpos = 0;
                }

                out.get(sendpos).put(element);
                sendpos++;
            }
        } else {
            combined.sendOut(element, combined_side);
        }
    }

    public void sendOut(U element, int combined_side) {

    }

    public void sendOutToAll(U element) {
        if (combined == null) {
            for (int i = 0; i < out.size(); i++) {
                out.get(i).put(element);
            }
        } else {
            combined.sendOut(element, combined_side);
        }
    }

    public void sendOutTo(U element, int index) {
        if (combined == null) {
            if (index >= out.size()) { return; }
            out.get(index).put(element);
        } else {
            combined.sendOut(element, combined_side);
        }
    }

    public void sendEOS() {
        if (combined == null) {
            for (int i=0; i<out.size(); i++) {
                out.get(i).setEOS();
            }
        } else {
            combined.sendEOS(combined_side);
        }
    }

    public void sendEOS(int combined_side) {

    }

    /**
     * default Runnable run method
     * This method runs only if there are at least 1 input channel and 1 output channel
     * Regarding output channel: if there isn't any node on the other side, just don't send anything in the output channel in runJob
     */
    @Override
    public void run() {
        init();

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
                if (runType == CUSTOM_FUNCTION) {
                    runJob();
                } else if (runType == INLINE) {
                    T received;
                    ff_queue<T> in_channel = in.get(0);
                    ff_queue<U> out_channel = out.get(0);

                    while (true) {
                        received = in_channel.take();
                        if (received == null) { // EOS
                            in.remove(0); // removing input channel, sequence finished
                            EOS();
                            out_channel.setEOS();
                            break;
                        } else {
                            U r = runJob(received);
                            if (r != null) {
                                if (combined == null) {
                                    out_channel.put(r);
                                } else {
                                    combined.sendOut(r, combined_side);
                                }
                            }
                        }
                        break;
                    }
                } else if (runType == INLINE_MULTI) {
                    T received = in.get(position).take();
                    if (received == null) {
                        in.remove(position); // input channel not needed anymore
                        position--;

                        if (in.size() == 0) { // no more input channels, EOS only last time
                            EOS();

                            for (int i=0; i<out.size(); i++) {
                                out.get(i).setEOS();
                            }
                        }
                    } else {
                        runJobMulti(received, out);
                    }

                    position++;
                    if (position >= in.size()) {
                        position = 0;
                    }
                }
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

    public U runJob(T element) throws InterruptedException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        return null;
    }

    public void runJobMulti(T element, LinkedList<ff_queue<U>> channels_output) {

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
    public void addOutputChannel(ff_queue<U> output)
    {
        out.add(output);
    }

    public boolean removeInputChannel(int index) {
        try {
            in.remove(index);
            return true;
        } catch (IndexOutOfBoundsException x) {
            return false;
        }
    }

    public boolean removeOutputChannel(int index) {
        try {
            out.remove(index);
            return true;
        } catch (IndexOutOfBoundsException x) {
            return false;
        }
    }

    public ff_queue<U> getOutputChannel(int index) {
        try {
            return out.get(index);
        } catch (IndexOutOfBoundsException x) {
            return null;
        }
    }

    public ff_queue<T> getInputChannel(int index) {
        try {
            return in.get(index);
        } catch (IndexOutOfBoundsException x) {
            return null;
        }
    }

    public void EOS() {

    }
}
