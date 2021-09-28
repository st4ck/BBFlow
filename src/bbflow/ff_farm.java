package bbflow;

import java.util.LinkedList;

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
    private LinkedList<T> input;

    /**
     *
     * @param worker_job the list of jobs (one for each worker) to be executed of type bbflow.defaultJob. They can be different if needed
     * @param EOF End Of File object used to detect the end of stream from the input channel
     */
    public ff_farm(LinkedList<defaultJob<T>> worker_job, T EOF, int communication_strategy) {
        this.workers = new LinkedList<>();
        this.worker_job = worker_job;
        Object emitterWorkersLock = new Object();
        Object workersCollectorLock = new Object();

        emitter = new ff_node<T>(new defaultEmitter<T>(communication_strategy, EOF));
        emitter.setOutputLock(emitterWorkersLock);

        collector = new ff_node<T>(new defaultCollector<T>(EOF));
        collector.setInputLock(workersCollectorLock);

        for (int i=0;i<worker_job.size();i++) {
            ff_node<T> worker = new ff_node<T>(worker_job.get(i));
            worker.setInputLock(emitterWorkersLock);
            worker.setOutputLock(workersCollectorLock);

            LinkedList<T> emitter_worker = new LinkedList<T>();
            emitter.addOutputChannel(emitter_worker);
            worker.addInputChannel(emitter_worker);

            LinkedList<T> worker_collector = new LinkedList<T>();
            worker.addOutputChannel(worker_collector);
            collector.addInputChannel(worker_collector);

            workers.add(worker);
        }
    }

    /**
     * main method to start all farm threads and wait them finishes
     */
    public void run() {
        for (int i=0; i<workers.size(); i++) {
            workers.get(i).start(); // start all workers threads
        }
        collector.start();
        emitter.start();

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
    public void pushElement(T i) {
        input.add(i);
    }

    /**
     * Push multiple elements inside input stream at the bottom of the list
     * @param i list to push
     */
    public void pushElement(LinkedList<T> i) {
        input.addAll(i);
    }

    /**
     * add a new input channel to the farm (commonly one) - to the emitter
     * @param input input channel
     */
    public void addInputChannel(LinkedList<T> input) {
        this.input = input;
        emitter.addInputChannel(this.input);
    }

    /**
     * add a new output channel to the collector
     * @param output output channel
     */
    public void addOutputChannel(LinkedList<T> output) {
        collector.addOutputChannel(output);
    }

    /**
     * set lock object needed to wait new elements from emitter input channel
     * @param l lock object
     */
    public void setInputLock(Object l) {
        emitter.setInputLock(l);
    }

    /**
     * set lock object needed to notify output channels of the collectors of the presence of new elements
     * @param l lock object
     */
    public void setOutputLock(Object l) {
        collector.setOutputLock(l);
    }
}
