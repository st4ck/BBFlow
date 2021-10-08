package bbflow;

import java.io.Serializable;

/**
 * default worker extending default job
 * it should be extended doing nothing
 * @param <T> Custom type of the channels
 */
public class defaultWorker<T,U> extends defaultJob<T,U> implements Serializable {

}
