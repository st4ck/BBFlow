package tests.benchmarks;

public class benchmark_farm_sequential {
    public static void main (String[] args) {
        int n = 1000;
        if (args.length == 1) {
            n = Integer.parseInt(args[0]);
        }

        Long[] x = new Long[n];
        for (int i = 0; i < n; ++i) {
            x[i] = Long.valueOf(i);
            long z = x[i];
            for (int j=0; j<1000000; j++) {
                z *= 1000;
                z /= 999;
            }
            x[i] = z;
        }

        System.out.println("done "+x[0]);
    }
}
