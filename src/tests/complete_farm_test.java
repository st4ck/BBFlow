package tests;

import bbflow.*;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

public class complete_farm_test {
    public static void main (String[] args) {
        int EOF = -1;
        int bufferSize = 16;

        LinkedBlockingQueue<Integer> input_data = new LinkedBlockingQueue<Integer>(bufferSize);
        LinkedBlockingQueue<Integer> farm_outnode = new LinkedBlockingQueue<Integer>(bufferSize);

        LinkedList<defaultJob<Integer>> worker_job = new LinkedList<>();
        int n_workers = 4;
        for (int i=0; i<n_workers; i++) {
            worker_job.add(new complete_farm_testWorker<Integer>(i, EOF));
        }

        ff_farm x = new ff_farm<Integer>(worker_job, EOF, defaultEmitter.ROUNDROBIN, defaultCollector.FIRSTCOME, 16);
        x.addInputChannel(input_data);
        x.addOutputChannel(farm_outnode);

        ff_node y = new ff_node<Integer>(new complete_farm_testOutnode<Integer>(1,EOF));
        y.addInputChannel(farm_outnode);
        y.addOutputChannel(new LinkedBlockingQueue<Integer>());

        x.start();
        y.start();

        try {
            for (int i = 0; i < 10000; i++) {
                input_data.put(i);
            }
            input_data.put(-1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        x.join();
    }
}
