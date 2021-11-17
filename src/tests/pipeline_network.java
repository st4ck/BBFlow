package tests;

import bbflow.*;
import bbflow_network.*;

/**
 * Two nodes connected each other with a network channel (TCP)
 */
public class pipeline_network {
    public static void main (String[] args) {
        preloader.preloadJVM();

        boolean node1 = false;
        String host;
        if (args.length < 2) {
            System.out.println("Please specify which node to start and the host");
            return;
        } else {
            if (Integer.parseInt(args[0]) == 0) { node1 = true; }
            host = args[1]; // host for the client node
        }

        defaultWorker<Long, Long> Node1 = new defaultWorker<>() {
            public Long runJob(Long x) { return null; }

            public void init() {
                for (long i = 1; i <=100; ++i)  sendOut(i);
                sendEOS();
            }
        };

        defaultWorker<Long, Long> Node2 = new defaultWorker<>() {
            public Long runJob(Long x) {
                x *= 2;
                System.out.println("Received "+x+" from "+position);
                return null;
            }
        };


        ff_node E = new ff_node(Node1);
        ff_node F = new ff_node(Node2);

        if (node1) E.addOutputChannel(new ff_queue_TCP(ff_queue_TCP.OUTPUT, 1, host));
        if (!node1) F.addInputChannel(new ff_queue_TCP(ff_queue_TCP.INPUT, 1));

        if (node1) { E.start(); E.join(); }
        else { F.start(); F.join(); }
    }
}
