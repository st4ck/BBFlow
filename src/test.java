import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Test class with a simple farm and sum of sequential numbers
 */
public class test {
    public static void main (String[] args) {
        int EOF = -1;
        LinkedList<Integer> input_data = new LinkedList<Integer>();
        for (int i=0; i<1000000; i++) {
            input_data.add(i);
        }
        input_data.add(-1);

        LinkedList<defaultJob<Integer>> worker_job = new LinkedList<>();
        int n_workers = 1;
        for (int i=0; i<n_workers; i++) {
            worker_job.add(new testWorker<Integer>(i, EOF));
        }

        ff_farm x = new ff_farm<Integer>(n_workers, worker_job, EOF);
        x.addInputChannel(input_data);
        x.run();
    }
}
