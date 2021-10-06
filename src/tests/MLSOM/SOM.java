package tests.MLSOM;

public class SOM {
    double[][][] som;
    int height = 0;
    int width = 0;
    int depth = 0;

    public SOM(int w, int h, int d) {
        som = new double[w][h][d];
        this.width = w;
        this.height = h;
        this.depth = d;

        randomize();
    }

    public SOM(int w, int h, int d, double[][][] som) {
        this.som = som;
        this.width = w;
        this.height = h;
        this.depth = d;
    }

    void randomize() {
        for (int w=0; w<width; w++) {
            for (int h=0; h<height; h++) {
                for (int d=0; d<depth; d++) {
                    som[w][h][d] = ((int) Math.random()*100);
                }
            }
        }
    }

    void learnVector(double[] line) {
        double[] neuron = new double[depth];

        double n_min = 10000;
        for (int n=0; n<depth; n++) {
            neuron[n] = line[n];
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
        }

        //Searching nearest similar vector

        int bestw = 0;
        int besth = 0;
        double bestdist = (depth*depth) * (depth*depth);
        bestdist = bestdist*bestdist;

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

        System.out.println("Best vector position " + bestw + ":" + besth + " Distance: " + bestdist);

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
}
