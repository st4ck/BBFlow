package tests.MSOM;

import bbflow.bb_settings;
import bbflow.customWatch;

import java.util.ArrayList;

/**
 * test function of MSOM with some benchmark with different number of cores used
 */
public class test_MSOM {
    public static void main (String[] args) {
        bb_settings.BLOCKING = false;
        bb_settings.BOUNDED = false;
        bb_settings.backOff = 5000;

        //testMSOM(Integer.parseInt(args[0]));
        if (args.length == 0) {
            testMSOM(1024, 1);
            testMSOM(1024, 2);
            testMSOM(1023, 3);
            testMSOM(1024, 4);
            testMSOM(1025, 5);
            testMSOM(1026, 6);
            testMSOM(1029, 7);
            testMSOM(1024, 8);
            testMSOM(1026, 9);
            testMSOM(1030, 10);
            testMSOM(1023, 11);
        } else {
            switch (Integer.parseInt(args[0])) {
                case 1:
                    testMSOM(1024, 1);
                    break;
                case 2:
                    testMSOM(1024, 2);
                    break;
                case 3:
                    testMSOM(1023, 3);
                    break;
                case 4:
                    testMSOM(1024, 4);
                    break;
                case 5:
                    testMSOM(1025, 5);
                    break;
                case 6:
                    testMSOM(1026, 6);
                    break;
                case 7:
                    testMSOM(1029, 7);
                    break;
                case 8:
                    testMSOM(1024, 8);
                    break;
                case 9:
                    testMSOM(1026, 9);
                    break;
                case 10:
                    testMSOM(1030, 10);
                    break;
                case 11:
                    testMSOM(1023, 11);
                    break;
            }
        }
    }

    private static void testMSOM(int size, int split) {
        int depth = 3;
        MSOM z = new MSOM(size,depth,split);
        z.randomize(0,255,1);

        ArrayList<Double> vector = new ArrayList<>();
        for (int j=0; j<depth; j++) {
            vector.add(Math.random()*255);
        }
        ArrayList<Double> neuron = SOM.normalize(vector,depth);
        double[] d_neuron = new double[depth];
        for (int j=0; j<depth; j++) {
            d_neuron[j] = neuron.get(j);
        }

        z.start();
        customWatch myWatch = new customWatch();
        myWatch.start();

        for (int i=0; i<100000; i++) {
            SOMData searchLearn = new SOMData(SOMData.SEARCH_AND_LEARN, i);
            searchLearn.neuron = d_neuron;
            z.push(searchLearn);
        }
        z.setEOS();
        z.join();
        myWatch.end();
        System.out.println("MSOM splitted in "+split+"x"+split+" took "+(myWatch.getExecutionTime()/1000)+"ms");
    }
}