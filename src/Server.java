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
                System.out.println("entered while loop of Server");
                acceptedSocket = serverSocket.accept();
                System.out.println("Accepts socket");
                out = new ObjectOutputStream(acceptedSocket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(acceptedSocket.getInputStream());
                System.out.println("Creates out and in for server");
                MessageManager msgManager = new MessageManager(peer, out, in);
                System.out.println("Made msg Manager for server");
                Thread msgThread = new Thread(msgManager);
                msgThread.start();

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