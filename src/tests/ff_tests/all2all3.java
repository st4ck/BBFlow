package tests.ff_tests;

import bbflow.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * |<- multi-output ->|  |<-------- all-to-all --------->| |<- multi-input ->|
 *
 *                      | -----> Router-->|
 *                      |                 |---> Even --->
 *                      |                 |              |
 *     Generator ------>| -----> Router-->|              | --------> Sink
 *                      |                 |              |
 *                      |                 |--->  Odd --->
 *                      | -----> Router-->|
 */

public class all2all3 {
    public static void main (String[] args) {
        defaultWorker<Long, Long> Generator = new defaultWorker<>() {
            public Long runJob(Long x) {
                return null;
            }

            public void init() {
                sendOutTo((long)(13),  0);
                sendOutTo((long)(17),  0);
                sendOutTo((long)(14),  1);
                sendOutTo((long)(27),  1);
                sendOutTo((long)(31),  1);
                sendOutTo((long)(6),   2);
                sendOutTo((long)(8),   0);
                sendOutTo((long)(4),   1);
                sendOutTo((long)(26),  2);
                sendOutTo((long)(31),  2);
                sendOutTo((long)(105), 0);
                sendOutTo((long)(238), 1);
                sendOutTo((long)(47),  2);
                sendOutTo((long)(48),  2);
                sendEOS();
            }
        };

        defaultWorker<Long, Long> Router = new defaultWorker<>() {
            public void runJobMulti(Long x, LinkedList<ff_queue<Long>> out) {
                System.out.println("Router" + id + " received "+x);
                if (x%2 == 0) {
                    sendOutTo(x, 0);
                } else {
                    sendOutTo(x, 1);
                }
            }
        };

        defaultJob<Long, Long> Odd = new defaultJob<>() {
            long sum = 0;

            public void runJobMulti(Long x, LinkedList<ff_queue<Long>> out) {
                sum += x;
            }

            public void EOS() {
                System.out.println("EOS received by Odd");
                System.out.println("Odd Sending sum "+sum);
                sendOut(sum);
            }
        };

        defaultJob<Long, Long> Even = new defaultJob<>() {
            long sum = 0;

            public void runJobMulti(Long x, LinkedList<ff_queue<Long>> out) {
                sum += x;
            }

            public void EOS() {
                System.out.println("EOS received by Even");
                System.out.println("Even Sending sum "+sum);
                sendOut(sum);
            }
        };

        defaultJob<Long,Long> Sink = new defaultJob<>() {
            public void runJobMulti(Long x, LinkedList<ff_queue<Long>> out) {
                System.out.println("Sink received: " + x);
            }
        };

        ff_farm stage1 = new ff_farm(3, Router);
        stage1.removeEmitter();
        stage1.emitter = new ff_node(Generator);
        stage1.connectEmitterWorkers();

        LinkedList<defaultJob<Long,Long>> stage2_workers = new LinkedList<>();
        stage2_workers.add(Even);
        stage2_workers.add(Odd);

        ff_farm stage2 = new ff_farm(stage2_workers);
        stage2.removeCollector();
        stage2.collector = new ff_node(Sink);
        stage2.connectWorkersCollector();
        stage2.addOutputChannel(new ff_queue());

        ff_all2all stages = new ff_all2all();
        stages.combine_farm(stage1,stage2);

        stages.start();
        stages.join();
    }

}
