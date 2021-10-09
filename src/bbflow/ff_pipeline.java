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
public class ff_pipeline<T,V> extends block<T,V> {
    LinkedList<pipeline_generic> pipe;
    int bufferSize = bb_settings.defaultBufferSize;

    public ff_pipeline(block<T,Object> b1, block<Object,V> b2) {
        pipeline_generic<T,Object,V> p = new pipeline_generic<>(bufferSize);
        p.createPipe(b1,b2);
        pipe = new LinkedList<>();
        pipe.add(p);
    }

    public ff_pipeline(block<T,Object> b1, block<Object,V> b2, int bufferSize) {
        this.bufferSize = bufferSize;
        pipeline_generic<T,Object,V> p = new pipeline_generic<>(bufferSize);
        p.createPipe(b1,b2);
        pipe = new LinkedList<>();
        pipe.add(p);
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
