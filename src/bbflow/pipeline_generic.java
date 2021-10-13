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
        connect(b1,b2,BLOCKING,BOUNDED);
        pipe.add(b1);
        pipe.add(b2);
    }

    public void connect(block<T,U> b1, block<U,V> b2, boolean BLOCKING, boolean BOUNDED) {
        ff_queue<U> channel = new ff_queue<>(BLOCKING,BOUNDED,this.bufferSize);

        b1.addOutputChannel(channel);
        b2.addInputChannel(channel);
    }

    public void createPipe(block<T,U> b1, block<U,V> b2) {
        createPipe(b1,b2,bb_settings.BLOCKING, bb_settings.BOUNDED);
    }

    public void createPipeMulti(block<T,U> b1, block<U,V> b2, boolean BLOCKING, boolean BOUNDED) {
        pipe.add(b1);
        pipe.add(b2);

        connectPipeMulti(b1,b2,BLOCKING,BOUNDED);
    }

    public void connectPipeMulti(block<T,U> b1, block<U,V> b2, boolean BLOCKING, boolean BOUNDED) {
        if (b1 instanceof ff_farm) {
            if (((ff_farm<T, U>) b1).collector != null) { // there is collector, normal pipeline single channel
                connect(b1,b2,BLOCKING,BOUNDED);
            } else { // no collector, so n channels (= #workers)
                int b1_size = ((ff_farm<T, U>) b1).workers.size();

                //now we need to determine size of b2 on first layer
                if (b2 instanceof ff_farm) {
                    if (((ff_farm<U, V>) b2).emitter != null) {
                        connect(b1,b2,BLOCKING,BOUNDED);
                    } else {
                        if (b1_size != ((ff_farm<U, V>) b2).workers.size()) { // wrong cardinality!
                            return;
                        } else {
                            for (int i=0; i<b1_size; i++) { // connect all workers 1 to 1
                                connect(((ff_farm<T, U>) b1).workers.get(i),((ff_farm<U, V>) b2).workers.get(i),BLOCKING,BOUNDED);
                            }
                        }
                    }
                } else if (b2 instanceof ff_all2all) {
                    if (((ff_all2all<?, ?, ?, ?>) b2).a2a.size() > 0) {
                        connectPipeMulti(b1, ((ff_all2all<?, ?, ?, ?>) b2).a2a.getFirst(), BLOCKING, BOUNDED);
                    }

                    return;
                } else if ((b2 instanceof ff_pipeline) || (b2 instanceof ff_comb)) {
                    ff_farm ret = ((ff_pipeline<U,V>) b2).getFirstFarm();
                    if (ret != null) {
                        connectPipeMulti(b1, ret, BLOCKING, BOUNDED);
                    } else {
                        connect(b1,b2,BLOCKING,BOUNDED);
                    }

                    return;
                }
            }
        } else if (b1 instanceof ff_all2all) {
            if (((ff_all2all<?, ?, ?, ?>) b1).a2a.size() > 0) {
                connectPipeMulti(((ff_all2all<?, ?, ?, ?>) b1).a2a.getLast(), b2, BLOCKING, BOUNDED);
            }

            return;
        } else if ((b1 instanceof ff_pipeline) || (b1 instanceof ff_comb)) {
            ff_farm ret = ((ff_pipeline<T, U>) b1).getLastFarm();
            if (ret != null) {
                connectPipeMulti(ret, b2, BLOCKING, BOUNDED);
            } else {
                connect(b1,b2,BLOCKING,BOUNDED);
            }

            return;
        } else {
            connect(b1,b2,BLOCKING,BOUNDED);
        }
    }

    public void createPipeMulti(block<T,U> b1, block<U,V> b2) {
        createPipeMulti(b1,b2,bb_settings.BLOCKING, bb_settings.BOUNDED);
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

        if (pipe.getFirst() instanceof ff_farm) {
            return (ff_farm) pipe.getFirst();
        } else if (pipe.getFirst() instanceof ff_all2all) {
            if (((ff_all2all<?, ?, ?, ?>) pipe.getFirst()).a2a.size() > 0) {
                return (ff_farm) ((ff_all2all<?, ?, ?, ?>) pipe.getFirst()).a2a.getFirst();
            } else {
                return null;
            }
        } else if (pipe.getFirst() instanceof ff_pipeline) {
            return ((ff_pipeline<?, ?>) pipe.getFirst()).getFirstFarm();
        } else if (pipe.getFirst() instanceof ff_comb) {
            return ((ff_pipeline<?, ?>) pipe.getFirst()).getFirstFarm();
        }

        return null;
    }

    public ff_farm getLastFarm() {
        if (pipe.size() == 0) {
            return null;
        }

        if (pipe.getLast() instanceof ff_farm) {
            return (ff_farm) pipe.getLast();
        } else if (pipe.getLast() instanceof ff_all2all) {
            if (((ff_all2all<?, ?, ?, ?>) pipe.getLast()).a2a.size() > 0) {
                return (ff_farm) ((ff_all2all<?, ?, ?, ?>) pipe.getLast()).a2a.getLast();
            } else {
                return null;
            }
        } else if (pipe.getLast() instanceof ff_pipeline) {
            return ((ff_pipeline<?, ?>) pipe.getLast()).getLastFarm();
        } else if (pipe.getLast() instanceof ff_comb) {
            return ((ff_pipeline<?, ?>) pipe.getLast()).getLastFarm();
        }

        return null;
    }
}
