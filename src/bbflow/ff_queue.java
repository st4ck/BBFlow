package bbflow;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ff_queue<T> {
    LinkedBlockingQueue<T> blocking_queue;
    ConcurrentLinkedQueue<T> nonblocking_queue;
    squeue<T> nonblocking_bounded_queue;
    private boolean EOS = false;

    public static final int BLOCKING = 1;
    public static final int NONBLOCKING = 2;

    public static final int BOUNDED = 3;
    public static final int UNBOUNDED = 4;

    boolean blocking = false;
    boolean bounded = false;

    public long backoff = 5;

    long queuesize = 0;

    public ff_queue(int blocking, int bounded, int size) {
        if (blocking == BLOCKING) {
            this.blocking = true;

            if (bounded == BOUNDED) {
                this.bounded = true;

                blocking_queue = new LinkedBlockingQueue<>(size);
            } else if (bounded == UNBOUNDED) {
                blocking_queue = new LinkedBlockingQueue<>();
            }
        } else if (blocking == NONBLOCKING){
            if (bounded == BOUNDED) {
                this.bounded = true;

                nonblocking_bounded_queue = new squeue<>(size);
            } else if (bounded == UNBOUNDED) {
                nonblocking_queue = new ConcurrentLinkedQueue<>();
            }
        }
    }

    public ff_queue() {
        this(NONBLOCKING, UNBOUNDED, 0);
    }

    public ff_queue(int blocking) {
        this(blocking, UNBOUNDED, 0);
    }

    public void put(T i) throws InterruptedException {
        if (this.EOS) { return; }

        if (blocking) {
            blocking_queue.put(i);
        } else {
            if (bounded) {
                nonblocking_bounded_queue.add(i);
            } else {
                nonblocking_queue.add(i);
            }
        }
    }

    public void setEOS() {
        this.EOS = true;
    }

    public boolean getEOS() {
        return this.EOS;
    }

    public T take() throws InterruptedException {
        if (blocking) {
            if (this.EOS) {
                // no infinite wait, if there are elements are already in the list
                return blocking_queue.poll();
            } else {
                T i;
                while (true) {
                    i = blocking_queue.poll();
                    if (i != null) {
                        return i;
                    } else if (this.EOS) {
                        return null;
                    }

                    Thread.sleep(backoff);
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

                        Thread.sleep(backoff);
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

                        Thread.sleep(backoff);
                    }
                }
            }
        }
    }

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

                    if (waited + backoff > ms_timeout) {
                        return null;
                    }

                    Thread.sleep(backoff);
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

                    if (waited + backoff > ms_timeout) {
                        return null;
                    }

                    Thread.sleep(backoff);
                }
            }
        }
    }

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

                    if (waited + backoff > ms_timeout) {
                        return false;
                    }

                    Thread.sleep(backoff);
                }
            } else { // unbounded, never return false
                return nonblocking_queue.offer(i);
            }
        }
    }
}
