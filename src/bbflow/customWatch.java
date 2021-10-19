package bbflow;

import java.util.ArrayList;

/**
 * custom time watch class to measure &amp; print performance
 */
public class customWatch {
    long starttime = 0;
    long endtime = 0;
    ArrayList<Long> steps = new ArrayList<>();

    public void start() {
        starttime = System.nanoTime();
    }

    public void end() {
        endtime = System.nanoTime();
    }

    public void watch() {
        steps.add(System.nanoTime());
    }

    public void printReport(boolean ms) {
        long lasttime = starttime;

        int divider = 1000;
        String unit = "us";
        if (ms) {
            divider *= 1000;
            unit = "ms";
        }

        int i=0;
        for (; i<steps.size(); i++) {
            System.out.println("Step "+i+": "+((steps.get(i)-lasttime)/divider)+unit);
            lasttime = steps.get(i);
        }

        System.out.println("Step "+(i)+": "+((endtime-lasttime)/divider)+unit);
        System.out.println("Total execution time: "+((endtime-starttime)/divider)+unit);
    }

    public void printReport() {
        printReport(false);
    }

    public long getExecutionTime() {
        return ((endtime-starttime)/1000);
    }
}
