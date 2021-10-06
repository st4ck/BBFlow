package tests.MLSOM;

import bbflow.*;

import java.util.LinkedList;

public class test_MLSOM {
    public static void main (String[] args) {
        MLSOM z = new MLSOM(1024,10,3);
        int[] x = {9,90,244,15,2,0,42,0,9,12};
        z.calcSOMPosition(x);
    }
}
