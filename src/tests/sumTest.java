package tests;

import bbflow.*;

import java.util.LinkedList;

/**
 * Test class with a simple farm and sum of sequential numbers
 */
public class sumTest {
    public static void main (String[] args) {
        int EOF = -1;
        LinkedList<Integer> input_data = new LinkedList<Integer>();
        for (int i=0; i<100000; i++) {
            input_data.add(i);
        }
        input_data.add(-1);

        LinkedList<defaultJob<Integer>> worker_job = new LinkedList<>();
        int n_workers = 4;
        for (int i=0; i<n_workers; i++) {
            worker_job.add(new sumTestWorker<Integer>(i, EOF));
        }

        ff_farm x = new ff_farm<Integer>(worker_job, EOF, defaultEmitter.ROUNDROBIN);
        x.addInputChannel(input_data);
        x.run();
    }
}
