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
    ArrayList<ff_node<Pair,Pair>> soms = new ArrayList<>();

    public MSOM(int size, int depth, int split) {
        bb_settings.BLOCKING = false;
        bb_settings.BOUNDED = false;

        this.parts = (int) Math.pow(split,2);
        this.side = size/split;
        this.depth = depth;
        this.size = size;
        this.split = split;

        for (int i=0; i<parts; i++) {
            SOM s = new SOM(side,side,depth);
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
                    ff_queue<Pair> q = new ff_queue<Pair>();
                    accessSOM_Matrix(x,y).out.remove(SOM.TOP);
                    accessSOM_Matrix(x,y).out.add(SOM.TOP, q);
                    accessSOM_Matrix(x-1,y).in.remove(SOM.BOTTOM);
                    accessSOM_Matrix(x-1,y).in.add(SOM.BOTTOM,q);
                }

                // with left
                if (y > 0) {
                    ff_queue<Pair> q = new ff_queue<Pair>();
                    accessSOM_Matrix(x,y).out.remove(SOM.LEFT);
                    accessSOM_Matrix(x,y).out.add(SOM.LEFT, q);
                    accessSOM_Matrix(x,y-1).in.remove(SOM.RIGHT);
                    accessSOM_Matrix(x,y-1).in.add(SOM.RIGHT,q);
                }

                // with bottom
                if (x < split-1) {
                    ff_queue<Pair> q = new ff_queue<Pair>();
                    accessSOM_Matrix(x,y).out.remove(SOM.BOTTOM);
                    accessSOM_Matrix(x,y).out.add(SOM.BOTTOM, q);
                    accessSOM_Matrix(x+1,y).in.remove(SOM.TOP);
                    accessSOM_Matrix(x+1,y).in.add(SOM.TOP,q);
                }

                // with right
                if (y < split-1) {
                    ff_queue<Pair> q = new ff_queue<Pair>();
                    accessSOM_Matrix(x,y).out.remove(SOM.RIGHT);
                    accessSOM_Matrix(x,y).out.add(SOM.RIGHT, q);
                    accessSOM_Matrix(x,y+1).in.remove(SOM.LEFT);
                    accessSOM_Matrix(x,y+1).in.add(SOM.LEFT,q);
                }
            }
        }
    }

    public void start() {
        for (int x=0; x<split; x++) {
            for (int y = 0; y < split; y++) {
                accessSOM_Node(x, y).start();
            }
        }
    }

    public void join() {
        for (int x=0; x<split; x++) {
            for (int y = 0; y < split; y++) {
                accessSOM_Node(x, y).join();
            }
        }
    }

    public SOM accessSOM_Matrix(int x, int y) {
        return (SOM) soms.get((x*split)+y).mynode.job;
    }

    public ff_node accessSOM_Node(int x, int y) {
        return soms.get((x*split)+y);
    }

    public bestPosition searchBestPositionThread(ArrayList<Double> neuron) {
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

        for (int i=0; i<threads_list.size(); i++) {
            threads_list.get(i).start();
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
        for (int x=0; x<split; x++) {
            for (int y = 0; y < split; y++) {
                LinkedList<ff_queue<Pair>> out = accessSOM_Matrix(x, y).out;
                for (int i=0; i<out.size(); i++) {
                    if (out.get(i) != null) {
                        out.get(i).put(new Pair());
                        out.get(i).setEOS();
                    }
                }
            }
        }
    }
}
