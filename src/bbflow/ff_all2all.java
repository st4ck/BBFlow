package bbflow;

import java.util.LinkedList;

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

    public void combine_farm(ff_farm<T> b) {
        if (a2a.size() > 0) {
            ff_farm<T> lastElement = a2a.getLast();
            lastElement.collector = null; // dispose collector
            b.emitter = null; // dispose emitter

            /**
             * works in this way:
             * remove collector old farm / emitter new farm
             * remove input channels from new farm
             * use old channels (connecting old workers with old collector) to connect old workers to new ones
             */
            for (int i=0; i<lastElement.workers.size(); i++) {
                ff_queue<T> x = lastElement.workers.get(i).getOutputChannel(0);
                for (int j=0; j<b.workers.size(); j++) {
                    boolean d = b.workers.get(i).removeInputChannel(0); // remove channel emitter/workers
                    b.workers.get(i).addInputChannel(x); // connect each worker out channel (from farm already in) to all workers of new farm
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
