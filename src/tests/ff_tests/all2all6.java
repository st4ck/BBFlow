package tests.ff_tests;

import bbflow.*;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 *  |<----------------- farm ----------------->|   |<------ all-to-all ------>|
 *  |            with a multi-output           |
 *  |                  collector               |
 *
 *
 *              |-> Worker ->|                      | --> Filter1 -->|
 *              |            |                      |                | --> Filter2
 *   Emitter -> |-> Worker ->|-->Collector/Emitter->| --> Filter1 -->|
 *              |            |                      |                | --> Filter2
 *              |-> Worker ->|                      | --> Filter1 -->|
 */

public class all2all6 {
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
        //firstStage.removeCollector(); - keeping collector

        ff_farm filter1 = new ff_farm(3, Filter1);
        ff_farm filter2 = new ff_farm(2, Filter2);

        ff_all2all secondStage = new ff_all2all();
        secondStage.combine_farm(filter1,filter2);

        ff_pipeline all = new ff_pipeline(firstStage,secondStage);
        all.start();
        all.join();
    }

}
