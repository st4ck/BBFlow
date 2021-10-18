package tests.MSOM;

import java.util.ArrayList;

public class SOMData {
    public Integer redirect = null;
    public Integer replyredirect = null;
    public int train_i;
    public int train_j;
    public ArrayList<Double> neuron = null;
    public double curve;
    public int dataType;
    public bestPosition searchResult;
    public int from;
    public int to;
    int packetId;

    public static final int EOS = 0;
    public static final int SEARCH = 1;
    public static final int LEARN = 2;
    public static final int SEARCH_AND_LEARN = 3;
    public static final int FINISHED = 4;
    public static final int LEARN_FINISHED = 5;
    public static final int LEARN_NEIGHBOURS = 6;

    public SOMData(int t, int id) {
        dataType = t;
        packetId = id;
    }
}
