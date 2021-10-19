package bbflow;

import tests.*;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *  * Fundamental block modeling the Farm paradigm
 *  * Composed by one emitter (single input by default), n_workers workers and one collector
 *  * Worker must be implemented; the function runJob() contains the computation part
 *  * Collector has by default one output channel
 *  * If collector is not needed and the job ends in the workers, just avoid sending data to the channel between workers and collector, and it will be ignored
 * @param <T> Custom type input
 * @param <U> Custom type output
 */
public class ff_farm<T,U> extends ff_node<T,U> {
    public ff_node<T,T> emitter = null;
    public ff_node<U,U> collector = null;
    public LinkedList<ff_node> workers = new LinkedList<>();
    private ff_queue<T> input;
    private int bufferSize;

    /**
     *
     * @param worker_job the list of jobs (one for each worker) to be executed of type bbflow.defaultJob. They can be different if needed
     * @param emitter_strategy Emitter communication strategy chosen between ROUNDROBIN, SCATTER and BROADCAST
     * @param collector_strategy Collector communication strategy chosen between FIRSTCOME, ROUNDROBIN and GATHER
     */
    public ff_farm(LinkedList<defaultJob<T,U>> worker_job, int emitter_strategy, int collector_strategy, int bufferSize) {
        create_farm(worker_job, emitter_strategy, collector_strategy, bufferSize);
    }

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

    public ff_farm(LinkedList<defaultJob<T,U>> worker_job, int emitter_strategy) {
        this(worker_job, emitter_strategy, defaultCollector.ROUNDROBIN, bb_settings.defaultBufferSize);
    }

    public ff_farm(LinkedList<defaultJob<T,U>> worker_job) {
        this(worker_job, defaultEmitter.ROUNDROBIN, defaultCollector.ROUNDROBIN, bb_settings.defaultBufferSize);
    }

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

    public ff_farm(int n_workers, defaultJob<T,U> worker_job, int emitter_strategy) {
        this(n_workers, worker_job, emitter_strategy, defaultCollector.ROUNDROBIN, bb_settings.defaultBufferSize);
    }

    public ff_farm(int n_workers, defaultJob<T,U> worker_job) {
        this(n_workers, worker_job, defaultEmitter.ROUNDROBIN, defaultCollector.ROUNDROBIN, bb_settings.defaultBufferSize);
    }

    public void connectEmitterWorkers() {
        if (emitter == null) { return; }
        if (workers.size() == 0) { return; }

        for (int i=0; i<workers.size(); i++) {
            ff_queue<T> channel = new ff_queue<T>(bb_settings.BLOCKING, bb_settings.BOUNDED, this.bufferSize);
            emitter.addOutputChannel(channel);
            workers.get(i).addInputChannel(channel);
        }
    }

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

        if ((collector != null) && (!collector.mynode.isAlive())) {
            collector.start();
        }

        if ((emitter != null) && (!emitter.mynode.isAlive())) {
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
    public void addOutputChannel(ff_queue<U> output) {
        if (collector != null) {
            collector.addOutputChannel(output);
        }
    }

    public void removeEmitter() {
        emitter = null;
        for (int i=0; i<workers.size(); i++) {
            workers.get(i).removeInputChannel(0);
        }
    }

    public void removeCollector() {
        collector = null;
        for (int i=0; i<workers.size(); i++) {
            workers.get(i).removeOutputChannel(0);
        }
    }
}
