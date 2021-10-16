package tests.MSOM;

import bbflow.customWatch;

import java.util.ArrayList;

public class test_MSOM {
    public static void main (String[] args) {
        //testMSOM(Integer.parseInt(args[0]));
        testMSOM(1);
        testMSOM(2);
        testMSOM(4);
        testMSOM(8);
        testMSOM(16);
    }

    private static void testMSOM(int split) {
        int depth = 3;
        int searchMaxThreads = 8;
        MSOM z = new MSOM(1024,depth,split);
        z.start();
        customWatch myWatch = new customWatch();
        myWatch.start();
//        double[] x = {9,90,70};
        /*bestPosition res = z.searchBestPosition(x);
        res.besti = 0;
        res.i = 1;
        res.j = 1;
        res.bestj = 0;*/
        //System.out.println("Best position found is in matrix "+res.i+","+res.j+" in position "+res.besti+","+res.bestj);
        //System.out.println("Training vector 100 times");
        for (int i=0; i<100; i++) {
            ArrayList<Double> vector = new ArrayList<>();
            for (int j=0; j<depth; j++) {
                vector.add(Math.random()*255);
            }
            ArrayList<Double> neuron = SOM.normalize(vector,depth);
            bestPosition res = z.searchBestPositionThreaded(neuron, searchMaxThreads);

            /*for (int x=Math.max(0,res.i-1); x<=Math.min(res.i+1, split-1); x++) {
                for (int y=Math.max(0,res.j-1); y<=Math.min(res.j+1, split-1); y++) {
                    z.start(x,y);
                }
            }*/
            z.learnVector(neuron, res.besti, res.bestj, res.i, res.j);
            myWatch.watch();
        }
        z.setEOS();
        z.join();
        myWatch.end();
        System.out.println("MSOM splitted in "+split+"x"+split+" took "+(myWatch.getExecutionTime()/1000)+"ms");
    }
}
