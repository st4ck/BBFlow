package tests;
import bbflow.*;
import bbflow_network.*;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;

public class networkTest {
    public static void main (String[] args) throws InterruptedException, IOException {
        int bufferSize = 16;

        bb_settings.BOUNDED = true;
        bb_settings.BLOCKING = true;

        ff_queue<Integer> input_data = new ff_queue<>(bb_settings.BLOCKING, bb_settings.BOUNDED, bufferSize);

        LinkedList<defaultJob<Integer>> worker_job = new LinkedList<>();
        int n_workers = 4;
        for (int i=0; i<n_workers; i++) {
            worker_job.add(new complete_farm_testWorker<Integer>(i));
        }

        ff_farm stage1 = new ff_farm<Integer>(worker_job, defaultEmitter.ROUNDROBIN, defaultCollector.FIRSTCOME, bufferSize);
        ff_node stage2 = new ff_node<Integer>(new complete_farm_testOutnode<Integer>(15));

        stage1.addOutputChannel(new ff_queue_TCP(ff_queue_TCP.OUTPUT,1, "127.0.0.1"));
        stage2.addInputChannel(new ff_queue_TCP(ff_queue_TCP.INPUT, 1));

        stage1.addInputChannel(input_data);
        stage2.addOutputChannel(input_data);

        stage1.start();
        stage2.start();

        try {
            for (int i = 0; i < 10000; i++) {
                input_data.put(i);
            }
            input_data.setEOS(); // sending EOF
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        stage1.join();
        stage2.join();
    }
}
