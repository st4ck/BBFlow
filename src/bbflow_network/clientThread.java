package bbflow_network;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import bbflow.*;

/**
 * Thread client of objectClient
 */
public class clientThread implements Runnable {
    String host;
    int connPort;
    Socket clientSocket = null;

    ObjectOutputStream outToServer;

    public clientThread(int connPort, String host) throws IOException, InterruptedException {
        this.connPort = connPort;
        this.host = host;
    }

    @Override
    public void run() {
        clientSocket = null;
        outToServer = null;

        while(true) {
            try {
                if ((clientSocket == null) || (!clientSocket.isConnected())) {
                    clientSocket = new Socket(host, connPort);
                }

                if (bb_settings.bufferedTCP) {
                    outToServer = new ObjectOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
                } else {
                    outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
                }
                break;
            } catch (IOException e) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
