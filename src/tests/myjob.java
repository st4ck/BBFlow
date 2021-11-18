package tests;

import bbflow.defaultJob;
import bbflow.ff_queue;

/**
 * a defaultJob used for the thesis
 */
public class myjob extends defaultJob<Long,Long> {
    public void runJob() throws InterruptedException {
        Long received;
        ff_queue<Long> in_channel = in.get(0);

        received = in_channel.take();
        if (received == null) { // EOS received
            in.remove(0); // Removing input channel
            return;
        }

        received += 2;
        sendOut(received);
    }
}
