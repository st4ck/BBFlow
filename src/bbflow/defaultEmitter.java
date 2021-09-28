package bbflow;

import java.util.LinkedList;

/**
 * default Emitter of bbflow.ff_farm. Implemented various Communication models with workers: ROUNDROBIN, SCATTER, BROADCAST
 * @param <T> Custom type of channels
 */
public class defaultEmitter<T> extends defaultJob<T> { // Runnable job
    /**
     * Communications model
     */
    public static final int ROUNDROBIN = 1;
    public static final int SCATTER = 2;
    public static final int BROADCAST = 3;

    int strategy = ROUNDROBIN;
    int position = 0;
    int buffersize = 2;

    /**
     * default constructor
     * @param strategy Communication strategy chosen between ROUNDROBIN, SCATTER and BROADCAST
     * @param EOF EOF symbol
     */
    public defaultEmitter(int strategy, T EOF) {
        this.strategy = strategy;
        this.EOF = EOF;
    }

    @Override
    public void runJob() {
        T received;
        LinkedList<T> in_channel = in.get(0);

        received = in_channel.get(0);
        if (received == EOF) {
            this.strategy = BROADCAST; // EOF sent to everyone
        }

        switch (strategy) {
            case ROUNDROBIN:
                out.get(position).add(received);
                outputLock.notify();
                position++;
                if (position >= out.size()) {
                    position = 0;
                }
                in_channel.remove(0);
                break;
            case SCATTER:
                if (in_channel.size() >= out.size()) {
                        /*
                        this works even if elements in input channel are less than output channels
                        necessary to avoid starvation if the elements are not multiple of output channels
                         */
                    for (int i = 0; i < Math.min(in_channel.size(), out.size()); i++) {
                        if (i > 0) {
                            received = in_channel.get(i);
                        } // first element already retrieved
                        out.get(i).add(received);
                        outputLock.notify();
                        in_channel.remove(i);
                    }
                }
                break;
            case BROADCAST:
                for (int i = 0; i < out.size(); i++) {
                    out.get(i).add(received);
                    outputLock.notify();
                }
                in_channel.remove(0);
                break;
        }

        if (received == EOF) {
            in.remove(0); // removing input channel, sequence finished
        }
    }
}
