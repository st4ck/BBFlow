package tests.benchmarks;

import bbflow.*;

/**
 * Benchmark of two nodes connected each other
 */
public class benchmark_blocking {
    public static void main (String[] args) {
        preloader.preloadJVM();

        int n = 1000;
        if (args.length == 1) {
            n = Integer.parseInt(args[0]);
        }
        int finalN = n;

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

        defaultWorker<Long, Long> Filter1 = new defaultWorker<>() {
            public Long runJob(Long x) {
                x *= 2;
                //System.out.println("Received "+x+" from "+position); sendOut(x);
                return null;
            }
        };

        bb_settings.BLOCKING = true;

        ff_pipeline all = new ff_pipeline(new ff_node(Emitter),new ff_node(Filter1));

        customWatch w = new customWatch();
        w.start();
        all.start();
        all.join();
        w.end();
        w.printReport(true);
    }
}
