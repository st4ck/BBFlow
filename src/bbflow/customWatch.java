package bbflow;

import java.util.ArrayList;

/**
 * custom time watch class to measure & print performance
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

    public void printReport() {
        long lasttime = starttime;

        int i=0;
        for (; i<steps.size(); i++) {
            System.out.println("Step "+i+": "+((steps.get(i)-lasttime)/1000)+"us");
            lasttime = steps.get(i);
        }

        System.out.println("Step "+(i)+": "+((endtime-lasttime)/1000)+"us");
        System.out.println("Total execution time: "+((endtime-starttime)/1000)+"us");
    }
}
