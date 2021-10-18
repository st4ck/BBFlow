package tests.MSOM;
import bbflow.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class SOM extends defaultWorker<SOMData, SOMData> {
    public static final int TOP = 0;
    public static final int LEFT = 1;
    public static final int BOTTOM = 2;
    public static final int RIGHT = 3;
    public int waiting_learners = 0;

    /*@Override
    public void init() {
        for (int i=0; i<5; i++) {
            EOS_sent[i] = false;
        }
    }*/

    public void runJob() throws InterruptedException {
        SOMData element = null;
        while (true) {
            if (position>3) {
                position = 0;
            }

            element = in.get(4).poll(5, TimeUnit.MILLISECONDS);
            if (element != null) { position = 4; break; }

            if (in.get(position) != null) {
                element = in.get(position).poll(5, TimeUnit.MILLISECONDS);

                if (element != null) {
                    break;
                }
            }

            position++;
        }

        if (MSOM.DEBUG) System.out.println(id+" received "+element.dataType+" from "+element.packetId);

        switch (element.dataType) {
            case SOMData.SEARCH_AND_LEARN:
            case SOMData.SEARCH:
                element.searchResult = searchBestPosition(element.neuron);
                element.from = id;
                sendOutTo(element,4);
                break;
            case SOMData.LEARN:
                if (element.to == id) {
                    //System.out.println("Training received vector from the border");
                    waiting_learners = 0;
                    learnVector(element.neuron, element.train_i, element.train_j);
                }
                break;
            case SOMData.LEARN_NEIGHBOURS:
                if (element.redirect != null) {
                    int t = element.redirect;
                    element.redirect = null;

                    if (out.get(t) != null) {
                        //System.out.println("Redirecting training vector");
                        element.replyredirect = position;
                        sendOutTo(element, t);
                    } else {
                        // no SOM to train, reply with ACK
                        SOMData rd = new SOMData(SOMData.LEARN_FINISHED, id);
                        if (MSOM.DEBUG) rd.debugString = "ACK";
                        sendOutTo(rd, position);
                    }
                } else {
                    //System.out.println("Training received vector from the border");
                    learnVector(element.neuron, element.train_i, element.train_j, element.curve);
                    SOMData rp = new SOMData(SOMData.LEARN_FINISHED, id);
                    if (MSOM.DEBUG) rp.debugString = "REPLY";

                    if (element.replyredirect != null) {
                        if (MSOM.DEBUG) rp.debugString = "REDIRECT_REPLY";
                        rp.replyredirect = element.replyredirect;
                    }

                    sendOutTo(rp, position);
                }
                break;
            case SOMData.LEARN_FINISHED:
                if (element.replyredirect != null) {
                    int rd = element.replyredirect;
                    element.replyredirect = null;
                    if (MSOM.DEBUG) element.debugString = "LEARN_REDIRECTED_REPLY";
                    sendOutTo(element, rd);
                } else {
                    if (MSOM.DEBUG) System.out.println("Node " + id + " received LF ("+element.debugString+") from " + element.packetId + " - "+(waiting_learners-1));
                    if (waiting_learners > 0) {
                        waiting_learners--;
                    }

                    if (waiting_learners == 0) {
                        if (MSOM.DEBUG) System.out.println("Sending learn finished to collector");
                        SOMData rd = new SOMData(SOMData.LEARN_FINISHED, id);
                        if (MSOM.DEBUG) rd.debugString = "LEARN_FINISHED";
                        sendOutTo(rd, 4);
                    }
                }
                break;
            case SOMData.FINISHED:
                break;
            case SOMData.EOS:
                sendOutTo(element, 4);
                out.get(4).setEOS();
                in = new LinkedList<>();
                return;
        }
    }

    double[][][] som;
    int size = 0;
    int depth = 0;

    public SOM(int w, int h, int d, int id) {
        som = new double[w][h][d];
        this.size = w;
        this.depth = d;
        this.id = id;

        randomize();
    }

    public SOM(int w, int h, int d, double[][][] som, int id) {
        this.som = som;
        this.size = w;
        this.depth = d;
        this.id = id;
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
        double bestdist = Double.MAX_VALUE;

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

        if (waiting_learners > 0) {
            waiting_learners--;
        }
    }

    void learnVector(ArrayList<Double> neuron, int besti, int bestj) {
        int circ = 5;

        /*for (int i=Math.max(0,bestw-circ); i<=Math.min(width-1,bestw+circ); i++) {
            for (int j=Math.max(0,besth-circ); j<=Math.min(height-1,besth+circ); j++) {*/
        waiting_learners++;

        for (int i=besti-circ; i<=besti+circ; i++) {
            for (int j=bestj-circ; j<=bestj+circ; j++) {
                double flatfactor = Math.max(Math.abs(i-besti),Math.abs(j-bestj));
                double curve = 0.2/Math.pow(1.3,flatfactor);

                waiting_learners++;
                learnVector(neuron, i, j, curve);
            }
        }

        SOMData rd = new SOMData(SOMData.LEARN_FINISHED, id);
        if (MSOM.DEBUG) rd.debugString = "LOCAL_LEARN";
        in.get(4).put(rd);
    }

    int counter = 0;

    private void train(Integer pos1, Integer pos2, ArrayList<Double> neuron, double curve, int i, int j) {
        if (out.get(pos1) == null) {
            // NO OUT CHANNEL
            waiting_learners--;
            return;
        }

        SOMData tosend = new SOMData(SOMData.LEARN_NEIGHBOURS, id);
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
