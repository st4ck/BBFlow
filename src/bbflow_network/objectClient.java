package bbflow_network;

import java.io.IOException;
import bbflow.*;

/**
 * class to manage connection to server socket in order to send data through TCP channel
 * Auto-reconnection in case of fault is implemented
 */
public class objectClient {
    int serverPort = bb_settings.serverPort;
    int connPort;
    String host;
    clientThread cc;
    Thread ccThread;
    public objectClient(int connectionId, String host) throws IOException, InterruptedException {
        connPort = serverPort+connectionId;
        this.host = host;
        cc = new clientThread(connPort, host);
        ccThread = new Thread(cc);
        ccThread.start();
    }

    public void put(Object i) throws InterruptedException {
        while(true) {
            try {
                tryToPut(i);
                break;
            } catch (IOException e) {
                Thread.sleep(100);
            }
        }
    }

    public void tryToPut(Object i) throws IOException, InterruptedException {
        while(true) {
            if (cc.outToServer == null) {
                Thread.sleep(100);
                continue;
            }

            try {
                cc.outToServer.writeObject(i);
                break;
            } catch (IOException e) { // try until sent, connection closed?
                if (!cc.clientSocket.isConnected()) {
                    ccThread.start();
                }
            }
        }
    }

    public void setEOS() throws InterruptedException {
        put("EOS");
    }
}
