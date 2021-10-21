package bbflow;

import java.util.ArrayList;

/**
 * custom time watch class to measure &amp; print performance
 */
public class customWatch {
    long starttime = 0;
    long endtime = 0;
    ArrayList<Long> steps = new ArrayList<>();

    /**
     * start the watch
     */
    public void start() {
        starttime = System.nanoTime();
    }

    /**
     * stop the watch
     */
    public void end() {
        endtime = System.nanoTime();
    }

    /**
     * register a new step in the watch
     */
    public void watch() {
        steps.add(System.nanoTime());
    }

    /**
     * print report of the registered times
     * @param ms true if report in milliseconds, otherwise microseconds used
     */
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

    /**
     * print report in microseconds
     */
    public void printReport() {
        printReport(false);
    }

    /**
     * return the total execution time
     * @return total execution time
     */
    public long getExecutionTime() {
        return ((endtime-starttime)/1000);
    }
}
