package tests;

import bbflow.defaultJob;

import java.util.concurrent.LinkedBlockingQueue;

public class complete_farm_testOutnode<T> extends defaultJob<T> {
    public complete_farm_testOutnode(int id, T EOF) {
        this.id = id;
        this.EOF = EOF;
    }

    @Override
    public void runJob() throws InterruptedException {
        T received;
        LinkedBlockingQueue<T> in_channel = in.get(0);

        received = in_channel.take();
        if (received == EOF) {
            in.remove(0); // removing input channel, sequence finished
            System.out.println(id + ": EOF");
            return;
        }

        System.out.println(id + ": " + (Integer) received);
    }
}
