package tests.ff_tests;

import bbflow.*;

import java.util.LinkedList;

/*
 *
 *
 *            |--> Worker1 -->|                                          |-->Worker2->|
 *            |               |                                          |            |
 * Emitter -> |--> Worker1 -->|------- Filter1 --------> Filter2-------->|            | ->Filter3
 *            |               |                                          |            |
 *            |--> Worker1 -->|                                          |-->Worker2->|
 *
 */

/**
 * Testing 2 farms with collector/emitter replaced and combined together
 */
public class combine3 {
    public static void main (String[] args) {
        defaultWorker<Long, Long> Emitter = new defaultWorker<>() {
            public Long runJob(Long x) {
                return null;
            }

            public void init() {
                for (long i = 1; i <=100; ++i) {
                    sendOutTo(i, ((int)i)%out.size());
                }
                sendEOS();
            }
        };

        defaultWorker<Long, Long> Worker1 = new defaultWorker<>() {
            public void runJobMulti(Long x, LinkedList<ff_queue<Long>> out) {
                sendOut(x);
            }
        };

        defaultWorker<Long, Long> Worker2 = new defaultWorker<>() {
            public void runJobMulti(Long x, LinkedList<ff_queue<Long>> out) {
                sendOut(x);
            }
        };

        defaultWorker<Long, Long> Filter1 = new defaultWorker<>() {
            public void runJobMulti(Long x, LinkedList<ff_queue<Long>> out) {
                System.out.println("F1 Received "+x+" from "+position);
                sendOut(x);
            }
        };

        defaultWorker<Long, Long> Filter2 = new defaultWorker<>() {
            public void runJobMulti(Long x, LinkedList<ff_queue<Long>> out) {
                sendOutTo(x, x.intValue()%2);
            }
        };

        defaultWorker<Long, Long> Filter3 = new defaultWorker<>() {
            public void runJobMulti(Long x, LinkedList<ff_queue<Long>> out) {
                System.out.println("F3 Received "+x+" from "+position);
            }
        };

        ff_farm stage1 = new ff_farm(3, Worker1);
        ff_farm stage2 = new ff_farm(3, Worker2);

        stage1.removeEmitter();
        stage1.emitter = new ff_node(Emitter);
        stage1.connectEmitterWorkers();
        stage1.removeCollector();
        stage1.collector = new ff_node(Filter1);
        stage1.connectWorkersCollector();

        stage2.removeEmitter();
        stage2.emitter = new ff_node(Filter2);
        stage2.connectEmitterWorkers();
        stage2.removeCollector();
        stage2.collector = new ff_node(Filter3);
        stage2.connectWorkersCollector();
        stage2.addOutputChannel(new ff_queue());

        ff_comb all = new ff_comb(stage1,stage2);
        all.start();
        all.join();
    }
}
