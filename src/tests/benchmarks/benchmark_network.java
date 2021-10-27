package tests.benchmarks;

import bbflow.*;
import bbflow_network.*;

/**
 * Benchmark of two nodes connected each other with a network channel (TCP)
 */
public class benchmark_network {
    public static void main (String[] args) {
        preloader.preloadJVM();

        int n = 1000;
        if (args.length > 0) {
            n = Integer.parseInt(args[0]);
            if (args.length == 2) {
                objectClient.flushThreshold = Long.parseLong(args[1]);
            }
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
        ff_node E = new ff_node(Emitter);
        ff_node F = new ff_node(Filter1);

        E.addOutputChannel(new ff_queue_TCP(ff_queue_TCP.OUTPUT,1, "127.0.0.1"));
        F.addInputChannel(new ff_queue_TCP(ff_queue_TCP.INPUT, 1));
        F.addOutputChannel(new ff_queue());

        customWatch w = new customWatch();
        w.start();
        E.start();
        F.start();
        F.join();
        E.join();
        w.end();
        w.printReport(true);
    }
}
