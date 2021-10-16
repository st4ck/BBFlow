package tests.MSOM;

import bbflow.customWatch;

import java.util.ArrayList;

public class test_MSOM {
    public static void main (String[] args) {
        testMSOM(2);
        testMSOM(4);
        testMSOM(8);
        testMSOM(16);
    }

    private static void testMSOM(int split) {
        int depth = 3;
        MSOM z = new MSOM(256,depth,split);
        z.start();
        double[] x = {9,90,70};
        ArrayList<Double> neuron = SOM.normalize(x,depth);
        /*bestPosition res = z.searchBestPosition(x);
        res.besti = 0;
        res.i = 1;
        res.j = 1;
        res.bestj = 0;*/
        //System.out.println("Best position found is in matrix "+res.i+","+res.j+" in position "+res.besti+","+res.bestj);
        //System.out.println("Training vector 100 times");
        customWatch myWatch = new customWatch();
        myWatch.start();
        for (int i=0; i<1000; i++) {
            bestPosition res = z.searchBestPosition(x);
            z.learnVector(neuron, res.besti, res.bestj, res.i, res.j);
            myWatch.watch();
        }
        z.setEOS();
        z.join();
        myWatch.end();
        System.out.println("MSOM splitted in "+split+"x"+split+" took "+(myWatch.getExecutionTime()/1000)+"ms");
    }
}
