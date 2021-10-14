package bbflow;

import bbflow_network.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Queue extending ff_queue exploting a network channel. The channel is uni-directional and type of it must be specified during initialization (INPUT/OUTPUT)
 * @param <T> Type of channels of the queue
 */
public class ff_queue_TCP<T> extends ff_queue<T> {
    objectClient client = null;
    Thread server = null;
    public final static int INPUT = 1;
    public final static int OUTPUT = 2;
    int sockettype;
    public int connectionId = -1;
    public ff_queue_TCP(int type, int connectionId, String host) {
        super(false, false,0);
        this.connectionId = connectionId;
        try {
            if (type == INPUT) {
                objectServer<T> s = new objectServer<T>(connectionId, this);
                server = new Thread(s);
                server.start();
                sockettype = INPUT;
            } else if ((type == OUTPUT) && (host != null)) {
                client = new objectClient(connectionId, host);
                sockettype = OUTPUT;
            }
        } catch (IOException | InterruptedException e) {
            System.out.println(e.toString());
        }

    }

    public ff_queue_TCP(int type, int connectionId) {
        this(type, connectionId,null);
    }

    public void put(T i) {
        if (this.EOS) { return; }

        if (sockettype == OUTPUT) {
            try {
                client.put(i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (sockettype == INPUT) {
            super.put(i);
        }
    }

    /**
     * tell the Queue the end of stream reached
     */
    public void setEOS() {
        if (sockettype == OUTPUT) {
            try {
                client.put("EOS");
            } catch (InterruptedException e) {

            }

            this.EOS = true;
        } else {
            super.setEOS();
        }
    }

    public boolean offer(T i, long timeout, TimeUnit timeunit) throws InterruptedException {
        if (sockettype == OUTPUT) {
            client.put(i);
            return true;
        } else {
            return super.offer(i, timeout, timeunit);
        }
    }

    public boolean offer(T i) {
        if (sockettype == OUTPUT) {
            try {
                client.put(i);
            } catch (InterruptedException e) {

            }
            return true;
        } else {
            return super.offer(i);
        }
    }
}
