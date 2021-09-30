package tests;

import bbflow.*;

import java.util.LinkedList;

public class complete_farm_test {
    public static void main (String[] args) {
        int bufferSize = 16;

        ff_queue<Integer> input_data = new ff_queue<>(bb_settings.BLOCKING, bb_settings.BOUNDED, bufferSize);
        ff_queue<Integer> farm_outnode = new ff_queue<Integer>(bb_settings.BLOCKING, bb_settings.BOUNDED, bufferSize);

        LinkedList<defaultJob<Integer>> worker_job = new LinkedList<>();
        int n_workers = 4;
        for (int i=0; i<n_workers; i++) {
            worker_job.add(new complete_farm_testWorker<Integer>(i));
        }

        ff_farm x = new ff_farm<Integer>(worker_job, defaultEmitter.ROUNDROBIN, defaultCollector.FIRSTCOME, 16);
        x.addInputChannel(input_data);
        x.addOutputChannel(farm_outnode);

        ff_node y = new ff_node<Integer>(new complete_farm_testOutnode<Integer>(1));
        y.addInputChannel(farm_outnode);
        y.addOutputChannel(new ff_queue<Integer>());

        x.start();
        y.start();

        try {
            for (int i = 0; i < 10000; i++) {
                input_data.put(i);
            }
            input_data.setEOS(); // sending EOF
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        x.join();
    }
}
