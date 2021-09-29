package tests;

import bbflow.defaultJob;

import java.util.concurrent.LinkedBlockingQueue;

public class complete_farm_testWorker<T> extends defaultJob<T> {
    public complete_farm_testWorker(int id, T EOF) {
        this.id = id;
        this.EOF = EOF;
    }

    Integer mysum = 0;
    @Override
    public void runJob() throws InterruptedException {
        T received;
        LinkedBlockingQueue<T> in_channel = in.get(0);
        LinkedBlockingQueue<T> out_channel = out.get(0);

        received = in_channel.take();
        if (received == EOF) {
            in.remove(0); // removing input channel, sequence finished
            out_channel.put(received);
        } else {
            mysum += (Integer) received;
            out_channel.put((T)mysum);
        }

        //System.out.println(id + ": (" + (Integer) received + ") " + mysum);
    }
}
