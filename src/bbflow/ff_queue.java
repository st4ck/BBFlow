package bbflow;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Default class of the queues. Queues are channels 1-1 between nodes of type SPSC and FIFO
 * @param <T> Type of the queue elements
 */
public class ff_queue<T> {
    LinkedBlockingQueue<T> blocking_queue;
    ConcurrentLinkedQueue<T> nonblocking_queue;
    squeue<T> nonblocking_bounded_queue;
    boolean EOS = false;

    boolean blocking = false;
    boolean bounded = false;

    /**
     * Default constructor of the queue. Types available are 4:
     * BLOCKING / BOUNDED
     * BLOCKING / UNBOUNDED
     * NON BLOCKING / BOUNDED
     * NON BLOCKING / UNBOUNDED
     * @param blocking BLOCKING = true / NONBLOCKING = false
     * @param bounded BOUNDED = true / UNBOUNDED = false
     * @param bufferSize size of the queue in case it's BOUNDED
     */
    public ff_queue(boolean blocking, boolean bounded, int bufferSize) {
        if (blocking) {
            this.blocking = true;

            if (bounded) {
                this.bounded = true;

                blocking_queue = new LinkedBlockingQueue<>(bufferSize);
            } else {
                blocking_queue = new LinkedBlockingQueue<>();
            }
        } else {
            if (bounded) {
                this.bounded = true;

                nonblocking_bounded_queue = new squeue<>(bufferSize);
            } else {
                nonblocking_queue = new ConcurrentLinkedQueue<>();
            }
        }
    }

    public ff_queue() {
        this(bb_settings.BLOCKING, bb_settings.BOUNDED, bb_settings.defaultBufferSize);
    }

    public ff_queue(boolean blocking) {
        this(blocking, bb_settings.BOUNDED, bb_settings.defaultBufferSize);
    }

    public ff_queue(int bufferSize) {
        this(bb_settings.BLOCKING, bb_settings.BOUNDED, bufferSize);
    }

    /**
     * Inserts the specified element at the tail of this queue, waiting if necessary for space to become available (if bounded)
     * @param i Element to insert
     * @throws InterruptedException
     */
    public void put(T i) throws InterruptedException {
        if (this.EOS) { return; }

        if (blocking) {
            blocking_queue.put(i);
        } else {
            if (bounded) {
                while (true) {
                    if (nonblocking_bounded_queue.offer(i)) {
                        return;
                    }

                    Thread.sleep(bb_settings.backOff);
                }
            } else {
                // no wait needed, unbounded
                nonblocking_queue.add(i);
            }
        }
    }

    /**
     * tell the Queue the end of stream reached
     */
    public void setEOS() {
        this.EOS = true;
    }

    /**
     * check if EOS is in the queue (virtually)
     */
    public boolean getEOS() {
        return this.EOS;
    }

    /**
     * Retrieves and removes the head of this queue, waiting if necessary until an element becomes available.
     * If EOS true and Queue empty, null is returned
     * @return Element retrieved or null
     * @throws InterruptedException
     */
    public T take() throws InterruptedException {
        if (blocking) {
            if (this.EOS) {
                // no infinite wait, if there are elements are already in the list
                return blocking_queue.poll();
            } else {
                T i;
                while (true) {
                    i = blocking_queue.poll(bb_settings.backOff, TimeUnit.MILLISECONDS);
                    if (i != null) {
                        return i;
                    } else if (this.EOS) {
                        return null;
                    }
                }
            }
        } else {
            if (bounded) {
                T i;
                if (this.EOS) {
                    return nonblocking_bounded_queue.poll();
                } else {
                    while (true) {
                        i = nonblocking_bounded_queue.poll();
                        if (i != null) {
                            return i;
                        } else if (this.EOS) {
                            return null;
                        }

                        Thread.sleep(bb_settings.backOff);
                    }
                }
            } else {
                if (this.EOS) {
                    return nonblocking_queue.poll();
                } else {
                    T i;
                    while (true) {
                        i = nonblocking_queue.poll();
                        if (i != null) {
                            return i;
                        } else if (this.EOS) {
                            return null;
                        }

                        Thread.sleep(bb_settings.backOff);
                    }
                }
            }
        }
    }

    /**
     * Retrieves and removes the head of this queue, waiting up to the specified wait time if necessary for an element to become available.
     * if EOS reached, call take() function returning the element or null (if queue empty)
     * @param timeout
     * @param timeunit
     * @return the element or null. Null if EOS is true, means EOS reached. Null with EOS false means poll timedout
     * @throws InterruptedException
     */
    public T poll(long timeout, TimeUnit timeunit) throws InterruptedException {
        if (this.EOS) {
            // no waiting needed, take in EOS is already fine
            return this.take();
        }

        if (blocking) {
            return blocking_queue.poll(timeout, timeunit);
        } else {
            long ms_timeout = TimeUnit.MILLISECONDS.convert(timeout, timeunit);
            if (bounded) {
                T i;
                long waited = 0;
                while (true) {
                    i = nonblocking_bounded_queue.poll();
                    if (i != null) {
                        return i;
                    } else if (this.EOS) {
                        return this.take();
                    }

                    if (waited + bb_settings.backOff > ms_timeout) {
                        return null;
                    }

                    Thread.sleep(bb_settings.backOff);
                }
            } else {
                T i;
                long waited = 0;
                while (true) {
                    i = nonblocking_queue.poll();
                    if (i != null) {
                        return i;
                    } else if (this.EOS) {
                        return this.take();
                    }

                    if (waited + bb_settings.backOff > ms_timeout) {
                        return null;
                    }

                    Thread.sleep(bb_settings.backOff);
                }
            }
        }
    }

    /**
     * Inserts the specified element at the tail of this queue if it is possible to do so immediately without exceeding the queue's capacity, returning true upon success and false if this queue is full
     * @param i element to insert
     * @return true or false
     */
    public boolean offer(T i) {
        if (this.EOS) { return false; }

        if (blocking) {
            return blocking_queue.offer(i);
        } else {
            if (bounded) {
                return nonblocking_bounded_queue.offer(i);
            } else {
                return nonblocking_queue.offer(i);
            }
        }
    }

    /**
     * Inserts the specified element at the tail of this queue, waiting if necessary up to the specified wait time for space to become available.
     * @param i element to insert
     * @param timeout time to wait
     * @param timeunit unit of the time to wait
     * @return true or false
     * @throws InterruptedException
     */
    public boolean offer(T i, long timeout, TimeUnit timeunit) throws InterruptedException {
        if (this.EOS) { return false; }

        if (blocking) {
            return blocking_queue.offer(i, timeout, timeunit);
        } else {
            long ms_timeout = TimeUnit.MILLISECONDS.convert(timeout, timeunit);
            if (bounded) {
                long waited = 0;
                while (true) {
                    if (nonblocking_bounded_queue.offer(i)) {
                        return true;
                    }

                    if (waited + bb_settings.backOff > ms_timeout) {
                        return false;
                    }

                    Thread.sleep(bb_settings.backOff);
                }
            } else { // unbounded, never return false
                return nonblocking_queue.offer(i);
            }
        }
    }
}
