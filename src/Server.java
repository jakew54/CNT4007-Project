import java.net.*;
import java.io.*;

public class Server implements Runnable {
    private Peer peer;
    private Socket acceptedSocket;
    private ServerSocket serverSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public Server(Peer peer) {
        this.peer = peer;
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(peer.getPeerPortNum());
        } catch (IOException e) {
            System.err.println("Server socket failed to start (Server.java)");
            e.printStackTrace();
        }
        try {
            while (true) {
                acceptedSocket = serverSocket.accept();
                out = new ObjectOutputStream(acceptedSocket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(acceptedSocket.getInputStream());

            }
        } catch (IOException e) {
            System.err.println("serverSocket.accept() IO error (Server.java)");
            e.printStackTrace();
        } finally {
            try {
                acceptedSocket.close();
            } catch (IOException e) {
                System.err.println("acceptedSocket.close() IO error (Server.java)");
                e.printStackTrace();
            }
        }
    }
}