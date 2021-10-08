package tests;

import bbflow.*;

/**
 * ff_farm test with an output node
 */
public class farmInline {
    public static void main (String[] args) throws InterruptedException {
        int bufferSize = 16;
        int n_workers = 4;

        ff_queue<Integer> input_data = new ff_queue<>(bb_settings.BLOCKING, bb_settings.BOUNDED, bufferSize);
        ff_queue<Double> farm_outnode = new ff_queue<>(bb_settings.BLOCKING, bb_settings.BOUNDED, bufferSize);

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

            /*public void runJobMulti(Double x, LinkedList<ff_queue<Integer>> out) {
                elements++;
                sum += x;

                out.get(0).put(sum.intValue());
                //out.get(1).put(elements);
            }*/
        };

        ff_farm x = new ff_farm<>(n_workers, workerJob, defaultEmitter.ROUNDROBIN, defaultCollector.FIRSTCOME, 16);
        x.addInputChannel(input_data);
        x.addOutputChannel(farm_outnode);
        ff_node y = new ff_node<>(outNode);
        y.addInputChannel(farm_outnode);
        y.addOutputChannel(new ff_queue<Integer>());

        x.start();
        y.start();

        for (int i = 0; i < 10000; i++) {
            input_data.put(i);
        }
        input_data.setEOS(); // sending EOF

        x.join();
    }
}
