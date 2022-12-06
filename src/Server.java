import java.net.*;
import java.io.*;

public class Server implements Runnable {
    private Peer peer;
    private Socket acceptedSocket;
    private ServerSocket serverSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private ServerSocket createServerSocket(Peer serverPeer) {
        try {
            ServerSocket ss = new ServerSocket(serverPeer.getPeerPortNum());
            return ss;
        } catch (IOException e) {
            System.err.println("Server socket failed to start (Server.java)");
            e.printStackTrace();
        }
        return null;
    }

    public Server(Peer peer) {
        this.peer = peer;
    }

    public void run() {
        serverSocket = createServerSocket(peer);
        try {
            while (true) {
                acceptedSocket = serverSocket.accept();
                in = new ObjectInputStream(acceptedSocket.getInputStream());
                out = new ObjectOutputStream(acceptedSocket.getOutputStream());
                out.flush();
                MessageManager msgManager = new MessageManager(peer, out, in);
                Thread msgThread = new Thread(msgManager);
                msgThread.start();
            }
        } catch (IOException e) {
            System.err.println("serverSocket.accept() IO error (Server.java)");
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                acceptedSocket.close();
            } catch (IOException e) {
                System.err.println("Disconnect with Client");
                e.printStackTrace();
            }
        }
    }
}