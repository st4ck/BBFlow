package bbflow;

/**
 * Combine two nodes (of any type) in one
 * @param <T> Custom type of channels
 */
public class ff_comb<T> extends ff_pipeline<T> {
    public ff_comb(block<T> node1, block<T> node2, int bufferSize) {
        super(bufferSize);
        appendNewBB(node1, false, false);
        appendNewBB(node2, false, false);
    }

    public ff_comb(block<T> node1, block<T> node2) {
        appendNewBB(node1);
        appendNewBB(node2);
    }
}
