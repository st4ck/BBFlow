package tests.MSOM;

import java.util.ArrayList;
import java.util.LinkedList;

import bbflow.*;

public class MSOM {
    int layers = 1;
    int size;
    int depth;
    int side;
    int parts;
    int split;
    LinkedList<ff_node> soms = new LinkedList<>();

    ff_farm<SOMData, SOMData> all;
    ff_queue<SOMData> feedback;
    ff_node<SOMData, SOMData> postfilter;
    ff_node<SOMData, SOMData> prefilter;
    ff_queue<SOMData> externalInput;

    public MSOM(int size, int depth, int split) {
        this.parts = (int) Math.pow(split,2);
        this.side = size/split;
        this.depth = depth;
        this.size = size;
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
                    ff_queue<SOMData> q = new ff_queue<SOMData>();
                    accessSOM_Matrix(x,y).out.remove(SOM.TOP);
                    accessSOM_Matrix(x,y).out.add(SOM.TOP, q);
                    accessSOM_Matrix(x-1,y).in.remove(SOM.BOTTOM);
                    accessSOM_Matrix(x-1,y).in.add(SOM.BOTTOM,q);
                }

                // with left
                if (y > 0) {
                    ff_queue<SOMData> q = new ff_queue<SOMData>();
                    accessSOM_Matrix(x,y).out.remove(SOM.LEFT);
                    accessSOM_Matrix(x,y).out.add(SOM.LEFT, q);
                    accessSOM_Matrix(x,y-1).in.remove(SOM.RIGHT);
                    accessSOM_Matrix(x,y-1).in.add(SOM.RIGHT,q);
                }

                // with bottom
                if (x < split-1) {
                    ff_queue<SOMData> q = new ff_queue<SOMData>();
                    accessSOM_Matrix(x,y).out.remove(SOM.BOTTOM);
                    accessSOM_Matrix(x,y).out.add(SOM.BOTTOM, q);
                    accessSOM_Matrix(x+1,y).in.remove(SOM.TOP);
                    accessSOM_Matrix(x+1,y).in.add(SOM.TOP,q);
                }

                // with right
                if (y < split-1) {
                    ff_queue<SOMData> q = new ff_queue<SOMData>();
                    accessSOM_Matrix(x,y).out.remove(SOM.RIGHT);
                    accessSOM_Matrix(x,y).out.add(SOM.RIGHT, q);
                    accessSOM_Matrix(x,y+1).in.remove(SOM.LEFT);
                    accessSOM_Matrix(x,y+1).in.add(SOM.LEFT,q);
                }
            }
        }

        all = new ff_farm(0, null);
        all.workers = soms;
        all.collector = new ff_node(new defaultCollector(defaultCollector.FIRSTCOME));
        all.emitter = new ff_node(new defaultEmitter(defaultEmitter.BROADCAST));
        all.connectWorkersCollector();
        all.connectEmitterWorkers();

        ff_queue<SOMData> collectorFilter = new ff_queue();
        postfilter = new ff_node(new Postfilter(parts));
        all.collector.addOutputChannel(collectorFilter);
        postfilter.addInputChannel(collectorFilter);

        prefilter = new ff_node(new Prefilter());

        externalInput = new ff_queue<>();
        prefilter.addInputChannel(externalInput);

        feedback = new ff_queue<>();
        postfilter.addOutputChannel(feedback);
        prefilter.addInputChannel(feedback);

        ff_queue<SOMData> filterEmitter = new ff_queue();
        prefilter.addOutputChannel(filterEmitter);
        all.emitter.addInputChannel(filterEmitter);
    }

    public void start() {
        for (int x=0; x<split; x++) {
            for (int y = 0; y < split; y++) {
                accessSOM_Node(x, y).start();
            }
        }
        all.collector.start();
        all.emitter.start();
        prefilter.start();
        postfilter.start();
    }

    public void join() {
        for (int x=0; x<split; x++) {
            for (int y = 0; y < split; y++) {
                accessSOM_Node(x, y).join();
            }
        }
        all.collector.join();
        all.emitter.join();
        prefilter.join();
        postfilter.join();
    }

    public SOM accessSOM_Matrix(int x, int y) {
        return (SOM) soms.get((x*split)+y).mynode.job;
    }

    public ff_node accessSOM_Node(int x, int y) {
        return soms.get((x*split)+y);
    }

    public bestPosition searchBestPositionThreaded(ArrayList<Double> neuron, int threads) {
        bestPosition result = new bestPosition();
        ArrayList<Thread> threads_list = new ArrayList<>();
        ArrayList<bestPosition> results = new ArrayList<>();
        int pindex = 0;
        for (int x=0; x<split; x++) {
            for (int y = 0; y < split; y++) {
                SOM s = accessSOM_Matrix(x,y);

                results.add(null);
                final Integer finalPindex = pindex;
                Thread z = new Thread(new Runnable() {
                    public void run(){
                        results.set(finalPindex, s.searchBestPosition(neuron));
                    }
                });
                threads_list.add(z);
                pindex++;
            }
        }

        int running = 0;
        int tpos = 0;
        for (int i=0; i<threads_list.size(); i++) {
            if (running > threads) {
                try {
                    threads_list.get(tpos).join();
                    running--;
                    tpos++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            threads_list.get(i).start();
            running++;
        }

        for (int i=0; i<threads_list.size(); i++) {
            try {
                threads_list.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            bestPosition x_res = results.get(i);
            if (x_res.bestdist < result.bestdist) {
                result.bestdist = x_res.bestdist;
                result.besti = x_res.besti;
                result.bestj = x_res.bestj;
                result.i = i/split;
                result.j = i%split;
            }
        }

        return result;
    }

    public bestPosition searchBestPosition(ArrayList<Double> neuron) {
        bestPosition result = new bestPosition();
        for (int x=0; x<split; x++) {
            for (int y = 0; y < split; y++) {
                SOM s = accessSOM_Matrix(x, y);
                bestPosition x_res = s.searchBestPosition(neuron);
                if (x_res.bestdist < result.bestdist) {
                    result.bestdist = x_res.bestdist;
                    result.besti = x_res.besti;
                    result.bestj = x_res.bestj;
                    result.i = x;
                    result.j = y;
                }
            }
        }

        return result;
    }

    public void learnVector(ArrayList<Double> neuron, int besti, int bestj, int i, int j) {
        accessSOM_Matrix(i,j).learnVector(neuron, besti, bestj);
    }

    public void setEOS() {
        /*for (int x=0; x<split; x++) {
            for (int y = 0; y < split; y++) {
                LinkedList<ff_queue<SOMData>> out = accessSOM_Matrix(x, y).out;
                for (int i=0; i<out.size(); i++) {
                    if (out.get(i) != null) {
                        out.get(i).put(new SOMData(SOMData.EOS,-1));
                        out.get(i).setEOS();
                    }
                }
            }
        }
         */

        push(new SOMData(SOMData.EOS,-1));
    }

    public void push(SOMData d) {
        externalInput.put(d);
    }
}
