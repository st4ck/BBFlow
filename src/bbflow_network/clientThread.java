package bbflow_network;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;

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
                outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
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
