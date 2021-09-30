package tests;

import bbflow.defaultJob;
import bbflow.ff_queue;

import java.util.concurrent.LinkedBlockingQueue;

public class complete_farm_testWorker<T> extends defaultJob<T> {
    public complete_farm_testWorker(int id) {
        this.id = id;
    }

    Integer mysum = 0;
    @Override
    public void runJob() throws InterruptedException {
        T received;
        ff_queue<T> in_channel = in.get(0);
        ff_queue<T> out_channel = out.get(0);

        received = in_channel.take();
        if (received == null) { // EOS
            in.remove(0); // removing input channel, sequence finished
            out_channel.setEOS();
        } else {
            mysum += (Integer) received;
            out_channel.put((T)mysum);
        }

        //System.out.println(id + ": (" + (Integer) received + ") " + mysum);
    }
}
