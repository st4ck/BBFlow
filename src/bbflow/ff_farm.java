package bbflow;

import java.io.*;
import java.util.LinkedList;

/**
 * Fundamental block modeling the Farm paradigm
 * Composed by one emitter (single input by default), n_workers workers and one collector
 * Worker must be implemented; the function runJob or runJobMulti contains the computation part. See details below or the thesis
 * Collector has by default one output channel
 * If collector is not needed and the job ends in the workers, just avoid sending data to the channel between workers and collector, and it will be ignored
 * @param <T> Custom type input
 * @param <U> Custom type output
 */
public class ff_farm<T,U> extends ff_node<T,U> {
    public ff_node<T,T> emitter = null;
    public ff_node<U,U> collector = null;
    public LinkedList<ff_node> workers = new LinkedList<>();
    private ff_queue<T> input;
    private int bufferSize;

    public static void preload() {
    }

    /**
     * constructor given a list of defaultJob, Emitter strategy, Collector strategy and buffer size
     * @param worker_job the list of jobs (one for each worker) to be executed of type bbflow.defaultJob. They can be different if needed
     * @param emitter_strategy Emitter communication strategy chosen between ROUNDROBIN, SCATTER and BROADCAST
     * @param collector_strategy Collector communication strategy chosen between FIRSTCOME, ROUNDROBIN and GATHER
     */
    public ff_farm(LinkedList<defaultJob<T,U>> worker_job, int emitter_strategy, int collector_strategy, int bufferSize) {
        create_farm(worker_job, emitter_strategy, collector_strategy, bufferSize);
    }

    /**
     * constructor given a list of defaultJob and the Emitter strategy
     * @param worker_job list of defaultJob
     * @param emitter_strategy Emitter strategy between ROUNDROBIN, SCATTER, BROADCAST
     * @param collector_strategy Collector strategy between ROUNDROBIN, FIRSTCOME, GATHER
     * @param bufferSize buffer size of channels
     */
    private void create_farm(LinkedList<defaultJob<T,U>> worker_job, int emitter_strategy, int collector_strategy, int bufferSize) {
        this.bufferSize = bufferSize;

        emitter = new ff_node<T,T>(new defaultEmitter<T>(emitter_strategy));
        collector = new ff_node<U,U>(new defaultCollector<U>(collector_strategy));

        for (int i=0;i<worker_job.size();i++) {
            ff_node<T,U> worker = new ff_node<T,U>(worker_job.get(i));

            ff_queue<T> emitter_worker = new ff_queue<T>(bb_settings.BLOCKING, bb_settings.BOUNDED, this.bufferSize);
            emitter.addOutputChannel(emitter_worker);
            worker.addInputChannel(emitter_worker);

            ff_queue<U> worker_collector = new ff_queue<U>(bb_settings.BLOCKING, bb_settings.BOUNDED, this.bufferSize);
            worker.addOutputChannel(worker_collector);
            collector.addInputChannel(worker_collector);

            workers.add(worker);
        }
    }

    /**
     * constructor given a list of defaultJob and the Emitter strategy
     * @param worker_job list of defaultJob
     * @param emitter_strategy Emitter strategy between ROUNDROBIN, SCATTER, BROADCAST
     */
    public ff_farm(LinkedList<defaultJob<T,U>> worker_job, int emitter_strategy) {
        this(worker_job, emitter_strategy, defaultCollector.ROUNDROBIN, bb_settings.defaultBufferSize);
    }

    /**
     * constructor given a list of defaultJob
     * @param worker_job list of defaultJob
     */
    public ff_farm(LinkedList<defaultJob<T,U>> worker_job) {
        this(worker_job, defaultEmitter.ROUNDROBIN, defaultCollector.ROUNDROBIN, bb_settings.defaultBufferSize);
    }

    /**
     * constructor for workerJob of anonymous type
     * @param n_workers number of workers
     * @param workerJob worker_job the worker job declared inline (as anonymous class)
     * @param emitter_strategy emitter strategy between ROUNDROBIN, SCATTER, BROADCAST
     * @param collector_strategy collector strategy between ROUNDROBIN, FIRSTCOME, GATHER
     * @param bufferSize buffer size of channels
     */
    public ff_farm(int n_workers, defaultJob<T,U> workerJob, int emitter_strategy, int collector_strategy, int bufferSize) {
        LinkedList<defaultJob<T,U>> worker_job = new LinkedList<>();

        if (n_workers < 1) { return; }
        if (workerJob == null) { return; }

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(workerJob);
            oos.flush();
            oos.close();
            bos.close();
            byte[] byteData = bos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(byteData);

            for (int i=0; i<n_workers; i++) {
                defaultWorker<T,U> WJ = (defaultWorker<T, U>) new ObjectInputStream(bais).readObject();
                bais.reset();
                WJ.runType = defaultWorker.INLINE;
                WJ.id = i;
                worker_job.add(WJ);
            }

            create_farm(worker_job, emitter_strategy, collector_strategy, bufferSize);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }

    /**
     * Create a new farm with Emitter in 'emitter_strategy' and default Collector in ROUNDROBIN
     * @param n_workers number of workers
     * @param worker_job the worker job declared inline (as anonymous class)
     * @param emitter_strategy emitter strategy between ROUNDROBIN, SCATTER, BROADCAST
     */
    public ff_farm(int n_workers, defaultJob<T,U> worker_job, int emitter_strategy) {
        this(n_workers, worker_job, emitter_strategy, defaultCollector.ROUNDROBIN, bb_settings.defaultBufferSize);
    }

    /**
     * Create a new farm with default Emitter in ROUNDROBING and Collector in ROUNDROBIN
     * @param n_workers number of workers
     * @param worker_job the worker job declared inline (as anonymous class)
     */
    public ff_farm(int n_workers, defaultJob<T,U> worker_job) {
        this(n_workers, worker_job, defaultEmitter.ROUNDROBIN, defaultCollector.ROUNDROBIN, bb_settings.defaultBufferSize);
    }

    /**
     * connect emitter to workers creating channels
     */
    public void connectEmitterWorkers() {
        if (emitter == null) { return; }
        if (workers.size() == 0) { return; }

        for (int i=0; i<workers.size(); i++) {
            ff_queue<T> channel = new ff_queue<T>(bb_settings.BLOCKING, bb_settings.BOUNDED, this.bufferSize);
            emitter.addOutputChannel(channel);
            workers.get(i).addInputChannel(channel);
        }
    }

    /**
     * connect workers to the collector creating channels
     */
    public void connectWorkersCollector() {
        if (collector == null) { return; }
        if (workers.size() == 0) { return; }

        for (int i=0; i<workers.size(); i++) {
            ff_queue<U> channel = new ff_queue<U>(bb_settings.BLOCKING, bb_settings.BOUNDED, this.bufferSize);
            collector.addInputChannel(channel);
            workers.get(i).addOutputChannel(channel);
        }
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

    /**
     * wait the farm to finish after EOS sent in the network
     */
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
    public void addOutputChannel(ff_queue<U> output) {
        if (collector != null) {
            collector.addOutputChannel(output);
        }
    }

    /**
     * remove Emitter from farm, including channels between emitter and workers
     */
    public void removeEmitter() {
        emitter = null;
        for (int i=0; i<workers.size(); i++) {
            workers.get(i).removeInputChannel(0);
        }
    }

    /**
     * remove Collector from farm, including channels between workers and collector
     */
    public void removeCollector() {
        collector = null;
        for (int i=0; i<workers.size(); i++) {
            workers.get(i).removeOutputChannel(0);
        }
    }
}
