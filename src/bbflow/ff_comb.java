package bbflow;

/**
 * Combine two nodes (of any type) in one
 * @param <T> Custom type of channels
 */
public class ff_comb<T,V> extends ff_pipeline<T,V> {
    public ff_comb(block<T,Object> node1, block<Object,V> node2, int bufferSize) {
        super(node1,node2,bufferSize);
    }

    public ff_comb(block<T,Object> node1, block<Object,V> node2) {
        super(node1,node2);
    }
}
