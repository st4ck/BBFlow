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

    public static void preload() {
    }

    /**
     * connect 1-1 the two blocks and add to pipeline
     * @param b1 first block
     * @param b2 second block
     * @param BLOCKING type of channel
     * @param BOUNDED type of channel
     */
    public void createPipe(block<T,U> b1, block<U,V> b2, boolean BLOCKING, boolean BOUNDED) {
        connect(b1,b2,BLOCKING,BOUNDED);
        pipe.add(b1);
        pipe.add(b2);
    }

    /**
     * connect 1-1 the two blocks
     * @param b1 first block
     * @param b2 second block
     * @param BLOCKING type of channel
     * @param BOUNDED type of channel
     */
    public void connect(block<T,U> b1, block<U,V> b2, boolean BLOCKING, boolean BOUNDED) {
        ff_queue<U> channel = new ff_queue<>(BLOCKING,BOUNDED,this.bufferSize);

        b1.addOutputChannel(channel);
        b2.addInputChannel(channel);
    }

    /**
     * connect 1-1 the two blocks and add to pipeline
     * @param b1 first block
     * @param b2 second block
     */
    public void createPipe(block<T,U> b1, block<U,V> b2) {
        createPipe(b1,b2,bb_settings.BLOCKING, bb_settings.BOUNDED);
    }

    /**
     * connect two blocks and add to pipeline. See ff_pipeline for reference
     * @param b1 first block
     * @param b2 second block
     * @param BLOCKING type of channel
     * @param BOUNDED type of channel
     * @param MULTI TYPE_1_1, TYPE_1_N, TYPE_Nx1, TYPE_N_N, TYPE_NxM
     */
    public void createPipeMulti(block<T,U> b1, block<U,V> b2, boolean BLOCKING, boolean BOUNDED, byte MULTI) {
        pipe.add(b1);
        pipe.add(b2);

        connectPipeMulti(b1,b2,BLOCKING,BOUNDED, MULTI);
    }

    /**
     * connect two blocks. See ff_pipeline for reference
     * @param b1 first block
     * @param b2 second block
     * @param BLOCKING type of channel
     * @param BOUNDED type of channel
     * @param MULTI TYPE_1_1, TYPE_1_N, TYPE_Nx1, TYPE_N_N, TYPE_NxM
     */
    public void connectPipeMulti(block<T,U> b1, block<U,V> b2, boolean BLOCKING, boolean BOUNDED, byte MULTI) {
        if (b1 instanceof ff_farm) {
            //now we need to determine size of b2 on first layer
            if (b2 instanceof ff_farm) {
                if (MULTI == ff_pipeline.TYPE_N_N) {
                    if (((ff_farm<T, U>) b1).collector != null) {
                        if (((ff_farm<U, V>) b2).emitter != null) {
                            connect(b1, b2, BLOCKING, BOUNDED);
                        } else {
                            connectPipeMulti(b1,b2,BLOCKING,BOUNDED,ff_pipeline.TYPE_1xN);
                        }
                        return;
                    } else if (((ff_farm<U, V>) b2).emitter != null) {
                        connectPipeMulti(b1,b2,BLOCKING,BOUNDED,ff_pipeline.TYPE_Nx1);
                        return;
                    }

                    int b1_size = ((ff_farm<T, U>) b1).workers.size();
                    if (b1_size != ((ff_farm<U, V>) b2).workers.size()) { // wrong cardinality
                        connectPipeMulti(b1,b2,BLOCKING,BOUNDED,ff_pipeline.TYPE_NxM);
                    } else {
                        for (int i = 0; i < b1_size; i++) { // connect all workers 1 to 1
                            connect(((ff_farm<T, U>) b1).workers.get(i), ((ff_farm<U, V>) b2).workers.get(i), BLOCKING, BOUNDED);
                        }
                    }
                    return;
                } else if (MULTI == ff_pipeline.TYPE_1xN) {
                    if (((ff_farm<T, U>) b1).collector == null) {
                        connectPipeMulti(b1,b2,BLOCKING,BOUNDED,ff_pipeline.TYPE_N_N);
                        return;
                    } else if (((ff_farm<T, U>) b2).emitter != null) {
                        connect(b1,b2,BLOCKING,BOUNDED);
                        return;
                    }

                    for (int i = 0; i < ((ff_farm<U, V>) b2).workers.size(); i++) {
                        connect((block<T, U>) ((ff_farm<T, U>) b1).collector, ((ff_farm<U, V>) b2).workers.get(i), BLOCKING, BOUNDED);
                    }
                } else if (MULTI == ff_pipeline.TYPE_Nx1) {
                    if (((ff_farm<U, V>) b2).emitter == null) {
                        connectPipeMulti(b1, b2, BLOCKING, BOUNDED, ff_pipeline.TYPE_N_N);
                        return;
                    } else if (((ff_farm<T, U>) b1).collector != null) {
                        connect(b1, b2, BLOCKING, BOUNDED);
                        return;
                    }

                    for (int i = 0; i < ((ff_farm<T, U>) b1).workers.size(); i++) {
                        connect(((ff_farm<T, U>) b1).workers.get(i), (block<U, V>) ((ff_farm<U, V>) b2).emitter, BLOCKING, BOUNDED);
                    }
                } else if (MULTI == ff_pipeline.TYPE_NxM) {
                    if (((ff_farm<T, U>) b1).collector != null) {
                        if (((ff_farm<U, V>) b2).emitter != null) {
                            connect(b1, b2, BLOCKING, BOUNDED);
                        } else {
                            connectPipeMulti(b1,b2,BLOCKING,BOUNDED,ff_pipeline.TYPE_1xN);
                        }
                        return;
                    } else if (((ff_farm<U, V>) b2).emitter != null) {
                        connectPipeMulti(b1,b2,BLOCKING,BOUNDED,ff_pipeline.TYPE_Nx1);
                        return;
                    }

                    for (int i = 0; i < ((ff_farm<T, U>) b1).workers.size(); i++) {
                        ((ff_farm<T, U>) b1).workers.get(i).removeOutputChannel(0); // removing channel between worker and removed collector
                        for (int j = 0; j < ((ff_farm<U, V>) b2).workers.size(); j++) {
                            ff_queue<U> b1_b2 = new ff_queue<>();
                            if (i==0) {
                                ((ff_farm<U, V>) b2).workers.get(j).removeInputChannel(0); // remove old channel between removed emitter and b2 worker
                            }
                            // connect each worker new out channel (from farm already in) to all workers of new farm
                            ((ff_farm<T, U>) b1).workers.get(i).addOutputChannel(b1_b2);
                            ((ff_farm<U, V>) b2).workers.get(j).addInputChannel(b1_b2); // U & V types must be equal in this case
                        }
                    }

                    return;
                } else {
                    connect(b1,b2,BLOCKING,BOUNDED);
                }
            } else if (b2 instanceof ff_all2all) {
                if (((ff_all2all<?, ?, ?, ?>) b2).a2a.size() > 0) {
                    connectPipeMulti(b1, ((ff_all2all<?, ?, ?, ?>) b2).a2a.getFirst(), BLOCKING, BOUNDED, MULTI);
                }

                return;
            } else if ((b2 instanceof ff_pipeline) || (b2 instanceof ff_comb)) {
                ff_farm ret = ((ff_pipeline<U,V>) b2).getFirstFarm();
                if (ret != null) {
                    connectPipeMulti(b1, ret, BLOCKING, BOUNDED, MULTI);
                } else {
                    connect(b1,b2,BLOCKING,BOUNDED);
                }

                return;
            } else {
                connect(b1,b2,BLOCKING,BOUNDED);
            }
        } else if (b1 instanceof ff_all2all) {
            if (((ff_all2all<?, ?, ?, ?>) b1).a2a.size() > 0) {
                connectPipeMulti(((ff_all2all<?, ?, ?, ?>) b1).a2a.getLast(), b2, BLOCKING, BOUNDED, MULTI);
            }

            return;
        } else if (b1 instanceof ff_pipeline) {
            ff_farm ret = ((ff_pipeline<T, U>) b1).getLastFarm();
            if (ret != null) {
                connectPipeMulti(ret, b2, BLOCKING, BOUNDED, MULTI);
            } else {
                connect(b1,b2,BLOCKING,BOUNDED);
            }

            return;
        } else {
            connect(b1,b2,BLOCKING,BOUNDED);
        }
    }

    /**
     * connect two blocks and add to pipeline. See ff_pipeline for reference
     * @param b1 first block
     * @param b2 second block
     * @param MULTI TYPE_1_1, TYPE_1_N, TYPE_Nx1, TYPE_N_N, TYPE_NxM
     */
    public void createPipeMulti(block<T,U> b1, block<U,V> b2, byte MULTI) {
        createPipeMulti(b1,b2,bb_settings.BLOCKING, bb_settings.BOUNDED, MULTI);
    }

    /**
     * add input channel to first block in pipe
     * @param input
     */
    public void addInputChannel(ff_queue<T> input) {
        if (pipe.size() > 0) {
            pipe.getFirst().addInputChannel(input);
        }
    }

    /**
     * add output channel to the last element in pipe
     * @param output
     */
    public void addOutputChannel(ff_queue<V> output) {
        if (pipe.size() > 0) {
            pipe.getLast().addOutputChannel(output);
        }
    }

    /**
     * start all blocks in pipe
     */
    public void start() {
        for (int i=0; i<pipe.size(); i++) {
            pipe.get(i).start();
        }
    }

    /**
     * wait end of all blocks in pipe
     */
    public void join() {
        for (int i = 0; i < pipe.size(); i++) {
            pipe.get(i).join();
        }
    }

    /**
     * get first farm of the pipeline
     * @return first farm or null
     */
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

    /**
     * get last farm of pipeline
     * @return last farm or null
     */
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

    public void appendBlock(block<Object, Object> newblock, byte MULTI) {
        block b1 = pipe.getLast();
        pipe.add(newblock);
        connectPipeMulti(b1, (block<U, V>) newblock,bb_settings.BLOCKING, bb_settings.BOUNDED, MULTI);
    }
}
