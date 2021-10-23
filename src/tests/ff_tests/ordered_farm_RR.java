package tests.ff_tests;

import bbflow.*;

import java.util.LinkedList;

/**
 * Example of Ordered Farm using Emitter and Collector in ROUNDROBIN
 */
public class ordered_farm_RR {
    public static void main (String[] args) {
        defaultWorker<Integer, Integer> Generator = new defaultWorker<>() {
            public Integer runJob(Integer x) {
                return null;
            }

            public void init() {
                for (Integer i = 0; i < 100; ++i) {
                    sendOutTo(Integer.valueOf(i), i % out.size());
                }
                sendEOS();
            }
        };

        defaultWorker<Integer, Integer> Worker1 = new defaultWorker<>() {
            public Integer runJob(Integer x) {
                return x;
            }
        };

        defaultWorker<Integer, Integer> Filter2 = new defaultWorker<>() {
            public void runJobMulti(Integer x, LinkedList<ff_queue<Integer>> out) {
                System.out.println("Filter2 received: "+x);
            }
        };

        ff_node stage1 = new ff_node(Generator);
        ff_node stage3 = new ff_node(Filter2);

        int n_workers = 16;
        if (args.length == 1) {
            n_workers = Integer.parseInt(args[0]);
        }

        ff_farm stage2 = new ff_farm<Integer, Integer>(0, null);
        for (int i = 0; i < n_workers; i++) {
            stage2.workers.push(new ff_node(defaultJob.uniqueJob(Worker1, i)));
        }

        stage2.emitter = new ff_node(new defaultEmitter(defaultEmitter.ROUNDROBIN));
        stage2.collector = new ff_node(new defaultCollector<Integer>(defaultCollector.ROUNDROBIN));
        stage2.connectWorkersCollector();
        stage2.connectEmitterWorkers();


        ff_pipeline all = new ff_pipeline(stage1, stage2);
        all.appendBlock(stage3, ff_pipeline.TYPE_1_1);
        all.addOutputChannel(new ff_queue());

        customWatch x = new customWatch();
        x.start();
        all.start();
        all.join();
        x.end();
        x.printReport(true);
    }
}
