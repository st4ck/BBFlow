package tests.MSOM;

import bbflow.bb_settings;
import bbflow.customWatch;

import java.util.ArrayList;

public class test_MSOM {
    public static void main (String[] args) {
        bb_settings.BLOCKING = true;
        bb_settings.BOUNDED = false;

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
    }

    private static void testMSOM(int size, int split) {
        int depth = 100;
        MSOM z = new MSOM(size,depth,split);
        z.start();
        customWatch myWatch = new customWatch();
        myWatch.start();

        for (int i=0; i<100; i++) {
            ArrayList<Double> vector = new ArrayList<>();
            for (int j=0; j<depth; j++) {
                vector.add(Math.random()*255);
            }
            ArrayList<Double> neuron = SOM.normalize(vector,depth);
            SOMData searchLearn = new SOMData(SOMData.SEARCH_AND_LEARN, i);
            searchLearn.neuron = neuron;
            z.push(searchLearn);
        }
        z.setEOS();
        z.join();
        myWatch.end();
        System.out.println("MSOM splitted in "+split+"x"+split+" took "+(myWatch.getExecutionTime()/1000)+"ms");
    }
}
