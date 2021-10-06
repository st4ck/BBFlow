package tests.MLSOM;

import java.util.ArrayList;

public class MLSOM {
    int layers = 1;
    int s;
    int depth;
    ArrayList<Integer> layersSize;
    ArrayList<SOM> soms;

    /**
     * default size of each layer is 8x8. w/h must be pow of 2
     * @param s
     * @param d
     * @param layers
     */
    public MLSOM(int s, int d, int layers) {
        this.s = s;
        this.depth = d;
        layersSize = new ArrayList<>();
        int layersize = s;
        layersSize.add(layersize);
        for (int i=2; i<=layers; i++) {
            if (layersize >= 128) {
                layersize /= 8;
                layersSize.add(layersize);
                this.layers = i;
            } else {
                break;
            }
        }

        System.out.println("MLSOM of "+this.layers+" layers");

        for (int i=0; i<s/layersize; i++) {
            soms.add(new SOM(layersize,layersize,d));
        }
    }

    int checksumX(int[] array) {
        int c = 0;
        for(int i = 0; i < depth; i++) {
            c += 7*(s+array[i]);
        }
        return c;
    }

    int checksumY(int[] array) {
        int c = 0;
        for(int i = 0; i < depth; i++) {
            c += 3*(s+array[i]);
        }
        return c;
    }

    public ArrayList<Integer[]> calcSOMPosition(int[] vector) {
        int x = checksumX(vector);
        int y = checksumY(vector);
        ArrayList<Integer[]> coordinates = new ArrayList<>();
        for (int i=0; i<layers; i++) {
            coordinates.add(new Integer[]{x % layersSize.get(i), y % layersSize.get(i)});
        }

        return coordinates;
    }
}
