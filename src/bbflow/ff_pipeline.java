package bbflow;

 /* Example
     *  *     |---------- 3-stage pipeline -----------|
     *  *     |                   |                   |
     *  *     |                   v                   |
     *  *     |            |(stage2_1->stage2_2)|     |
     *  *     v            |                    |     v
     *  *    stage1-->farm |(stage2_1->stage2_2)|-->stage3
     *  *                  |                    |
     *  *                  |(stage2_1->stage2_2)|
     *  *                    ^                ^
     *  *                    |- Farm -|
     */

import java.util.LinkedList;

/**
 * Pipeline building block allows auto-connection between all types of ff_blocks
 */
public class ff_pipeline<T> extends block<T> {
    LinkedList<block<T>> pipe;
    int bufferSize = bb_settings.defaultBufferSize;

    public ff_pipeline() {
        pipe = new LinkedList<>();
    }

    public ff_pipeline(int bufferSize) {
        pipe = new LinkedList<>();
        this.bufferSize = bufferSize;
    }

    public void appendNewBB(block<T> b) {
        if (pipe.size() > 0) {
            block<T> lastElement = pipe.getLast();
            ff_queue<T> channel = new ff_queue<>(bb_settings.BLOCKING,bb_settings.BOUNDED,this.bufferSize);
            lastElement.addOutputChannel(channel);
            b.addInputChannel(channel);
        }

        pipe.add(b);
    }

    public void addInputChannel(ff_queue<T> input) {
        if (pipe.size() > 0) {
            pipe.getFirst().addInputChannel(input);
        }
    }

    public void addOutputChannel(ff_queue<T> output) {
        if (pipe.size() > 0) {
            pipe.getLast().addOutputChannel(output);
        }
    }

    public void start() {
        for (int i=0; i<pipe.size(); i++) {
            pipe.get(i).start();
        }
    }

    public void join() throws InterruptedException {
        for (int i=0; i<pipe.size(); i++) {
            pipe.get(i).join();
        }
    }
}
