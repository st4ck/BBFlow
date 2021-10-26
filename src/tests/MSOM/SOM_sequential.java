package tests.MSOM;

import bbflow.customWatch;

import java.util.ArrayList;

public class SOM_sequential {
    static int depth = 3;
    static int width = 1024;
    static int height = 1024;
    static double[][][] som;

    static void searchAndLearn(ArrayList<Double> line) {
        double[] neuron = new double[depth];
        double n_min = 10000;
        for (int n=0; n<depth; n++) {
            neuron[n] = line.get(n);
            // find minimum excluding last
            if (n < (depth-1)) {
                if (n_min > neuron[n]) {
                    n_min = neuron[n];
                }
            }
        }

        /* Convert vector to custom format */
        for (int n=0; n<depth; n++) {
            neuron[n] -= n_min;
            //cout << neuron[n] << ",";
        }

        //cout << endl;

        int bestw = 0;
        int besth = 0;
        double bestdist = Double.MAX_VALUE;

        for (int w=0; w<width; w++) {
            //cout << "Row " << (w+1) << "\n";
            for (int h=0; h<height; h++) {
                // Euclidean distance between D-dimensional points
                double distance = 0;
                for (int d=0; d<depth; d++) {
                    distance += (som[w][h][d] - neuron[d]) * (som[w][h][d] - neuron[d]);

                    if (distance > bestdist) { // avoid unuseful loops
                        break;
                    }
                }

                if (distance < bestdist) {
                    bestdist = distance;
                    bestw = w;
                    besth = h;
                }
            }
        }

        //System.out.println("Best vector position "+bestw + ":" + besth + " Distance: " + bestdist);

        int circ = 5;

        for (int i=Math.max(0,bestw-circ); i<=Math.min(width-1,bestw+circ); i++) {
            for (int j=Math.max(0,besth-circ); j<=Math.min(height-1,besth+circ); j++) {
                double flatfactor = Math.max(Math.abs(i-bestw),Math.abs(j-besth));
                //double curve = 0.5/(flatfactor*0.2);
                //double curve = 0.2/pow(1.7,flatfactor);

                double curve = 0.2/Math.pow(1.3,flatfactor);
                //double curve = 0.5/pow(1.3,flatfactor);

                for (int d=0; d<depth; d++) {
                    som[i][j][d] = som[i][j][d] * (1-curve) + neuron[d] * curve;
                    som[i][j][d] = Math.round(som[i][j][d]*100)/100;
                }
            }
        }
    }

    static void randomize() {
        for (int w=0; w<width; w++) {
            for (int h=0; h<height; h++) {
                for (int d=0; d<depth; d++) {
                    som[w][h][d] = (int)(Math.random()*255);
                    //som[w][h][d] = 0;
                }
            }
        }
    }

    public static void main (String[] args) {
        som = new double[width][height][depth];

        ArrayList<Double> vector = new ArrayList<>();
        for (int j=0; j<depth; j++) {
            vector.add(Math.random()*255);
        }
        ArrayList<Double> neuron = SOM.normalize(vector,depth);

        randomize();

        customWatch myWatch = new customWatch();
        myWatch.start();

        for (int i=0; i<100000; i++) {
            searchAndLearn(neuron);
        }

        myWatch.end();
        System.out.println("SOM took "+(myWatch.getExecutionTime()/1000)+"ms");
    }
}
