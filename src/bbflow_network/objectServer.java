package bbflow_network;

import bbflow.*;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

/**
 * Server socket single threaded used to receive data from another node through TCP channel
 * @param <T> Type of data received through TCP channel
 */
public class objectServer<T> implements Runnable {
    int serverPort = 44444;
    ServerSocket serverSocket = null;
    boolean isStopped = false;
    Thread runningThread = null;
    ff_queue<T> queue;

    public objectServer(int connectionId, ff_queue<T> q){
        this.serverPort += connectionId;
        queue = q;
    }

    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();

        while(!isStopped()){
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Server Stopped.") ;
                    return;
                }
                throw new RuntimeException("Error accepting client connection", e);
            }
            try {
                receiveData(clientSocket);
                if (queue.getEOS()) {
                    stop();
                    break;
                }
            } catch (Exception e) {
                //log exception and go on to next request.
            }
        }

        System.out.println("Server Stopped.");
    }

    public void receiveData(Socket clientSocket) {
        try {
            InputStream input = clientSocket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(input);

            while (true) {
                Object o = ois.readObject();
                if (o instanceof String) {
                    if (((String) o).compareTo("EOS") == 0) {
                        queue.setEOS();
                        break;
                    }
                } else {
                    queue.put((T) o);
                }
            }
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            //report exception somewhere.
            e.printStackTrace();
        }
    }


    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port", e);
        }
    }
}