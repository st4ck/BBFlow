package bbflow;

import java.util.LinkedList;

/**
 * All2All Building Block able to merge multiple farms
 * @param <T> Custom type of channels
 */
public class ff_all2all<T,U,V,W> extends ff_node<T,V> {
    /**
     * collection of blocks where first and last element are of type ff_farm in any circumstances
     */
    LinkedList<block> a2a;
    int bufferSize = bb_settings.defaultBufferSize;

    public ff_all2all() {
        a2a = new LinkedList<>();
    }

    public ff_all2all(int bufferSize) {
        a2a = new LinkedList<>();
        this.bufferSize = bufferSize;
    }

    /**
     * All2All building block. For details see the thesis. 8 Cases considered
     * @param b1 Left farm
     * @param b2 Right farm
     * @param customEmitterR custom emitter to use during fusion
     * @param customCollectorG custom collector to use during fusion
     * @param merge true, pipeline with single channel generated between farms. False pipeline with multiple channels
     */
    public void combine_farm(ff_farm<T,U> b1, ff_farm<V,W> b2, ff_node<U,Object> customEmitterR, ff_node<Object,V> customCollectorG, boolean merge) {
        b1.collector = null; // dispose collector
        b2.emitter = null; // dispose emitter

        a2a.add(b1);

        if (merge) {
            if ((customEmitterR == null) && (customCollectorG == null)) {
                a2a.remove(b1);
                combine_farm(b1, b2, null, null, false);
                return;
            } else if ((customEmitterR != null) && (customCollectorG == null)) {
                // default collector + R as new Emitter
                ff_node<U,U> collector = new ff_node<U,U>(new defaultCollector<U>(defaultCollector.ROUNDROBIN));
                ff_pipeline<U,V> R_G = new ff_pipeline<U,V>((block<U, Object>)collector, (block<Object, V>)customEmitterR);

                for (int i = 0; i < b1.workers.size(); i++) {
                    ff_queue<U> x = b1.workers.get(i).getOutputChannel(0);
                    R_G.addInputChannel(x);

                    for (int j = 0; j < b2.workers.size(); j++) {
                        ff_queue<V> y = b2.workers.get(j).getInputChannel(0);
                        R_G.addOutputChannel(y);
                    }
                }

                a2a.add(R_G);
            } else if ((customEmitterR == null) && (customCollectorG != null)) {
                // default collector + G as new Emitter
                ff_node<U,U> collector = new ff_node<U,U>(new defaultCollector<U>(defaultCollector.ROUNDROBIN));
                ff_pipeline<U,V> R_G = new ff_pipeline<U,V>((block<U, Object>)collector, (block<Object, V>)customCollectorG);

                for (int i = 0; i < b1.workers.size(); i++) {
                    ff_queue<U> x = b1.workers.get(i).getOutputChannel(0);
                    R_G.addInputChannel(x);

                    for (int j = 0; j < b2.workers.size(); j++) {
                        ff_queue<V> y = b2.workers.get(j).getInputChannel(0);
                        R_G.addOutputChannel(y);
                    }
                }

                a2a.add(R_G);
            } else { // both pointers valid
                // default collector + compose R & G as new Emitter (in pipeline)
                ff_node<U,U> collector = new ff_node<U,U>(new defaultCollector<U>(defaultCollector.ROUNDROBIN));
                ff_pipeline<U,V> R_G = new ff_pipeline<U,V>(customEmitterR, customCollectorG);
                ff_queue<U> collector_R_G = new ff_queue<>();
                collector.addOutputChannel(collector_R_G);
                R_G.addInputChannel(collector_R_G);

                for (int i = 0; i < b1.workers.size(); i++) {
                    ff_queue<U> x = b1.workers.get(i).getOutputChannel(0);
                    collector.addInputChannel(x);

                    for (int j = 0; j < b2.workers.size(); j++) {
                        ff_queue<V> y = b2.workers.get(j).getInputChannel(0);
                        R_G.addOutputChannel(y);
                    }
                }

                a2a.add(collector);
                a2a.add(R_G);
            }
        } else {
            if ((customEmitterR == null) && (customCollectorG == null)) {
                /**
                 * works in this way:
                 * remove collector from old farm and emitter from new farm
                 * remove input channels from new farm
                 * use old channels (connecting old workers with old collector) to connect old workers to new ones
                 */
                for (int i = 0; i < b1.workers.size(); i++) {
                    b1.workers.get(i).removeOutputChannel(0); // removing channel between worker and removed collector
                    for (int j = 0; j < b2.workers.size(); j++) {
                        ff_queue<U> b1_b2 = new ff_queue<>();
                        if (i==0) {
                            b2.workers.get(j).removeInputChannel(0); // remove old channel between removed emitter and b2 worker
                        }
                        // connect each worker new out channel (from farm already in) to all workers of new farm
                        b1.workers.get(i).addOutputChannel(b1_b2);
                        b2.workers.get(j).addInputChannel(b1_b2); // U & V types must be equal in this case
                    }
                }
            } else if ((customEmitterR != null) && (customCollectorG == null)) {
                /**
                 * works in this way:
                 * remove collector from old farm and emitter from new farm
                 * use old channels (connecting old workers with old collector) to connect old workers to customEmitterR
                 * and use old channels (connecting new emitter with new collector) to connect customEmitterR to all new workers
                 */
                for (int i = 0; i < b1.workers.size(); i++) {
                    ff_queue<U> x = b1.workers.get(i).getOutputChannel(0);
                    // connect old workers with R (one per worker)
                    ff_node<U,Object> newR = new ff_node<U,Object>(customEmitterR);
                    a2a.add(newR);
                    newR.addInputChannel(x);
                    for (int j = 0; j < b2.workers.size(); j++) {
                        // connect each R to all R-workers
                        ff_queue<V> y = b2.workers.get(j).getInputChannel(0);
                        newR.addOutputChannel((ff_queue<Object>) y);
                    }
                }
            } else if ((customEmitterR == null) && (customCollectorG != null)) {
                /**
                 * works in this way:
                 * remove collector from old farm and emitter from new farm
                 * use old channels (connecting old workers with old collector) to connect old workers to all customCollectorG
                 * and use old channels (connecting new emitter with new collector) to connect customCollectorG to workers
                 */
                for (int j = 0; j < b2.workers.size(); j++) {
                    ff_queue<V> x = b2.workers.get(j).getInputChannel(0);
                    ff_node<Object,V> newG = new ff_node<Object,V>(customCollectorG);
                    a2a.add(newG);
                    newG.addOutputChannel(x);
                    for (int i = 0; i < b1.workers.size(); i++) {
                        ff_queue<U> y = b1.workers.get(i).getOutputChannel(0);
                        newG.addInputChannel((ff_queue<Object>) y);
                    }
                }
            } else { // both pointers valid
                for (int i = 0; i < b1.workers.size(); i++) {
                    ff_queue<U> x = b1.workers.get(i).getOutputChannel(0);
                    ff_node<U,Object> newR = new ff_node<U,Object>(customEmitterR);
                    a2a.add(newR);
                    newR.addInputChannel(x);
                    for (int j = 0; j < b2.workers.size(); j++) {
                        ff_queue<V> y = b2.workers.get(j).getInputChannel(0);
                        ff_node<Object,V> newG = new ff_node<Object,V>(customCollectorG);
                        a2a.add(newG);
                        newG.addOutputChannel(y);

                        ff_queue<Object> z = new ff_queue<>(bufferSize);
                        newR.addOutputChannel(z);
                        newG.addInputChannel(z);
                    }
                }
            }
        }

        a2a.add(b2);
    }

    public void combine_farm(ff_farm<T,U> b1, ff_farm<V,W> b2) {
        combine_farm(b1,b2,null,null,false);
    }

    /**
     * add input channel to the left side
     * @param input input channel
     */
    public void addInputChannel(ff_queue<T> input) {
        if (a2a.size() > 0) {
            a2a.getFirst().addInputChannel(input);
        }
    }

    /**
     * add output channel to the right side
     * @param output output channel
     */
    public void addOutputChannel(ff_queue<V> output) {
        if (a2a.size() > 0) {
            a2a.getLast().addOutputChannel(output);
        }
    }

    /**
     * start all blocks
     */
    public void start() {
        for (int i=0; i<a2a.size(); i++) {
            a2a.get(i).start();
        }
    }

    /**
     * wait end of all blocks
     */
    public void join() {
        for (int i = 0; i < a2a.size(); i++) {
            a2a.get(i).join();
        }
    }
}
