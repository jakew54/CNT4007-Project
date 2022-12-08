import java.net.*;
import java.io.*;

public class Server implements Runnable {
    private Peer peer;
    private Socket acceptedSocket;
    private ServerSocket serverSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private LogManager logger;

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

    public Server(Peer peer, LogManager logger) {
        this.peer = peer;
        this.logger = logger;
    }

    public void run() {

        serverSocket = createServerSocket(peer);
        try {

            while (true) {
                acceptedSocket = serverSocket.accept();

                out = new ObjectOutputStream(acceptedSocket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(acceptedSocket.getInputStream());

                MessageManager msgManager = new MessageManager(peer, out, in, logger);

                Thread msgThread = new Thread(msgManager);
                msgThread.start();

                peer.addMsgManagerToList(msgManager);
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