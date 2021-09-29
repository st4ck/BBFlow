package bbflow;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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

    int strategy;
    int position = 0;

    /**
     * default constructor
     * @param strategy Emitter communication strategy chosen between ROUNDROBIN, SCATTER and BROADCAST
     * @param EOF EOF symbol
     */
    public defaultEmitter(int strategy, T EOF) {
        this.strategy = strategy;
        this.EOF = EOF;
    }

    @Override
    public void runJob() throws InterruptedException {
        T received;
        LinkedBlockingQueue<T> in_channel = in.get(0);

        received = in_channel.take();
        if (received == EOF) {
            this.strategy = BROADCAST; // EOF sent to everyone
        }

        switch (strategy) {
            case ROUNDROBIN:
                boolean inserted = out.get(position).offer(received);
                while (!inserted) {
                    // insertion failed, buffer full. Try cyclically all workers
                    for (int i = 0; i < out.size(); i++) {
                        position++;
                        if (position >= out.size()) {
                            position = 0;
                        }

                        inserted = out.get(position).offer(received, 50, TimeUnit.MILLISECONDS);
                        if (inserted) { // found a free worker
                            break;
                        }
                    }
                }
                break;
            case SCATTER:
                if ((in_channel.size()+1) >= out.size()) {
                        /*
                        this works even if elements in input channel are less than output channels
                        necessary to avoid starvation if the elements are not multiple of output channels
                         */
                    for (int i = 0; i < Math.min((in_channel.size()+1), out.size()); i++) {
                        if (i > 0) { // first element already retrieved
                            received = in_channel.take();
                        }
                        out.get(i).put(received);
                    }
                }
                break;
            case BROADCAST:
                for (int i = 0; i < out.size(); i++) {
                    out.get(i).put(received);
                }
                break;
        }

        if (received == EOF) {
            in.remove(0); // removing input channel, sequence finished
        }
    }
}
