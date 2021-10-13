package bbflow;

import java.util.LinkedList;

/**
 * Pipeline building block allows auto-connection between all types of ff_blocks
 */
public class ff_pipeline<T,V> extends block<T,V> {
    LinkedList<pipeline_generic> pipe;
    int bufferSize = bb_settings.defaultBufferSize;

    public ff_pipeline(block<T,Object> b1, block<Object,V> b2) {
        this(b1,b2,false);
    }

    public ff_pipeline(block<T,Object> b1, block<Object,V> b2, int bufferSize) {
        this(b1,b2,bufferSize,false);
    }

    public ff_pipeline(block<T,Object> b1, block<Object,V> b2, boolean MULTI) {
        pipeline_generic<T,Object,V> p = new pipeline_generic<>(bufferSize);
        if (MULTI) {
            p.createPipeMulti(b1, b2);
        } else {
            p.createPipe(b1, b2);
        }
        pipe = new LinkedList<>();
        pipe.add(p);
    }

    public ff_pipeline(block<T,Object> b1, block<Object,V> b2, int bufferSize, boolean MULTI) {
        this.bufferSize = bufferSize;
        pipeline_generic<T,Object,V> p = new pipeline_generic<>(bufferSize);
        if (MULTI) {
            p.createPipeMulti(b1, b2);
        } else {
            p.createPipe(b1, b2);
        }
        pipe = new LinkedList<>();
        pipe.add(p);
    }

    public ff_pipeline<T, Object> appendBlock(block<V,Object> x) {
        return new ff_pipeline<T,Object>((block<T, Object>) this, ((block<Object,Object>) x));
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

    public ff_farm getFirstFarm() {
        if (pipe.size() == 0) {
            return null;
        }

        return pipe.getFirst().getFirstFarm();
    }

    public ff_farm getLastFarm() {
        if (pipe.size() == 0) {
            return null;
        }

        return pipe.getLast().getLastFarm();
    }
}
