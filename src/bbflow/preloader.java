package bbflow;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Class providing a static method used to invite JVM to preload main classes used in BBFlow.
 */
public class preloader {
    public static void preloadJVM() {
        ff_queue.preload();
        block.preload();
        node.preload();
        ff_node.preload();
        defaultJob.preload();
        defaultEmitter.preload();
        defaultWorker.preload();
        ff_pipeline.preload();
        ff_all2all.preload();
        ff_farm.preload();
        ff_comb.preload();
        pipeline_generic.preload();
        squeue.preload();

        LinkedList<Object> x = new LinkedList<>();
        Thread y = new Thread();
        Runnable z = new Runnable() {
            @Override
            public void run() {

            }
        };

        ArrayList<Object> w = new ArrayList<>();
    }
}
