package tests.ff_tests;

import bbflow.*;

import java.util.LinkedList;

/*
 *
 *  |                |<---- comb1 ---->|        |<- multi-input ->|
 *
 *
 *               |--> Worker1-->Worker2 -->|
 *               |                         |
 *   Emitter --> |--> Worker1-->Worker2 -->|-------> Collector ---------> Filter1
 *               |                         |
 *               |--> Worker1-->Worker2 -->|
 *
 */

/**
 * Two combined workers as single one in ff_farm
 */
public class combine2_benchmark {
    public static void main (String[] args) {
        defaultWorker<Double, Double> Emitter = new defaultWorker<>() {
            public Double runJob(Double x) {
                return null;
            }

            public void init() {
                for (Integer i = 1; i <=1000000; ++i) {
                    sendOutTo(Double.valueOf(i), ((int)i)%out.size());
                }
                sendEOS();
            }
        };

        defaultWorker<Double, Double> Worker1 = new defaultWorker<>() {
            public Double runJob(Double x) {
                x *= 1.02;
                x /= 1.01;
                return x;
            }
        };

        defaultWorker<Double, Double> Worker2 = new defaultWorker<>() {
            public Double runJob(Double x) {
                //System.out.println("Worker2 (id="+id+") in="+x);
                for (int i=0; i<10000; i++) {
                    x *= 1.02;
                    x /= 1.01;
                }

                if (x%10 == 0) {
                    return x;
                } else {
                    return null;
                }
            }
        };

        defaultWorker<Double, Double> Filter2 = new defaultWorker<>() {
            public void runJobMulti(Double x, LinkedList<ff_queue<Double>> out) {
                //System.out.println("Filter2 received: "+x);
            }
        };

        ff_node stage1 = new ff_node(Emitter);
        ff_node stage4 = new ff_node(Filter2);

        int n_workers = 16;
        if (args.length == 1) {
            n_workers = Integer.parseInt(args[0]);
        }

        ff_farm stage2 = new ff_farm<Double,Double>(0, null);
        for (int i=0; i<n_workers; i++) {
            stage2.workers.push(new ff_comb(new ff_node(defaultJob.uniqueJob(Worker1, i)), new ff_node(defaultJob.uniqueJob(Worker2, i))));
        }

        stage2.emitter = stage1;
        stage2.collector = new ff_node(new defaultCollector<Double>());
        stage2.connectWorkersCollector();
        stage2.connectEmitterWorkers();

        ff_pipeline all = new ff_pipeline(stage2,stage4);

        customWatch x = new customWatch();
        x.start();
        all.start();
        all.join();
        x.end();
        x.printReport(true);
    }
}
