package tests.benchmarks;

import bbflow.*;

/**
 * Pipeline benchmark with N nodes
 */
public class benchmark_pipeline {
    public static void main (String[] args) {
        preloader.preloadJVM();

        bb_settings.backOff = 100;

        int n = 1000;
        int n_workers = 16;
        if (args.length >= 2) {
            n = Integer.parseInt(args[0]);
            n_workers = Integer.parseInt(args[1]);
            if (args.length == 3) {
                bb_settings.backOff = Integer.parseInt(args[2]);
            }
        }

        Long finalN = Long.valueOf(n);
        defaultWorker<Long, Long> Generator = new defaultWorker<>() {
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

        int totalTask = 1000000/n_workers;
        defaultWorker<Long, Long> Worker = new defaultWorker<>() {
            public Long runJob(Long x) {
                long y = x;
                for (int i=0; i<totalTask; i++) {
                    y *= 1000;
                    y /= 999;
                }
                return y;
            }
        };

        defaultWorker<Long, Long> Final = new defaultWorker<>() {
            public Long runJob(Long x) {
                return null;
            }
        };

        ff_pipeline all = new ff_pipeline(new ff_node(Generator), new ff_node(defaultJob.uniqueJob(Worker)));

        for (int i=0; i<n_workers-1; i++) {
            all.appendBlock(new ff_node<>(defaultJob.uniqueJob(Worker)), ff_pipeline.TYPE_1_1);
        }

        all.appendBlock(new ff_node<>(Final), ff_pipeline.TYPE_1_1);

        customWatch x = new customWatch();
        x.start();
        all.start();
        all.join();
        x.end();
        x.printReport(true);
    }
}
