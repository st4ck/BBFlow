package tests.ff_tests;

import bbflow.*;

import java.util.ArrayList;
import java.util.LinkedList;

/*
 *  |<------------- farm ----------->|   |<------ all-to-all ------>|
 *  |    without the collectort      |
 *  |    with workers multi-output   |
 *
 *
 *              |-> Worker --->|         | --> Filter1 -->|
 *              |              |         |                | --> Filter2
 *   Emitter -> |-> Worker --->|------>  | --> Filter1 -->|
 *              |              |         |                | --> Filter2
 *              |-> Worker --->|         | --> Filter1 -->|
 */

public class all2all7 {
    public static void main (String[] args) {
        defaultWorker<Long, Long> Emitter = new defaultWorker<>() {
            public Long runJob(Long x) {
                return null;
            }

            public void init() {
                for (long i = 1; i <=100; ++i) {
                    sendOutTo(i, ((int)i)%3);
                }
                sendEOS();
            }
        };

        defaultWorker<Long, Long> MultiInputHelper = new defaultWorker<>() {
            public void runJobMulti(Long x, LinkedList<ff_queue<Long>> out) {
                sendOut(x);
            }
        };

        defaultWorker<Long, Long> Worker = new defaultWorker<>() {
            public void runJobMulti(Long x, LinkedList<ff_queue<Long>> out) {
                sendOut(x);
            }
        };

        defaultWorker<Long, Long> Filter1 = new defaultWorker<>() {
            public void runJobMulti(Long x, LinkedList<ff_queue<Long>> out) {
                sendOut(x);
            }
        };

        defaultWorker<Long, Long> Filter2 = new defaultWorker<>() {
            public void runJobMulti(Long x, LinkedList<ff_queue<Long>> out) {
                System.out.println(id + ": "+x);
            }
        };

        ff_node stage1 = new ff_node(Emitter);

        ff_farm firstStage = new ff_farm<>(3, Worker);
        firstStage.removeEmitter();
        firstStage.emitter = stage1;
        firstStage.connectEmitterWorkers();
        firstStage.removeCollector();

        firstStage.collector = new ff_node(MultiInputHelper);
        firstStage.connectWorkersCollector();

        ff_farm filter1 = new ff_farm(3, Filter1);
        filter1.removeEmitter();
        ff_farm filter2 = new ff_farm(2, Filter2);

        ff_all2all secondStage = new ff_all2all();
        secondStage.combine_farm(filter1,filter2);

        ff_pipeline all = new ff_pipeline(firstStage,secondStage,ff_pipeline.TYPE_1_N);

        all.start();
        all.join();
    }

}
