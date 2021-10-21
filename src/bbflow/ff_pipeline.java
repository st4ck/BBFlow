package bbflow;

/**
 * Pipeline building block allows auto-connection between all types of ff_blocks
 */
public class ff_pipeline<T,V> extends ff_node<T,V> {
    pipeline_generic pipe = null;
    int bufferSize = bb_settings.defaultBufferSize;

    /**
     * one single channel between two blocks. Using addInputChannel and addOutputChannel. If emitter or collector null in case of farm, 1_N or N_1 or N_N or NxM used.
     */
    public static byte TYPE_1_1 = 0;
    /**
     * N channels connecting first block to N elements in the second block. Used between collector of left farm to workers of second for example. If collector null, N_N or NxM used
     */
    public static byte TYPE_1xN = 1;
    /**
     * N channels connecting N elements in the first block to the second block. Used for example between farm workers of first block to emitter of second. If emitter null, N_N or NxM used
     */
    public static byte TYPE_Nx1 = 2;
    /**
     * N channels connecting 1 to 1 elements of the two blocks. Example N workers from left block and N workers of right block. The cardinality must be the same, otherwise NxM used. Emitter and colletor must be null, otherwise 1_N or N_1 is used.
     */
    public static byte TYPE_N_N = 3;
    /**
     * connect all N workers from left to all M workers from right.
     */
    public static byte TYPE_NxM = 4;

    /**
     * constructor of pipeline between two blocks of type 1-1 (one channel created between b1 and b2)
     * @param b1 first block
     * @param b2 second block
     */
    public ff_pipeline(block<T,Object> b1, block<Object,V> b2) {
        this(b1,b2,TYPE_1_1);
    }

    /**
     * constructor of pipeline between two blocks of type 1-1 (one channel created between b1 and b2)
     * @param b1 first block
     * @param b2 second block
     * @param bufferSize buffer size of the channel
     */
    public ff_pipeline(block<T,Object> b1, block<Object,V> b2, int bufferSize) {
        this(b1,b2,bufferSize,TYPE_1_1);
    }

    /**
     * constructor of pipeline between two blocks of type MULTI (see types)
     * @param b1 first block
     * @param b2 second block
     * @param MULTI TYPE_1_1, TYPE_1_N, TYPE_Nx1, TYPE_N_N, TYPE_NxM
     */
    public ff_pipeline(block<T,Object> b1, block<Object,V> b2, byte MULTI) {
        pipeline_generic<T,Object,V> p = new pipeline_generic<>(bufferSize);
        if (MULTI != TYPE_1_1) {
            p.createPipeMulti(b1, b2, MULTI);
        } else {
            p.createPipe(b1, b2);
        }
        pipe = p;
    }

    /**
     * constructor of pipeline between two blocks of type MULTI (see types)
     * @param b1 first block
     * @param b2 second block
     * @param bufferSize buffer size of the channels
     * @param MULTI TYPE_1_1, TYPE_1_N, TYPE_Nx1, TYPE_N_N, TYPE_NxM
     */
    public ff_pipeline(block<T,Object> b1, block<Object,V> b2, int bufferSize, byte MULTI) {
        this.bufferSize = bufferSize;
        pipeline_generic<T,Object,V> p = new pipeline_generic<>(bufferSize);
        if (MULTI != TYPE_1_1) {
            p.createPipeMulti(b1, b2, MULTI);
        } else {
            p.createPipe(b1, b2);
        }
        pipe = p;
    }

    /**
     * append a new block to pipeline connected
     * @param newblock
     * @param MULTI
     */
    public void appendBlock(block<Object,Object> newblock, byte MULTI) {
        if (pipe != null) {
            pipe.appendBlock(newblock, MULTI);
        }
    }

    /**
     * add a new input channel to the first block of the pipeline
     * @param input input channel
     */
    public void addInputChannel(ff_queue<T> input) {
        if (pipe != null) {
            pipe.addInputChannel(input);
        }
    }

    /**
     * add a new output channel to the last block of the pipeline
     * @param output output channel
     */
    public void addOutputChannel(ff_queue<V> output) {
        if (pipe != null) {
            pipe.addOutputChannel(output);
        }
    }

    /**
     * start all blocks in the pipeline
     */
    public void start() {
        if (pipe != null) {
            pipe.start();
        }
    }

    /**
     * wait end of all blocks in the pipeline
     */
    public void join() {
        if (pipe != null) {
            pipe.join();
        }
    }

    /**
     * get first farm in the pipeline or null if not
     * @return return first farm in the pipeline or null
     */
    public ff_farm getFirstFarm() {
        if (pipe == null) {
            return null;
        }

        return pipe.getFirstFarm();
    }

    /**
     * get last farm in the pipeline or null if not
     * @return return last farm in the pipeline or null
     */
    public ff_farm getLastFarm() {
        if (pipe == null) {
            return null;
        }

        return pipe.getLastFarm();
    }
}
