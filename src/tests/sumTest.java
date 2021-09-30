package tests;

import bbflow.*;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Test class with a simple farm and sum of sequential numbers
 */
public class sumTest {
    public static void main (String[] args) {
        int EOF = -1;
        int bufferSize = 16;

        ff_queue<Integer> input_data = new ff_queue<Integer>(ff_queue.BLOCKING,ff_queue.BOUNDED,bufferSize);

        LinkedList<defaultJob<Integer>> worker_job = new LinkedList<>();
        int n_workers = 4;
        for (int i=0; i<n_workers; i++) {
            worker_job.add(new sumTestWorker<Integer>(i));
        }

        ff_farm x = new ff_farm<Integer>(worker_job, defaultEmitter.ROUNDROBIN, defaultCollector.FIRSTCOME, 16);
        x.addInputChannel(input_data);
        x.start();

        try {
            for (int i = 0; i < 10000; i++) {
                input_data.put(i);
            }
            input_data.setEOS();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        x.join();
    }
}
