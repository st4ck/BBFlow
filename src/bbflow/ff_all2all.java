package bbflow;

import java.util.LinkedList;

/**
 * All2All Building Block able to merge multiple farms
 * @param <T> Custom type of channels
 */
public class ff_all2all<T> extends block<T> {
    LinkedList<ff_farm<T>> a2a;
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
     * @param b new farm to add in the all2all block
     * @param CustomEmitter custom emitter to use during fusion
     * @param CustomCollector custom collector to use during fusione
     * @param merge true, pipeline with single channel generated between farms. False pipeline with multiple channels
     */
    public void combine_farm(ff_farm<T> b, ff_node<T> customEmitterR, ff_node<T> customCollectorG, boolean merge) {
        if (a2a.size() > 0) {
            ff_farm<T> lastElement = a2a.getLast(); // get last farm present (I call it old farm)
            lastElement.collector = null; // dispose collector
            b.emitter = null; // dispose emitter

            if (merge) {
                if ((customEmitterR == null) && (customCollectorG == null)) {
                    combine_farm(b, null, null, false);
                    return;
                } else if ((customEmitterR != null) && (customCollectorG == null)) {
                    // compose R & G as new Emitter (in pipeline) + default collector
                    ff_node<T> collector = new ff_node<T>(new defaultCollector<T>(defaultCollector.ROUNDROBIN));
                    ff_pipeline<T> R_G = new ff_pipeline<>();
                    R_G.appendNewBB(collector);
                    R_G.appendNewBB(customEmitterR);

                    for (int i = 0; i < lastElement.workers.size(); i++) {
                        ff_queue<T> x = lastElement.workers.get(i).getOutputChannel(0);
                        R_G.addInputChannel(x);

                        for (int j = 0; j < b.workers.size(); j++) {
                            ff_queue<T> y = b.workers.get(i).getInputChannel(0);
                            R_G.addOutputChannel(y);
                        }
                    }
                } else if ((customEmitterR == null) && (customCollectorG != null)) {
                    // compose R & G as new Emitter (in pipeline) + default collector
                    ff_node<T> collector = new ff_node<T>(new defaultCollector<T>(defaultCollector.ROUNDROBIN));
                    ff_pipeline<T> R_G = new ff_pipeline<>();
                    R_G.appendNewBB(collector);
                    R_G.appendNewBB(customCollectorG);

                    for (int i = 0; i < lastElement.workers.size(); i++) {
                        ff_queue<T> x = lastElement.workers.get(i).getOutputChannel(0);
                        R_G.addInputChannel(x);

                        for (int j = 0; j < b.workers.size(); j++) {
                            ff_queue<T> y = b.workers.get(i).getInputChannel(0);
                            R_G.addOutputChannel(y);
                        }
                    }
                } else { // both pointers valid
                    // compose R & G as new Emitter (in pipeline) + default collector
                    ff_node<T> collector = new ff_node<T>(new defaultCollector<T>(defaultCollector.ROUNDROBIN));
                    ff_pipeline<T> R_G = new ff_pipeline<>();
                    R_G.appendNewBB(collector);
                    R_G.appendNewBB(customEmitterR);
                    R_G.appendNewBB(customCollectorG);

                    for (int i = 0; i < lastElement.workers.size(); i++) {
                        ff_queue<T> x = lastElement.workers.get(i).getOutputChannel(0);
                        R_G.addInputChannel(x);

                        for (int j = 0; j < b.workers.size(); j++) {
                            ff_queue<T> y = b.workers.get(i).getInputChannel(0);
                            R_G.addOutputChannel(y);
                        }
                    }
                }
            } else {
                if ((customEmitterR == null) && (customCollectorG == null)) {
                    /**
                     * works in this way:
                     * remove collector from old farm and emitter from new farm
                     * remove input channels from new farm
                     * use old channels (connecting old workers with old collector) to connect old workers to new ones
                     */
                    for (int i = 0; i < lastElement.workers.size(); i++) {
                        ff_queue<T> x = lastElement.workers.get(i).getOutputChannel(0);
                        for (int j = 0; j < b.workers.size(); j++) {
                            boolean d = b.workers.get(i).removeInputChannel(0); // remove channel emitter/workers
                            b.workers.get(i).addInputChannel(x); // connect each worker out channel (from farm already in) to all workers of new farm
                        }
                    }
                } else if ((customEmitterR != null) && (customCollectorG == null)) {
                    /**
                     * works in this way:
                     * remove collector from old farm and emitter from new farm
                     * use old channels (connecting old workers with old collector) to connect old workers to customEmitterR
                     * and use old channels (connecting new emitter with new collector) to connect customEmitterR to all new workers
                     */
                    for (int i = 0; i < lastElement.workers.size(); i++) {
                        ff_queue<T> x = lastElement.workers.get(i).getOutputChannel(0);
                        // connect old workers with R (one per worker)
                        ff_node<T> newR = new ff_node<T>(customEmitterR);
                        newR.addInputChannel(x);
                        for (int j = 0; j < b.workers.size(); j++) {
                            // connect each R to all R-workers
                            ff_queue<T> y = b.workers.get(j).getInputChannel(0);
                            newR.addOutputChannel(y);
                        }
                    }
                } else if ((customEmitterR == null) && (customCollectorG != null)) {
                    /**
                     * works in this way:
                     * remove collector from old farm and emitter from new farm
                     * use old channels (connecting old workers with old collector) to connect old workers to all customCollectorG
                     * and use old channels (connecting new emitter with new collector) to connect customCollectorG to workers
                     */
                    for (int j = 0; j < b.workers.size(); j++) {
                        ff_queue<T> x = b.workers.get(j).getInputChannel(0);
                        ff_node<T> newG = new ff_node<T>(customCollectorG);
                        newG.addOutputChannel(x);
                        for (int i = 0; i < lastElement.workers.size(); i++) {
                            ff_queue<T> y = lastElement.workers.get(i).getOutputChannel(0);
                            newG.addInputChannel(y);
                        }
                    }
                } else { // both pointers valid
                    for (int i = 0; i < lastElement.workers.size(); i++) {
                        ff_queue<T> x = lastElement.workers.get(i).getOutputChannel(0);
                        ff_node<T> newR = new ff_node<T>(customEmitterR);
                        newR.addInputChannel(x);
                        for (int j = 0; j < b.workers.size(); j++) {
                            ff_queue<T> y = b.workers.get(j).getInputChannel(0);
                            ff_node<T> newG = new ff_node<T>(customCollectorG);
                            newG.addOutputChannel(y);

                            ff_queue<T> z = new ff_queue<>();
                            newR.addOutputChannel(z);
                            newG.addInputChannel(z);
                        }
                    }
                }
            }

        } else {
            // simply add new farm (nothing in a2a at the moment)
            a2a.add(b);
        }
    }

    public void addInputChannel(ff_queue<T> input) {
        if (a2a.size() > 0) {
            a2a.getFirst().addInputChannel(input);
        }
    }

    public void addOutputChannel(ff_queue<T> output) {
        if (a2a.size() > 0) {
            a2a.getLast().addOutputChannel(output);
        }
    }

    public void start() {
        for (int i=0; i<a2a.size(); i++) {
            a2a.get(i).start();
        }
    }

    public void join() {
        for (int i = 0; i < a2a.size(); i++) {
            a2a.get(i).join();
        }
    }
}
