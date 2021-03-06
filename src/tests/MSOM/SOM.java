package tests.MSOM;
import bbflow.*;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Workers of the MSOM containing a part of the neural network and communicating with neighbours (TOP, LEFT, BOTTOM, RIGHT)
 * Receiving instructions from Emitter and sending results to Collector
 */
public class SOM extends defaultWorker<SOMData, SOMData> {
    public static final int TOP = 0;
    public static final int LEFT = 1;
    public static final int BOTTOM = 2;
    public static final int RIGHT = 3;
    public int waiting_learners = 0;

    public static final int SLAVE = 0;
    public static final int LEARNING = 1;

    public int mode = SLAVE;

    public static final int circ = 5;

    double[][][] som;
    int size = 0;
    int depth = 0;

    bestPosition AIT_result = null;
    double[] AIT_neuron;

    public void runJob() throws InterruptedException {
        SOMData element = null;

        if (mode == SLAVE) {
            element = in.get(4).take();
        } else if (mode == LEARNING){
            while (true) {
                if (position>3) {
                    position = 0;
                }

                element = in.get(4).poll();
                if (element != null) { position = 4; break; }

                if (in.get(position) != null) {
                    element = in.get(position).poll();

                    if (element != null) {
                        break;
                    }
                }

                position++;
            }
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
                        rd.communicationType = SOMData.LISTEN_NEIGHBOURS;
                        if (MSOM.DEBUG) rd.debugString = "ACK";
                        sendOutTo(rd, position);
                    }
                } else {
                    //System.out.println("Training received vector from the border");
                    learnVector(element.neuron, element.train_i, element.train_j, element.curve);
                    SOMData rp = new SOMData(SOMData.LEARN_FINISHED, id);
                    rp.communicationType = SOMData.LISTEN_NEIGHBOURS;
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
                        rd.communicationType = SOMData.LISTEN_NEIGHBOURS;
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

        switch (element.communicationType) {
            case SOMData.LISTEN_NEIGHBOURS:
                if (this.mode != LEARNING) {
                    if (MSOM.DEBUG) System.out.println(id + " COMMUNICATION LEARNING");
                    this.mode = LEARNING;
                }
                break;
            case SOMData.LISTEN_COMMAND:
                if (this.mode != SLAVE) {
                    if (MSOM.DEBUG) System.out.println(id + " COMMUNICATION SLAVE");
                    this.mode = SLAVE;
                }
                break;
        }
    }

    public SOM(int w, int h, int d, int id) {
        som = new double[w][h][d];
        this.size = w;
        this.depth = d;
        this.id = id;
    }

    public SOM(int w, int h, int d, double[][][] som, int id) {
        this.som = som;
        this.size = w;
        this.depth = d;
        this.id = id;
    }

    /**
     * @param low lowest random number generated
     * @param high highest random number generated
     * @param divider the random number generated will be divided by divider. If divider is 1, the random number will be an integer. If divider is 0, SOM will be initialized to 0
     */
    void randomize(int low, int high, double divider) {
        for (int w=0; w<size; w++) {
            for (int h=0; h<size; h++) {
                for (int d=0; d<depth; d++) {
                    if (divider == 1) {
                        som[w][h][d] = (int) (low + (Math.random() * high));
                    } else if (divider == 0) {
                        som[w][h][d] = 0;
                    } else {
                        som[w][h][d] = (low + (Math.random() * high))/divider;
                    }
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

    public bestPosition searchBestPosition(double[] d_neuron) {
        //Searching nearest similar vector
        int besti = 0;
        int bestj = 0;
        double bestdist = Double.MAX_VALUE;

        if (AIT_result != null) {
            boolean AIT = true;
            for (int d = 0; d < depth; d++) {
                if (d_neuron[d] != AIT_neuron[d]) {
                    AIT = false;
                    break;
                }
            }

            if (AIT) {
                return AIT_result;
            }
        }

        for (int w=0; w<size; w++) {
            for (int h=0; h<size; h++) {
                // Euclidean distance between D-dimensional points
                double distance = 0;
                for (int d=0; d<depth; d++) {
                    distance += (som[w][h][d] - d_neuron[d]) * (som[w][h][d] - d_neuron[d]);

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

        // saving search results in case the input vector is the same and the matrix doesn't change
        AIT_result = b;
        AIT_neuron = d_neuron;

        //System.out.println("Best vector position " + besti + ":" + bestj + " Distance: " + bestdist);

        return b;
    }

    void learnVector(double[] neuron, int i, int j, double curve) {
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

        AIT_result = null;

        for (int d=0; d<depth; d++) {
            som[i][j][d] = som[i][j][d] * (1-curve) + neuron[d] * curve;
            som[i][j][d] = Math.round(som[i][j][d]*100)/100;
        }

        if (waiting_learners > 0) {
            waiting_learners--;
        }
    }

    void learnVector(double[] neuron, int besti, int bestj) {
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
		rd.communicationType = SOMData.LISTEN_NEIGHBOURS;
        if (MSOM.DEBUG) rd.debugString = "LOCAL_LEARN";
        in.get(4).put(rd);
    }

    private void train(Integer pos1, Integer pos2, double[] neuron, double curve, int i, int j) {
        if (out.get(pos1) == null) {
            // NO OUT CHANNEL
            waiting_learners--;
            return;
        }

        SOMData tosend = new SOMData(SOMData.LEARN_NEIGHBOURS, id);
        tosend.redirect = pos2;
        tosend.neuron = neuron;
        tosend.curve = curve;
		tosend.communicationType = SOMData.LISTEN_NEIGHBOURS;

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
