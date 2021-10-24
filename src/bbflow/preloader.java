package bbflow;

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
    }
}
