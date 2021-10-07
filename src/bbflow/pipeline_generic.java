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
public class pipeline_generic<T,U,V> extends block<T,V> {
    LinkedList<block> pipe;
    int bufferSize;

    public pipeline_generic(int bufferSize) {
        pipe = new LinkedList<>();
        this.bufferSize = bufferSize;
    }

    public void createPipe(block<T,U> b1, block<U,V> b2, boolean BLOCKING, boolean BOUNDED) {
        if (pipe.size() > 0) {
            ff_queue<U> channel = new ff_queue<>(BLOCKING,BOUNDED,this.bufferSize);
            b1.addOutputChannel(channel);
            b2.addInputChannel(channel);
        }

        pipe.add(b1);
        pipe.add(b2);
    }

    public void createPipe(block<T,U> b1, block<U,V> b2) {
        createPipe(b1,b2,bb_settings.BLOCKING, bb_settings.BOUNDED);
    }

    public void addInputChannel(ff_queue<T> input) {
        if (pipe.size() > 0) {
            pipe.getFirst().addInputChannel(input);
        }
    }

    public void addOutputChannel(ff_queue<V> output) {
        if (pipe.size() > 0) {
            pipe.getLast().addOutputChannel(output);
        }
    }

    public void start() {
        for (int i=0; i<pipe.size(); i++) {
            pipe.get(i).start();
        }
    }

    public void join() {
        for (int i = 0; i < pipe.size(); i++) {
            pipe.get(i).join();
        }
    }
}
