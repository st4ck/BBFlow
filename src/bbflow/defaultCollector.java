package bbflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * default Collector of bbflow.ff_farm. Can be extended and runJob() overwritten
 * @param <T> Custom type of channels
 */
public class defaultCollector<T> extends defaultJob<T,T> {
    public static final int FIRSTCOME = 1;
    public static final int ROUNDROBIN = 2;
    public static final int GATHER = 3;

    int strategy;
    int position = 0;

    public defaultCollector() {
        this.strategy = FIRSTCOME;
    }

    /**
     * default constructor
     * @param strategy Collector communication strategy chosen between FIRSTCOME, GATHER and ALLGATHER
     */
    public defaultCollector(int strategy) {
        this.strategy = strategy;
    }

    @Override
    public void runJob() throws InterruptedException {
        T received = null;
        ff_queue<T> out_channel = out.get(0);

        switch (strategy) {
            case FIRSTCOME:
                received = in.get(position).poll(50, TimeUnit.MILLISECONDS);
                if ((received == null) && in.get(position).getEOS()) {
                    in.remove(position); // input channel not needed anymore
                    position--; // stepping back of 1 position because next there's the increment to the next position that now has the same index

                    if (in.size() == 0) { // no more input channels, EOS only last time
                        out_channel.setEOS();
                    }
                } else if (received != null) {
                    out_channel.put(received);
                }

                position++;
                if (position >= in.size()) {
                    position = 0;
                }
                break;
            case ROUNDROBIN:
                received = in.get(position).take();
                if (received == null) {
                    in.remove(position); // input channel not needed anymore
                    position--;

                    if (in.size() == 0) { // no more input channels, EOS only last time
                        out_channel.setEOS();
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
                // retrieve one element (possibly Collection<?>) per channel
                ArrayList<Object> vector = new ArrayList<Object>();

                for (int i=0; i<in.size(); i++) {
                    received = in.get(i).take();

                    if (received == null) {
                        in.remove(i); // input channel not needed anymore
                        i--;

                        if (in.size() == 0) { // no more input channels, EOS only last time
                            out_channel.setEOS();
                        }

                        continue;
                    }

                    if (received instanceof Collection<?>) { // collection type, SCATTERED by Emitter
                        Iterator<?> iterator = ((Collection<Object>) received).iterator();

                        while (iterator.hasNext()) {
                            vector.add(iterator.next());
                        }
                    } else {
                        vector.add(received);
                    }
                }

                if (received != null) { // not EOS reached
                    out_channel.put((T) vector);
                }
                break;
        }
    }
}
