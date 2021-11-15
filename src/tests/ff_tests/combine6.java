package tests.ff_tests;

import bbflow.*;

import java.util.LinkedList;

/*
 *
 * Single farm topology where the emitter and the collector are a composition of nodes
 *
 *   |<------------ comp ----------->| |--> Worker -->|    |<----------- comp --------->|
 *                                     |              |
 *   Generator -->Filter1 --->Emit --> |--> Worker -->|--> Coll --> Filter2 --> Gatherer
 *                                     |              |
 *                                     |--> Worker -->|
 *
 */

/**
 * Single farm topology where the emitter and the collector are a composition of nodes
 */
public class combine6 {
    public static void main (String[] args) {
        defaultWorker<Long, Long> Generator = new defaultWorker<>() {
            public Long runJob(Long x) {
                return null;
            }

            public void init() {
                for (long i = 1; i <=100; ++i) {
                    sendOut(i);
                }
                sendEOS();
            }
        };

        defaultWorker<Long, Long> Filter1 = new defaultWorker<>() {
            public Long runJob(Long x) {
                return x;
            }

            public void EOS() {
                System.out.println("Filter1 EOS received");
            }
        };
        defaultWorker<Long, Long> Filter2 = new defaultWorker<>() {
            public Long runJob(Long x) {
                return x;
            }

            public void EOS() {
                System.out.println("Filter2 EOS received");
            }
        };

        defaultWorker<Long, Long> Emitter = new defaultWorker<>() {
            public void runJobMulti(Long x, LinkedList<ff_queue<Long>> o) {
                sendOut(x);
            }

            public void EOS() {
                System.out.println("Emitter EOS received");
            }
        };

        defaultWorker<Long, Long> Collector = new defaultWorker<>() {
            public void runJobMulti(Long x, LinkedList<ff_queue<Long>> o) {
                sendOut(x);
            }

            public void EOS() {
                System.out.println("Collector EOS received");
            }
        };

        defaultWorker<Long, Long> Worker = new defaultWorker<>() {
            public Long runJob(Long x) {
                if (id == 0) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                System.out.println("Worker "+id+" received "+x);

                return x;
            }
        };

        defaultWorker<Long, Long> Gatherer = new defaultWorker<>() {
            public Long runJob(Long x) {
                System.out.println("Gatherer received "+x);
                return null;
            }

            public void EOS() {
                System.out.println("Gatherer EOS received");
            }
        };

        ff_comb g_f = new ff_comb(new ff_node(Generator), new ff_node(Filter1));
        ff_comb f_g = new ff_comb(new ff_node(Filter2), new ff_node(Gatherer));

        ff_farm all = new ff_farm(3, Worker);
        all.removeEmitter();
        all.removeCollector();

        all.emitter = new ff_pipeline(g_f,new ff_node(Emitter));
        all.collector = new ff_pipeline(new ff_node(Collector), f_g);

        all.connectEmitterWorkers();
        all.connectWorkersCollector();

        all.start();
        all.join();
    }
}
