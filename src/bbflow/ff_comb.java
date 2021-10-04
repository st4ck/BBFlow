package bbflow;

public class ff_comb<T> extends ff_pipeline<T> {
    public ff_comb(block<T> node1, block<T> node2, int bufferSize) {
        super(bufferSize);
        appendNewBB(node1);
        appendNewBB(node2);
    }

    public ff_comb(block<T> node1, block<T> node2) {
        appendNewBB(node1);
        appendNewBB(node2);
    }
}
