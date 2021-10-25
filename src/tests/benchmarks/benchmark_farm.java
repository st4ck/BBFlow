package tests.benchmarks;

import bbflow.*;

import java.util.LinkedList;

public class benchmark_farm {
    public static void main (String[] args) {
        preloader.preloadJVM();

        int n = 1000;
        int n_workers = 16;
        if (args.length == 2) {
            n = Integer.parseInt(args[0]);
            n_workers = Integer.parseInt(args[1]);
        }


        Long finalN = Long.valueOf(n);
        defaultWorker<Long, Long> Emitter = new defaultWorker<>() {
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

        defaultWorker<Long, Long> Worker1 = new defaultWorker<>() {
            public Long runJob(Long x) {
                /*try {
                    ff_queue.sleepNanos(5000000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/

                for (int i=0; i<1000; i++) {
                    x *= 1000;
                    x /= 999;
                }
                return x;
            }
        };

        defaultWorker<Long, Long> Collector = new defaultWorker<>() {
            public void runJobMulti(Long x, LinkedList<ff_queue<Long>> out) {
                //System.out.println("Filter2 received: "+x);
            }
        };

        ff_node e = new ff_node(Emitter);
        ff_node c = new ff_node(Collector);

        ff_farm farm = new ff_farm<Long,Long>(0, null);
        for (int i=0; i<n_workers; i++) {
            farm.workers.push(new ff_node(defaultJob.uniqueJob(Worker1, i)));
        }

        farm.emitter = e;
        farm.collector = c;
        farm.connectWorkersCollector();
        farm.connectEmitterWorkers();
        farm.addOutputChannel(new ff_queue());

        customWatch x = new customWatch();
        x.start();
        farm.start();
        farm.join();
        x.end();
        x.printReport(true);
    }
}
