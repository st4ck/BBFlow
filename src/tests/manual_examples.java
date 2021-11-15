package tests;

import bbflow.*;

import java.util.LinkedList;

public class manual_examples {
    public static void main (String[] args) {
        /*defaultJob <Long, Long> Worker1 = new defaultJob<>() {
            public Long runJob(Long x) {
                return null;
            }

            public void init() {
                for (long i = 1; i <= finalN; ++i) {
                    sendOut(i);
                }
                sendEOS();
            }
        };

        defaultJob <Long, Long> Worker2 = new defaultJob<>() {
            public Long runJob(Long x) {
                System.out.println("Received item "+x);
                return null;
            }
        };

        ff_node stage1 = new ff_node(Worker1);
        ff_node stage2 = new ff_node(Worker2);
        ff_queue shm_channel = new ff_queue();
        stage1.addOutputChannel(shm_channel);
        stage2.addInputChannel(shm_channel);

        ff_pipeline two_stage_pipeline = new ff_pipeline(stage1,stage2);

        ff_comb comb = new ff_comb(stage1,stage2);*/

        /*defaultJob <Long, Long> Worker1 = new defaultJob<>() {
            public Long runJob(Long x) {
                x *= 5;
                return x;
            }
        };

        defaultJob <Long, Long> Worker2 = new defaultJob<>() {
            public Long runJob(Long x) {
                x /= 2;
                return x;
            }
        };*/

        /*defaultJob <Long, Long> Worker1 = new defaultJob<>() {
            public Long runJob(Long x) {
                return null;
            }

            public void init() {
                for (long i = 1; i <= 100; ++i) {
                    sendOut(i);
                }
                sendEOS();
            }
        };

        defaultWorker<Long,Long> workerJob = new defaultWorker<>() {
            public Long runJob(Long x) {
                return x*2;
            }
        };

        defaultJob <Long, Long> Worker2 = new defaultJob<>() {
            public Long runJob(Long x) {
                System.out.println("Received item "+x);
                return null;
            }
        };


        int n_workers = 3;
        ff_node generator = new ff_node(Worker1);
        ff_farm farm = new ff_farm<>(n_workers, workerJob);
        ff_node filter = new ff_node(Worker2);

        ff_pipeline pipe = new ff_pipeline(generator,farm);
        pipe.appendBlock(filter);

        pipe.start();
        pipe.join();*/

        /*defaultJob <Long, Long> Emitter = new defaultJob<>() {
            public Long runJob(Long x) {
                return null;
            }

            public void init() {
                for (long i = 1; i <= 100; ++i) {
                    sendOut(i);
                }
                sendEOS();
            }
        };

        defaultWorker<Long,Long> workerJob = new defaultWorker<>() {
            public Long runJob(Long x) {
                return x*2;
            }
        };

        defaultJob <Long, Long> Collector = new defaultJob<>() {
            public void runJobMulti(Long x, LinkedList<ff_queue<Long>> out) {
                System.out.println("Received item "+x+" from channel "+position);
            }
        };

        LinkedList<ff_node> workers = new LinkedList<>();
        for (int i=0; i<3; i++) workers.add(new ff_node(defaultJob.uniqueJob(workerJob)));

        ff_farm farm = new ff_farm(0, null);
        farm.emitter = new ff_node(Emitter);
        farm.collector = new ff_node(Collector);
        farm.workers = workers;
        farm.connectEmitterWorkers();
        farm.connectWorkersCollector();

        farm.start();
        farm.join();

        /*ff_farm farm = new ff_farm(3, workerJob);
        farm.removeEmitter();
        farm.removeCollector();
        farm.emitter = new ff_node(Emitter);
        farm.collector = new ff_node(Collector);
        farm.connectEmitterWorkers();
        farm.connectWorkersCollector();

        farm.start();
        farm.join();*/

        defaultWorker<Long,Long> w0 = new defaultWorker<>() {
            public Long runJob(Long x) {
                return null;
            }

            public void init() {
                for (long i = 1; i <= 100; ++i) {
                    sendOut(i);
                }
                sendEOS();
            }
        };

        defaultWorker<Long,Long> w1 = new defaultWorker<>() {
            public Long runJob(Long x) {
                return x*2;
            }
        };

        defaultJob <Long, Long> w2 = new defaultJob<>() {
            public Long runJob(Long x) {
                System.out.println("Received item "+x);
                return null;
            }
        };

        ff_node stage1 = new ff_node(w0);
        ff_node stage2 = new ff_node(w1);
        ff_node stage3 = new ff_node(w2);

        ff_pipeline pipe = new ff_pipeline(stage1,stage2);
        pipe.appendBlock(stage3);

        pipe.start();
        pipe.join();


        defaultJob<Long,Long> myjob = new defaultJob<>() {
            public Long runJob(Long x) {
                if (x%5 == 0) {
                    x += 2;
                    sendOut(x);
                }

                return null;
            }
        };
        ff_node mynode = new ff_node(myjob);

        /*myjob job = new myjob();
        ff_node mynode = new ff_node(job);*/

    }
}
