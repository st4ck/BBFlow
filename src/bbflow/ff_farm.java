package bbflow;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Fundamental block modeling the Farm paradigm
 * Composed by one emitter (single input by default), n_workers workers and one collector
 * Worker must be implemented; the function runJob() contains the computation part
 * Collector has by default one output channel
 * @param <T>
 */
public class ff_farm<T> {
    private ff_node<T> emitter;
    private ff_node<T> collector;
    private LinkedList<ff_node> workers;
    private LinkedList<defaultJob<T>> worker_job;
    private LinkedBlockingQueue<T> input;
    private int bufferSize;

    /**
     *
     * @param worker_job the list of jobs (one for each worker) to be executed of type bbflow.defaultJob. They can be different if needed
     * @param EOF End Of File object used to detect the end of stream from the input channel
     * @param communication_strategy Emitter communication strategy chosen between ROUNDROBIN, SCATTER and BROADCAST
     * @param bufferSize buffer size of the channels between emitter/workers and between workers/collector
     */
    public ff_farm(LinkedList<defaultJob<T>> worker_job, T EOF, int communication_strategy, int bufferSize) {
        this.bufferSize = bufferSize;
        this.workers = new LinkedList<>();
        this.worker_job = worker_job;

        emitter = new ff_node<T>(new defaultEmitter<T>(communication_strategy, EOF));
        collector = new ff_node<T>(new defaultCollector<T>(EOF));

        for (int i=0;i<worker_job.size();i++) {
            ff_node<T> worker = new ff_node<T>(worker_job.get(i));

            LinkedBlockingQueue<T> emitter_worker = new LinkedBlockingQueue<T>(this.bufferSize);
            emitter.addOutputChannel(emitter_worker);
            worker.addInputChannel(emitter_worker);

            LinkedBlockingQueue<T> worker_collector = new LinkedBlockingQueue<T>(this.bufferSize);
            worker.addOutputChannel(worker_collector);
            collector.addInputChannel(worker_collector);

            workers.add(worker);
        }
    }

    public ff_farm(LinkedList<defaultJob<T>> worker_job, T EOF, int communication_strategy) {
        this(worker_job, EOF, communication_strategy, 4096);
    }

    public ff_farm(LinkedList<defaultJob<T>> worker_job, T EOF) {
        this(worker_job, EOF, defaultEmitter.ROUNDROBIN, 4096);
    }

    /**
     * main method to start all farm threads
     */
    public void run() {
        for (int i=0; i<workers.size(); i++) {
            workers.get(i).start(); // start all workers threads
        }
        collector.start();
        emitter.start();
    }

    public void join() {
        try {
            emitter.join();
            for (int i=0; i<workers.size(); i++) {
                workers.get(i).join();
            }
            collector.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Push element inside input stream at the bottom of the list
     * @param i element to push
     */
    public void pushElement(T i) throws InterruptedException {
        input.put(i);
    }

    /**
     * add a new input channel to the farm (commonly one) - to the emitter
     * @param input input channel
     */
    public void addInputChannel(LinkedBlockingQueue<T> input) {
        this.input = input;
        emitter.addInputChannel(this.input);
    }

    /**
     * add a new output channel to the collector
     * @param output output channel
     */
    public void addOutputChannel(LinkedBlockingQueue<T> output) {
        collector.addOutputChannel(output);
    }
}
