package tests.MSOM;

import bbflow.customWatch;

import java.util.ArrayList;

public class test_MSOM {
    public static void main (String[] args) {
        //testMSOM(Integer.parseInt(args[0]));
        testMSOM(1024,1);
        testMSOM(1024,2);
        testMSOM(1023,3);
        testMSOM(1024,4);
        testMSOM(1025,5);
        testMSOM(1026,6);
        testMSOM(1029,7);
        testMSOM(1024,8);
        testMSOM(1026,9);
        testMSOM(1030,10);
        testMSOM(1023,11);
        testMSOM(1020,12);
        testMSOM(1027,13);
        testMSOM(1022,14);
        testMSOM(1020,15);
        testMSOM(1024,16);
    }

    private static void testMSOM(int size, int split) {
        int depth = 3;
        int searchMaxThreads = 8;
        MSOM z = new MSOM(size,depth,split);
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
