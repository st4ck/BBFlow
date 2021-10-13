package tests.ff_tests;

import bbflow.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/*
 * |<----------------------- farm ------------------->|
 *                    |<---- all-to-all ---->|
 *
 *             ------> Router-->
 *             |                |---> Even ->
 *             |                |            |
 * Generator --|-----> Router-->|            | ---> Sink
 *             |                |            |
 *             |                |--->  Odd ->
 *             ------> Router-->
 */

public class all2all2 {
    public static void main (String[] args) {
        ff_queue<Long> input_data = new ff_queue<>();

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
        stages.addInputChannel(input_data);

        stages.start();

        input_data.put((long)(13));
        input_data.put((long)(17));
        input_data.put((long)(14));
        input_data.put((long)(27));
        input_data.put((long)(31));
        input_data.put((long)(6));
        input_data.put((long)(8));
        input_data.put((long)(4));
        input_data.put((long)(26));
        input_data.put((long)(31));
        input_data.put((long)(105));
        input_data.put((long)(238));
        input_data.put((long)(47));
        input_data.put((long)(48));
        input_data.setEOS(); // sending EOF

        stages.join();
    }

}
