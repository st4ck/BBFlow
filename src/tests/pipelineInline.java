package tests;

import bbflow.*;

import java.util.LinkedList;

/**
 * example of pipeline usage with 2 stages
 * Stage1: a farm with n_workers
 * Stage2: a node receiving data from collector and printing to the output
 */
public class pipelineInline {
    public static void main (String[] args) {
        int bufferSize = 16;
        int n_workers = 4;

        bb_settings.BOUNDED = true;
        bb_settings.BLOCKING = true;

        ff_queue<Integer> input_data = new ff_queue<>(bb_settings.BLOCKING, bb_settings.BOUNDED, bufferSize);

        defaultWorker<Integer,Double> workerJob = new defaultWorker<>() {
            Double mysum = 1.0;
            boolean op = true;
            public Double runJob(Integer x) {
                if (x > 0) {
                    if (op) { mysum *= x; }
                    else { mysum /= x; }
                }
                return mysum;
            }
        };

        defaultJob<Double, String> outNode = new defaultJob<>() {
            Double sum = 0.0;
            Integer elements = 0;

            public String runJob(Double x) {
                elements++;
                sum += x;
                return null;
            }

            public void EOS() {
                System.out.println("Final integer value: "+sum.intValue() + " of "+elements+" elements");
            }
        };

        ff_farm stage1 = new ff_farm<>(n_workers, workerJob, defaultEmitter.ROUNDROBIN, defaultCollector.FIRSTCOME, 16);
        ff_node stage2 = new ff_node<>(outNode);

        ff_pipeline<Integer,String> pipe = new ff_pipeline<>(stage1,stage2,bufferSize);
        pipe.addInputChannel(input_data);
        pipe.addOutputChannel(new ff_queue<String>());
        pipe.start();

        for (int i = 0; i < 10000; i++) {
            input_data.put(i);
        }
        input_data.setEOS(); // sending EOF

        pipe.join();
    }
}
