import java.net.*;
import java.io.*;

public class Server implements Runnable {
    private Peer peer;
    private Socket acceptedSocket;
    private ServerSocket serverSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private ServerSocket createServerSocket(Peer serverPeer) {
        System.out.println("Enters createServerSocket function");
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
        System.out.println("Enters run() of Server...");
        serverSocket = createServerSocket(peer);
        try {

            while (true) {
                System.out.println("Enters while loop");
                acceptedSocket = serverSocket.accept();
                System.out.println("Completes acceptedSocket");
                out = new ObjectOutputStream(acceptedSocket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(acceptedSocket.getInputStream());
                System.out.println("Done with creating in and out");
                MessageManager msgManager = new MessageManager(peer, out, in);
                System.out.println("Done with creating msg manager");
                Thread msgThread = new Thread(msgManager);
                msgThread.start();
                System.out.println("Starts msgThread");
                int connectingPeerID = 0;
                for (int i = 0; i < peer.getConnectedPeers().size(); i++) {
                    if (!(peer.getMsgManagerList().containsKey(peer.getConnectedPeers().get(i)))) {
                        connectingPeerID = peer.getConnectedPeers().get(i);
                    }
                }
                peer.addMsgManagerToList(connectingPeerID, msgManager);
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