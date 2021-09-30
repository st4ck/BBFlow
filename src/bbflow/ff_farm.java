package bbflow;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Fundamental block modeling the Farm paradigm
 * Composed by one emitter (single input by default), n_workers workers and one collector
 * Worker must be implemented; the function runJob() contains the computation part
 * Collector has by default one output channel
 * If collector is not needed and the job ends in the workers, just avoid sending data to the channel between workers and collector, and it will be ignored
 * @param <T>
 */
public class ff_farm<T> extends block<T> {
    public ff_node<T> emitter;
    public ff_node<T> collector;
    public LinkedList<ff_node> workers;
    private ff_queue<T> input;
    private int bufferSize;

    /**
     *
     * @param worker_job the list of jobs (one for each worker) to be executed of type bbflow.defaultJob. They can be different if needed
     * @param emitter_strategy Emitter communication strategy chosen between ROUNDROBIN, SCATTER and BROADCAST
     * @param collector_strategy Collector communication strategy chosen between FIRSTCOME, ROUNDROBIN and GATHER
     */
    public ff_farm(LinkedList<defaultJob<T>> worker_job, int emitter_strategy, int collector_strategy, int bufferSize) {
        this.bufferSize = bufferSize;
        this.workers = new LinkedList<>();

        emitter = new ff_node<T>(new defaultEmitter<T>(emitter_strategy));
        collector = new ff_node<T>(new defaultCollector<T>(collector_strategy));

        for (int i=0;i<worker_job.size();i++) {
            ff_node<T> worker = new ff_node<T>(worker_job.get(i));

            ff_queue<T> emitter_worker = new ff_queue<T>(bb_settings.BLOCKING, bb_settings.BOUNDED, this.bufferSize);
            emitter.addOutputChannel(emitter_worker);
            worker.addInputChannel(emitter_worker);

            ff_queue<T> worker_collector = new ff_queue<T>(bb_settings.BLOCKING, bb_settings.BOUNDED, this.bufferSize);
            worker.addOutputChannel(worker_collector);
            collector.addInputChannel(worker_collector);

            workers.add(worker);
        }
    }

    public ff_farm(LinkedList<defaultJob<T>> worker_job, int emitter_strategy) {
        this(worker_job, emitter_strategy, defaultCollector.ROUNDROBIN, bb_settings.defaultBufferSize);
    }

    public ff_farm(LinkedList<defaultJob<T>> worker_job) {
        this(worker_job, defaultEmitter.ROUNDROBIN, defaultCollector.ROUNDROBIN, bb_settings.defaultBufferSize);
    }

    /**
     * main method to start all farm threads
     */
    public void start() {
        for (int i=0; i<workers.size(); i++) {
            workers.get(i).start(); // start all workers threads
        }

        if (collector != null) {
            collector.start();
        }

        if (emitter != null) {
            emitter.start();
        }
    }

    public void join() {
        if (emitter != null) {
            emitter.join();
        }
        for (int i=0; i<workers.size(); i++) {
            workers.get(i).join();
        }

        if (collector != null) {
            collector.join();
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
    public void addInputChannel(ff_queue<T> input) {
        this.input = input;
        if (emitter != null) {
            emitter.addInputChannel(this.input);
        }
    }

    /**
     * add a new output channel to the collector
     * @param output output channel
     */
    public void addOutputChannel(ff_queue<T> output) {
        if (collector != null) {
            collector.addOutputChannel(output);
        }
    }
}
