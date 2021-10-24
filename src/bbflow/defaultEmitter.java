package bbflow;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * default Emitter of bbflow.ff_farm. Implemented various Communication models with workers: ROUNDROBIN, SCATTER, BROADCAST
 * @param <T> Custom type of channels
 */
public class defaultEmitter<T> extends defaultJob<T,T> { // Runnable job
    /**
     * Communications model
     */
    public static final int ROUNDROBIN = 1;
    public static final int SCATTER = 2;
    public static final int BROADCAST = 3;

    int strategy;
    int position = 0;

    public static void preload() {
    }

    public defaultEmitter() {
        this.strategy = ROUNDROBIN;
    }

    /**
     * default constructor
     * @param strategy Emitter communication strategy chosen between ROUNDROBIN, SCATTER and BROADCAST
     */
    public defaultEmitter(int strategy) {
        this.strategy = strategy;
    }

    @Override
    public void runJob() throws InterruptedException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        T received;
        ff_queue<T> in_channel = in.get(0);

        received = in_channel.take();
        if (received == null) { // EOS
            this.strategy = BROADCAST; // EOS sent to everyone
        }

        switch (strategy) {
            case ROUNDROBIN:
                boolean inserted = out.get(position).offer(received);

                if (inserted) {
                    position++;
                    if (position >= out.size()) {
                        position = 0;
                    }
                }

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
                if (received instanceof Collection<?>) {
                    int inputsize = ((Collection<?>) received).size();
                    if (inputsize < out.size()) {
                        throw new ArrayIndexOutOfBoundsException("SCATTER Found a collection with less items than "+out.size()+ " output channels");
                    }

                    int chunksize = inputsize / out.size();
                    if (chunksize*out.size() < inputsize) {
                        chunksize++; // out.size() not a divisor, distribute one element more for each worker
                    }

                    Iterator<?> iterator = ((Collection<Object>) received).iterator();

                    int collection_pos = 0;
                    ArrayList<Object> vector = new ArrayList<Object>();

                    while (iterator.hasNext()) {
                        int outpos = collection_pos/chunksize;

                        vector.add(iterator.next());

                        if (vector.size() == chunksize) {
                            out.get(outpos).put((T) vector);
                            vector = new ArrayList<Object>();
                        }

                        collection_pos++;
                    }
                } else {
                    throw new IllegalArgumentException("SCATTER Expect collections of items");
                }
                break;
            case BROADCAST:
                for (int i = 0; i < out.size(); i++) {
                    if (received == null) {
                        out.get(i).setEOS();
                    } else {
                        out.get(i).put(received);
                    }
                }
                break;
        }

        if (received == null) {
            in.remove(0); // removing input channel, sequence finished
        }
    }
}
