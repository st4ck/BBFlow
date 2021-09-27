import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class defaultWorker<T> extends defaultJob<T> {
    public defaultWorker(int id, T EOF) {
        this.id = id;
        this.EOF = EOF;
    }

    @Override
    public void runJob() {

    }
}
