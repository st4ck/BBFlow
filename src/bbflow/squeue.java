package bbflow;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Reimplementation of ConcurrentLinkedQueue in Bounded manner
 * Taken inspiration from Hadoop class BoundedConcurrentLinkedQueue
 * @param <T> Type of the elements in the queue
 */
public class squeue<T> extends ConcurrentLinkedQueue<T> {
    private final AtomicLong size = new AtomicLong(0L);
    private final long maxSize;

    public squeue() {
        this(Long.MAX_VALUE);
    }

    public squeue(long maxSize) {
        super();
        this.maxSize = maxSize;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        while(true) {
            long currentSize = size.get();
            long nextSize = currentSize + c.size();
            if (nextSize > maxSize) { // already exceeded limit
                return false;
            }
            if (size.compareAndSet(currentSize, nextSize)) {
                break;
            }
        }
        return super.addAll(c); // Always true for ConcurrentLinkedQueue
    }

    @Override
    public void clear() {
        // override this method to batch update size.
        long removed = 0L;
        while (super.poll() != null) {
            removed++;
        }
        size.addAndGet(-removed);
    }

    @Override
    public boolean offer(T e) {
        while(true) {
            long currentSize = size.get();
            if (currentSize >= maxSize) { // already exceeded limit
                return false;
            }
            if (size.compareAndSet(currentSize, currentSize + 1)) {
                break;
            }
        }
        return super.offer(e); // Always true for ConcurrentLinkedQueue
    }

    @Override
    public T poll() {
        T result = super.poll();
        if (result != null) {
            size.decrementAndGet();
        }
        return result;
    }

    @Override
    public boolean remove(Object o) {
        boolean result = super.remove(o);
        if (result) {
            size.decrementAndGet();
        }
        return result;
    }

    @Override
    public int size() {
        return (int) size.get();
    }

    public long remainingCapacity() {
        return maxSize - size.get();
    }
}