package tests;

import bbflow.defaultJob;
import bbflow.ff_queue;

/**
 * output node of complete_farm_test
 * @param <T> Channels type
 */
public class complete_farm_testOutnode<T> extends defaultJob<T> {
    public complete_farm_testOutnode(int id) {
        this.id = id;
    }

    @Override
    public void runJob() throws InterruptedException {
        T received;
        ff_queue<T> in_channel = in.get(0);

        received = in_channel.take();
        if (received == null) {
            in.remove(0); // removing input channel, sequence finished
            System.out.println(id + ": EOS");
            return;
        }

        System.out.println(id + ": " + (Integer) received);
    }
}
