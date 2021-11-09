package tests.benchmarks.distributed;

import bbflow.*;
import bbflow_network.*;

/**
 * Benchmark of two nodes connected each other with a network channel (TCP)
 */
public class benchmark_network {
    public static void main (String[] args) {
        preloader.preloadJVM();

        int n = 1000;
        int start_nodes = -1;
        String host = "127.0.0.1";
        if (args.length > 0) {
            n = Integer.parseInt(args[0]);

            if (args.length > 1) {
                start_nodes = Integer.parseInt(args[1]); // node to start, [0,1]. -1 for both
                if (args.length > 2) {
                    host = args[2]; // host for the client node
                    if (args.length > 3) {
                        objectClient.flushThreshold = Long.parseLong(args[1]);
                    }
                }

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

        boolean startE = false;
        boolean startF = false;

        if ((start_nodes == -1) || (start_nodes == 0)) startE = true;
        if ((start_nodes == -1) || (start_nodes == 1)) startF = true;

        if (startE) E.addOutputChannel(new ff_queue_TCP(ff_queue_TCP.OUTPUT, 1, host));
        if (startF) F.addInputChannel(new ff_queue_TCP(ff_queue_TCP.INPUT, 1));
        F.addOutputChannel(new ff_queue());

        customWatch w = new customWatch();
        w.start();

        if (startE) E.start();
        if (startF) F.start();
        if (startF) F.join();
        if (startE) E.join();

        w.end();
        w.printReport(true);
    }
}
