package tests.MSOM;

import bbflow.bb_settings;

/**
 * MSOM real usage example. Creating of a MSOM, learning, saving on disk, loading from disk and searching.
 */
public class MSOM_real_usage {
    public static void main (String[] args) {
        bb_settings.BLOCKING = false;
        bb_settings.BOUNDED = false;
        bb_settings.backOff = 5000;

        int depth = 3;
        int side = 1024;
        int split = 4;
        MSOM msom = new MSOM(side,depth,split);
        msom.randomize(0,255,1);
        msom.start();

        // random vector generation
        double[] d_neuron = new double[depth];
        for (int j=0; j<depth; j++) {
            d_neuron[j] = Math.random()*255;
        }

        // creating a new job for MSOM
        SOMData searchLearn = new SOMData(SOMData.SEARCH, 0);
        searchLearn.neuron = d_neuron;
        msom.push(searchLearn);

        searchLearn = new SOMData(SOMData.SEARCH, 1);
        searchLearn.neuron = d_neuron;
        msom.push(searchLearn);

        msom.setEOS();
        msom.join();

        var x = msom.getOlderResult();
        x = msom.getOlderResult();
    }
}