package tests.MSOM;
import bbflow.*;
import bbflow_network.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class SOM extends defaultWorker<Pair,Pair> {
    public static final int TOP = 0;
    public static final int LEFT = 1;
    public static final int BOTTOM = 2;
    public static final int RIGHT = 3;

    public void runJob() {
        Pair element = null;
        boolean allnull = true;
        for (int i = 0; i < in.size(); i++) {
            if (in.get(i) != null) {
                allnull = false;
                try {
                    //element = in.get(i).poll(100, TimeUnit.MILLISECONDS);
                    element = in.get(i).take();
                    if (element != null) {
                        if (element.neuron == null) {
                            // received EOSs
                            in.set(i, null);
                            return;
                        }
                        break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (allnull) {
            in = new LinkedList<>();
            return;
        }
        if (element == null) { return; }

        if (element.redirect != null) {
            int t = element.redirect;
            element.redirect = null;

            if (out.get(t) != null) {
                //System.out.println("Redirecting training vector");
                sendOutTo(element, t);
            }
        } else {
            //System.out.println("Training received vector from the border");
            learnVector(element.neuron, element.train_i, element.train_j, element.curve);
        }
    }

    double[][][] som;
    int size = 0;
    int depth = 0;

    public SOM(int w, int h, int d) {
        som = new double[w][h][d];
        this.size = w;
        this.depth = d;

        randomize();
    }

    public SOM(int w, int h, int d, double[][][] som) {
        this.som = som;
        this.size = w;
        this.depth = d;
    }

    void randomize() {
        for (int w=0; w<size; w++) {
            for (int h=0; h<size; h++) {
                for (int d=0; d<depth; d++) {
                    som[w][h][d] = (int)(Math.random()*255);
                    //som[w][h][d] = 0;
                }
            }
        }
    }

    public static ArrayList<Double> normalize(ArrayList<Double> line, int depth) {
        ArrayList<Double> neuron = new ArrayList<>(depth);

        double n_min = Double.MAX_VALUE;
        for (int n=0; n<depth; n++) {
            neuron.add(line.get(n));
            // find minimum excluding last
            if (n < (depth-1)) {
                if (n_min > neuron.get(n)) {
                    n_min = neuron.get(n);
                }
            }
        }

        /* Convert vector to custom format */
        for (int n=0; n<depth; n++) {
            neuron.set(n,neuron.get(n)-n_min);
        }

        return neuron;
    }

    public bestPosition searchBestPosition(ArrayList<Double> neuron) {
        //Searching nearest similar vector
        int besti = 0;
        int bestj = 0;
        double bestdist = (depth*depth) * (depth*depth);
        bestdist = bestdist*bestdist;

        for (int w=0; w<size; w++) {
            //cout << "Row " << (w+1) << "\n";
            for (int h=0; h<size; h++) {
                // Euclidean distance between D-dimensional points
                double distance = 0;
                for (int d=0; d<depth; d++) {
                    distance += (som[w][h][d] - neuron.get(d)) * (som[w][h][d] - neuron.get(d));

                    if (distance > bestdist) { // avoid unuseful loops
                        break;
                    }
                }

                if (distance < bestdist) {
                    bestdist = distance;
                    besti = w;
                    bestj = h;
                }
            }
        }

        bestPosition b = new bestPosition();
        b.besti = besti;
        b.bestj = bestj;
        b.bestdist = bestdist;

        //System.out.println("Best vector position " + besti + ":" + bestj + " Distance: " + bestdist);

        return b;
    }
    void learnVector(ArrayList<Double> neuron, int i, int j, double curve) {
        if (i<0) {
            if (j<0) {
                train(TOP, LEFT, neuron, curve, i, j);
            } else if (j>size-1) {
                train(TOP,RIGHT, neuron, curve, i, j);
            } else {
                train(TOP,null, neuron, curve, i, j);
            }
            return;
        } else if (i>size-1) {
            if (j>size-1) {
                train(BOTTOM, RIGHT, neuron, curve, i, j);
            } else if (j<0) {
                train(BOTTOM, LEFT, neuron, curve, i, j);
            } else {
                train(BOTTOM, null, neuron, curve, i, j);
            }
            return;
        } else if (j<0) {
            train(LEFT, null, neuron, curve, i, j);
            return;
        } else if (j>size-1) {
            train(RIGHT, null, neuron, curve, i, j);
            return;
        }

        for (int d=0; d<depth; d++) {
            som[i][j][d] = som[i][j][d] * (1-curve) + neuron.get(d) * curve;
            som[i][j][d] = Math.round(som[i][j][d]*100)/100;
        }
    }

    void learnVector(ArrayList<Double> neuron, int besti, int bestj) {
        int circ = 5;

        /*for (int i=Math.max(0,bestw-circ); i<=Math.min(width-1,bestw+circ); i++) {
            for (int j=Math.max(0,besth-circ); j<=Math.min(height-1,besth+circ); j++) {*/
        for (int i=besti-circ; i<=besti+circ; i++) {
            for (int j=bestj-circ; j<=bestj+circ; j++) {
                double flatfactor = Math.max(Math.abs(i-besti),Math.abs(j-bestj));
                double curve = 0.2/Math.pow(1.3,flatfactor);

                learnVector(neuron, i, j, curve);
            }
        }
    }

    private void train(Integer pos1, Integer pos2, ArrayList<Double> neuron, double curve, int i, int j) {
        if (out.get(pos1) == null) { return; }

        Pair tosend = new Pair();
        tosend.redirect = pos2;
        tosend.neuron = neuron;
        tosend.curve = curve;

        switch (pos1) {
            case TOP:
                if (pos2 == null) {
                    tosend.train_j = j;
                    tosend.train_i = size+i;
                } else if (pos2 == LEFT) {
                    tosend.train_j = size+j;
                    tosend.train_i = size+i;
                } else if (pos2 == RIGHT) {
                    tosend.train_j = j-size;
                    tosend.train_i = size+i;
                }
                break;
            case LEFT:
                tosend.train_j = size+j;
                tosend.train_i = i;
                break;
            case RIGHT:
                tosend.train_j = j-size;
                tosend.train_i = i;
                break;
            case BOTTOM:
                if (pos2 == null) {
                    tosend.train_j = j;
                    tosend.train_i = i-size;
                } else if (pos2 == LEFT) {
                    tosend.train_j = size+j;
                    tosend.train_i = i-size;
                } else if (pos2 == RIGHT) {
                    tosend.train_j = j-size;
                    tosend.train_i = i-size;
                }
                break;
        }

        out.get(pos1).put(tosend);
    }
}
