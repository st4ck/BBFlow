package tests.ff_tests;

import bbflow.*;

import java.util.LinkedList;

/*
 *
 *  |<------ farm without collector ------->|   |<--------- comb2 --------->|
 *  |                |<---- comb1 ---->|        |<- multi-input ->|
 *
 *
 *               |--> Worker1-->Worker2 -->|
 *               |                         |
 *   Emitter --> |--> Worker1-->Worker2 -->|-------> Filter1 ---------> Filter2
 *               |                         |
 *               |--> Worker1-->Worker2 -->|
 *
 */

/**
 * Two combined workers as single one in ff_farm with different cardinalities
 */
public class combine2_multi {
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
                sendOutToAll(x);
            }
        };

        defaultWorker<Long, Long> Worker2 = new defaultWorker<>() {
            public void runJobMulti(Long x, LinkedList<ff_queue<Long>> out) {
                System.out.println("Worker2 (id="+id+") in="+x);
                sendOut(x);
            }
        };

        defaultWorker<Long, Long> Filter1 = new defaultWorker<>() {
            public void runJobMulti(Long x, LinkedList<ff_queue<Long>> out) {
                System.out.println("Received "+x+" from "+position); sendOut(x);
            }
        };

        defaultWorker<Long, Long> Filter2 = new defaultWorker<>() {
            public void runJobMulti(Long x, LinkedList<ff_queue<Long>> out) {
                System.out.println("Filter2 received: "+x);
            }
        };

        ff_node stage1 = new ff_node(Emitter);
        ff_node stage3 = new ff_node(Filter1);
        ff_node stage4 = new ff_node(Filter2);


        ff_farm stage2 = new ff_farm<Long,Long>(0, null);
        stage2.workers.push(new ff_comb(new ff_node(defaultJob.uniqueJob(Worker1,1)),new ff_node(defaultJob.uniqueJob(Worker2,1))));
        stage2.workers.push(new ff_comb(new ff_node(defaultJob.uniqueJob(Worker1,2)),new ff_node(defaultJob.uniqueJob(Worker2,2))));
        stage2.workers.push(new ff_comb(new ff_node(defaultJob.uniqueJob(Worker1,3)),new ff_node(defaultJob.uniqueJob(Worker2,3))));

        stage2.emitter = stage1;
        stage2.collector = stage3;
        stage2.connectWorkersCollector();
        stage2.connectEmitterWorkers();

        ff_pipeline all = new ff_pipeline(stage2,stage4);

        all.start();
        all.join();
    }
}
