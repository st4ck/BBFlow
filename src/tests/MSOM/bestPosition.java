package tests.MSOM;

public class bestPosition {
    public int besti = 0;
    public int bestj = 0;
    public double bestdist = 10000;
    public int i = -1;
    public int j = -1;

    public bestPosition(int depth) {
        double bestdist = (depth*depth) * (depth*depth);
        this.bestdist = bestdist*bestdist;
    }
}
