package tests.benchmarks.distributed;

import bbflow.*;
import bbflow_network.*;

/**
 * Benchmark of a farm with a network channels (TCP)
 */
public class benchmark_network_farm {
    public static void main (String[] args) {
        preloader.preloadJVM();

        int n = 1000;
        int start_nodes = 0;
        String host_EC = "127.0.0.1";
        String host_W = "127.0.0.1";
        if (args.length > 0) {
            n = Integer.parseInt(args[0]);
            if (args.length > 1) {
                start_nodes = Integer.parseInt(args[1]); // node to start, [0,1]. 0=E+C, 1=Ws, -1 for all
                if (args.length > 2) {
                    host_EC = args[2]; // host for the client node
                    if (args.length > 4) {
                        host_W = args[3];
                        if (args.length > 3) {
                            objectClient.flushThreshold = Long.parseLong(args[4]);
                        }
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

        defaultWorker<Long, Long> Worker = new defaultWorker<>() {
            public Long runJob(Long x) {
                x += id;
                return x;
            }
        };

        boolean startEC = false;
        boolean startW = false;

        if ((start_nodes == -1) || (start_nodes == 0)) startEC = true;
        if ((start_nodes == -1) || (start_nodes == 1)) startW = true;

        int n_workers = 3;

        ff_farm farm = new ff_farm<Long,Long>(n_workers, Worker);
        farm.removeEmitter();
        farm.removeCollector();
        if (startEC) {
            farm.emitter = new ff_node(Emitter);
            farm.collector = new ff_node(new defaultCollector<Long>());
            farm.collector.addOutputChannel(new ff_queue());
        }

        // connect emitter, workers and collector with network channels
        for (int i=0; i<n_workers; i++) {
            if (startEC) {
                farm.emitter.addOutputChannel(new ff_queue_TCP(ff_queue_TCP.OUTPUT, i, host));
                farm.collector.addInputChannel(new ff_queue_TCP(ff_queue_TCP.INPUT, i + n_workers));
            }

            if (startW) {
                ((ff_node<Long, Long>) farm.workers.get(i)).addInputChannel(new ff_queue_TCP(ff_queue_TCP.INPUT, i));
                ((ff_node<Long, Long>) farm.workers.get(i)).addOutputChannel(new ff_queue_TCP(ff_queue_TCP.OUTPUT, i + n_workers, host));
            }
        }

        customWatch w = new customWatch();
        w.start();
        farm.start();
        farm.join();
        w.end();
        w.printReport(true);
    }
}
