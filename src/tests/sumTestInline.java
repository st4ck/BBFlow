package tests;

import bbflow.*;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Test class with a simple farm and sum of sequential numbers
 */
public class sumTestInline {
    public static void main (String[] args) throws InterruptedException {
        int bufferSize = 16;
        int n_workers = 4;

        defaultWorker<Integer,Integer> workerJob = new defaultWorker<>() {
            Integer mysum = 0;
            public Integer runJob(Integer x) {
                mysum += x;
                System.out.println(mysum);
                return mysum;
            }
        };

        ff_farm x = new ff_farm<Integer,Integer>(n_workers, workerJob, defaultEmitter.ROUNDROBIN, defaultCollector.FIRSTCOME, bufferSize);
        ff_queue<Integer> input_data = new ff_queue<Integer>(bb_settings.BLOCKING,bb_settings.BOUNDED, bufferSize);
        x.addInputChannel(input_data);
        x.start();

        for (int i = 0; i < 4; i++) {
            input_data.put(i);
        }
        input_data.setEOS();

        x.join();
    }
}
