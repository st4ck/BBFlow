package bbflow;

/**
 * default worker extending default job
 * it should be extended doing nothing
 * @param <T> Custom type of the channels
 */
public class defaultWorker<T> extends defaultJob<T,T> {
    public defaultWorker(int id) {
        this.id = id;
    }

    /**
     * in this method the custom code should be added overriding this class
     * see default job for channels and locks name
     */
    @Override
    public void runJob() {

    }
}
