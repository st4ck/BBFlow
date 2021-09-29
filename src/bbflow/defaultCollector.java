package bbflow;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * default Collector of bbflow.ff_farm. Can be extended and runJob() overwritten
 * @param <T> Custom type of channels
 */
public class defaultCollector<T> extends defaultJob<T> {
    public static final int FIRSTCOME = 1;
    public static final int ROUNDROBIN = 2;
    public static final int GATHER = 3;

    int strategy;
    int position = 0;

    /**
     * default constructor
     * @param strategy Collector communication strategy chosen between FIRSTCOME, GATHER and ALLGATHER
     * @param EOF EOF symbol
     */
    public defaultCollector(int strategy, T EOF) {
        this.strategy = strategy;
        this.EOF = EOF;
    }

    @Override
    public void runJob() throws InterruptedException {
        T received;
        LinkedBlockingQueue<T> out_channel = out.get(0);

        switch (strategy) {
            case FIRSTCOME:
                received = in.get(position).poll(50, TimeUnit.MILLISECONDS);
                if (received != null) {
                    if (received == EOF) {
                        in.remove(position); // input channel not needed anymore
                        position--; // stepping back of 1 position because next there's the increment to the next position that now has the same index

                        if (in.size() == 0) { // no more input channels, EOF only last time
                            out_channel.put(EOF);
                        }
                    } else {
                        out_channel.put(received);
                    }
                }

                position++;
                if (position >= in.size()) {
                    position = 0;
                }
                break;
            case ROUNDROBIN:
                received = in.get(position).take();
                if (received == EOF) {
                    in.remove(position); // input channel not needed anymore
                    position--;

                    if (in.size() == 0) { // no more input channels, EOF only last time
                        out_channel.put(EOF);
                    }
                } else {
                    out_channel.put(received);
                }

                position++;
                if (position >= in.size()) {
                    position = 0;
                }
                break;
            case GATHER:
                if (in.size() < out_channel.remainingCapacity()) {
                    for (int i=0; i<in.size(); i++) {
                        received = in.get(i).take();

                        if (received == EOF) {
                            in.remove(i); // input channel not needed anymore

                            if (in.size() == 0) { // no more input channels, EOF only last time
                                out_channel.put(EOF);
                            } else {
                                i--; // finish the for with all inputs (now size decremented by 1)
                            }
                        } else {
                            out_channel.put(received);
                        }
                    }
                }
                break;
        }
    }
}
