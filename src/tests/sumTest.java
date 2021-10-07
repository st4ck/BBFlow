package tests;

import bbflow.*;

import java.util.LinkedList;

/**
 * Test class with a simple farm and sum of sequential numbers
 */
public class sumTest {
    public static void main (String[] args) {
        int bufferSize = 16;

        ff_queue<Integer> input_data = new ff_queue<Integer>(bb_settings.BLOCKING,bb_settings.BOUNDED, bufferSize);

        LinkedList<defaultJob<Integer,Integer>> worker_job = new LinkedList<>();
        int n_workers = 4;
        for (int i=0; i<n_workers; i++) {
            worker_job.add(new sumTestWorker<Integer,Integer>(i));
        }

        ff_farm x = new ff_farm<Integer,Integer>(worker_job, defaultEmitter.ROUNDROBIN, defaultCollector.FIRSTCOME, bufferSize);
        x.addInputChannel(input_data);
        x.start();

        try {
            for (int i = 0; i < 4; i++) {
                input_data.put(i);
            }
            input_data.setEOS();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        x.join();
    }
}
