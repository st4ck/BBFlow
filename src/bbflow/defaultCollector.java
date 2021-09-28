package bbflow;

/**
 * default Collector of bbflow.ff_farm. Can be extended and runJob() overwritten
 * @param <T> Custom type of channels
 */
public class defaultCollector<T> extends defaultJob<T> {
    /**
     * basic constructor of bbflow.defaultCollector
     * @param EOF End of File symbol to detect stream finished
     */
    public defaultCollector(T EOF) {
        this.EOF = EOF;
    }

    @Override
    public void runJob() {

    }
}
