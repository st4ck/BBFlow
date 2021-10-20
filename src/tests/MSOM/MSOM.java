package tests.MSOM;

import java.util.LinkedList;

import bbflow.*;

public class MSOM {
    public static final boolean DEBUG = false;
    int parts;
    int split;
    LinkedList<ff_node> soms = new LinkedList<>();

    ff_farm<SOMData, SOMData> all;
    ff_queue<SOMData> feedback;
    ff_queue<SOMData> externalInput;

    public MSOM(int size, int depth, int split) {
        this.parts = (int) Math.pow(split,2);
        int side = size/split;
        this.split = split;

        for (int i=0; i<parts; i++) {
            SOM s = new SOM(side,side,depth,i);
            for (int j=0;j<4; j++) {
                s.in.add(null);
                s.out.add(null);
            }

            soms.add(new ff_node(s));
        }

        for (int x=0; x<split; x++) {
            for (int y=0; y<split; y++) {
                // with top
                if (x > 0) {
                    ff_queue<SOMData> q = new ff_queue<>();
                    accessSOM_Matrix(x,y).out.remove(SOM.TOP);
                    accessSOM_Matrix(x,y).out.add(SOM.TOP, q);
                    accessSOM_Matrix(x-1,y).in.remove(SOM.BOTTOM);
                    accessSOM_Matrix(x-1,y).in.add(SOM.BOTTOM,q);
                }

                // with left
                if (y > 0) {
                    ff_queue<SOMData> q = new ff_queue<>();
                    accessSOM_Matrix(x,y).out.remove(SOM.LEFT);
                    accessSOM_Matrix(x,y).out.add(SOM.LEFT, q);
                    accessSOM_Matrix(x,y-1).in.remove(SOM.RIGHT);
                    accessSOM_Matrix(x,y-1).in.add(SOM.RIGHT,q);
                }

                // with bottom
                if (x < split-1) {
                    ff_queue<SOMData> q = new ff_queue<>();
                    accessSOM_Matrix(x,y).out.remove(SOM.BOTTOM);
                    accessSOM_Matrix(x,y).out.add(SOM.BOTTOM, q);
                    accessSOM_Matrix(x+1,y).in.remove(SOM.TOP);
                    accessSOM_Matrix(x+1,y).in.add(SOM.TOP,q);
                }

                // with right
                if (y < split-1) {
                    ff_queue<SOMData> q = new ff_queue<>();
                    accessSOM_Matrix(x,y).out.remove(SOM.RIGHT);
                    accessSOM_Matrix(x,y).out.add(SOM.RIGHT, q);
                    accessSOM_Matrix(x,y+1).in.remove(SOM.LEFT);
                    accessSOM_Matrix(x,y+1).in.add(SOM.LEFT,q);
                }
            }
        }

        all = new ff_farm(0, null);
        all.workers = soms;
        all.collector = new ff_node(new Collector(parts));
        all.emitter = new ff_node(new Emitter());
        all.connectWorkersCollector();
        all.connectEmitterWorkers();

        externalInput = new ff_queue<>();
        all.emitter.addInputChannel(externalInput);

        feedback = new ff_queue<>();
        all.collector.addOutputChannel(feedback);
        all.emitter.addInputChannel(feedback);
    }

    public void start() {
        for (int x=0; x<split; x++) {
            for (int y = 0; y < split; y++) {
                accessSOM_Node(x, y).start();
            }
        }
        all.collector.start();
        all.emitter.start();
    }

    public void join() {
        for (int x=0; x<split; x++) {
            for (int y = 0; y < split; y++) {
                accessSOM_Node(x, y).join();
            }
        }
        all.collector.join();
        all.emitter.join();
    }

    public SOM accessSOM_Matrix(int x, int y) {
        return (SOM) soms.get((x*split)+y).mynode.job;
    }

    public ff_node accessSOM_Node(int x, int y) {
        return soms.get((x*split)+y);
    }


    public void setEOS() {
        push(new SOMData(SOMData.EOS,-1));
    }

    public void push(SOMData d) {
        externalInput.put(d);
    }
}
