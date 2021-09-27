import java.util.ArrayList;
import java.util.LinkedList;

/**
 * worker code extending defaultJob for the test of the farm
 * @param <T>
 */
public class testWorker<T> extends defaultJob<T> {
    public testWorker(int id, T EOF) {
        this.id = id;
        this.EOF = EOF;
    }

    Integer mysum = 0;
    @Override
    public void runJob() {
        T received;
        LinkedList<T> in_channel = in.get(0);

        received = in_channel.get(0);
        if (received == EOF) {
            System.out.println(id + ": EOF");
            in.remove(0); // removing input channel, sequence finished
            return;
        }

        mysum += (Integer) received;
        in_channel.remove(0);

        //System.out.println(id + ": (" + (Integer) received + ") " + mysum);
    }
}
