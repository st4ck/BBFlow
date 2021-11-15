package tests;

import bbflow.*;

import java.util.LinkedList;

/**
 * ff_farm test with an output node
 */
public class complete_farm_test {
    public static void main (String[] args) {
        int bufferSize = 16;

        ff_queue<Integer> input_data = new ff_queue<>(bb_settings.BLOCKING, bb_settings.BOUNDED, bufferSize);
        ff_queue<Integer> farm_outnode = new ff_queue<Integer>(bb_settings.BLOCKING, bb_settings.BOUNDED, bufferSize);

        LinkedList<defaultJob<Integer,Integer>> worker_job = new LinkedList<>();
        int n_workers = 4;
        for (int i=0; i<n_workers; i++) {
            worker_job.add(new complete_farm_testWorker<Integer,Integer>(i));
        }

        ff_farm x = new ff_farm<Integer,Integer>(worker_job, defaultEmitter.ROUNDROBIN, defaultCollector.FIRSTCOME, 16);
        x.addInputChannel(input_data);
        x.addOutputChannel(farm_outnode);

        ff_node y = new ff_node<Integer,Integer>(new complete_farm_testOutnode<Integer,Integer>(1));
        y.addInputChannel(farm_outnode);

        x.start();
        y.start();

        for (int i = 0; i < 10000; i++) {
            input_data.put(i);
        }
        input_data.setEOS(); // sending EOF

        x.join();
    }
}
