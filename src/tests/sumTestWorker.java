package tests;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import bbflow.*;


/**
 * worker code extending bbflow.defaultJob for the test of the farm
 * @param <T>
 */
public class sumTestWorker<T> extends defaultJob<T> {
    public sumTestWorker(int id) {
        this.id = id;
    }

    Integer mysum = 0;
    @Override
    public void runJob() throws InterruptedException {
        T received;
        ff_queue<T> in_channel = in.get(0);

        received = in_channel.take();
        if (received == null) {
            System.out.println(id + ": EOS");
            in.remove(0); // removing input channel, sequence finished
            return;
        }

        mysum += (Integer) received;

        System.out.println(id + ": (" + (Integer) received + ") " + mysum);
    }
}
