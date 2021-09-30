package bbflow;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
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
    public void runJob() throws InterruptedException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
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
                    ArrayList<Object> newcollection = new ArrayList<Object>();

                    while (iterator.hasNext()) {
                        int outpos = collection_pos/chunksize;

                        newcollection.add(iterator.next());

                        if (newcollection.size() == chunksize) {
                            out.get(outpos).put((T) newcollection);
                            newcollection = new ArrayList<Object>();
                        }

                        collection_pos++;
                    }
                } else {
                    throw new IllegalArgumentException("SCATTER Expect collections of items");
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
