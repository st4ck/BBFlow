package tests;

import bbflow.*;

import java.util.LinkedList;

/**
 * example of pipeline usage with 2 stages
 * Stage1: a farm with n_workers
 * Stage2: a node receiving data from collector and printing to the output
 */
public class pipeline_farm_node {
    public static void main (String[] args) {
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
        ff_node stage2 = new ff_node<Integer>(new complete_farm_testOutnode<Integer>(1));

        ff_pipeline<Integer> pipe = new ff_pipeline<Integer>(bufferSize);
        pipe.appendNewBB(stage1);
        pipe.appendNewBB(stage2);
        pipe.addInputChannel(input_data);
        pipe.addOutputChannel(new ff_queue<Integer>());
        pipe.start();

        try {
            for (int i = 0; i < 10000; i++) {
                input_data.put(i);
            }
            input_data.setEOS(); // sending EOF
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        pipe.join();
    }
}
