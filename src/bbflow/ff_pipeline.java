package bbflow;

import java.util.LinkedList;

/**
 * Pipeline building block allows auto-connection between all types of ff_blocks
 */
public class ff_pipeline<T,V> extends ff_node<T,V> {
    pipeline_generic pipe = null;
    int bufferSize = bb_settings.defaultBufferSize;

    public static byte TYPE_1_1 = 0;
    public static byte TYPE_1xN = 1;
    public static byte TYPE_Nx1 = 2;
    public static byte TYPE_N_N = 3;
    public static byte TYPE_NxM = 4;

    public ff_pipeline(block<T,Object> b1, block<Object,V> b2) {
        this(b1,b2,TYPE_1_1);
    }

    public ff_pipeline(block<T,Object> b1, block<Object,V> b2, int bufferSize) {
        this(b1,b2,bufferSize,TYPE_1_1);
    }

    public ff_pipeline(block<T,Object> b1, block<Object,V> b2, byte MULTI) {
        pipeline_generic<T,Object,V> p = new pipeline_generic<>(bufferSize);
        if (MULTI != TYPE_1_1) {
            p.createPipeMulti(b1, b2, MULTI);
        } else {
            p.createPipe(b1, b2);
        }
        pipe = p;
    }

    public ff_pipeline(block<T,Object> b1, block<Object,V> b2, int bufferSize, byte MULTI) {
        this.bufferSize = bufferSize;
        pipeline_generic<T,Object,V> p = new pipeline_generic<>(bufferSize);
        if (MULTI != TYPE_1_1) {
            p.createPipeMulti(b1, b2, MULTI);
        } else {
            p.createPipe(b1, b2);
        }
        pipe = p;
    }

    public void appendBlock(block<Object,Object> newblock, byte MULTI) {
        if (pipe != null) {
            pipe.appendBlock(newblock, MULTI);
        }
    }

    public void addInputChannel(ff_queue<T> input) {
        if (pipe != null) {
            pipe.addInputChannel(input);
        }
    }

    public void addOutputChannel(ff_queue<V> output) {
        if (pipe != null) {
            pipe.addOutputChannel(output);
        }
    }

    public void start() {
        if (pipe != null) {
            pipe.start();
        }
    }

    public void join() {
        if (pipe != null) {
            pipe.join();
        }
    }

    public ff_farm getFirstFarm() {
        if (pipe == null) {
            return null;
        }

        return pipe.getFirstFarm();
    }

    public ff_farm getLastFarm() {
        if (pipe == null) {
            return null;
        }

        return pipe.getLastFarm();
    }
}
